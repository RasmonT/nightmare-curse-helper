@echo off
setlocal enabledelayedexpansion
REM ---------------------------------------------------------------------------
REM  Nightmare Curse Helper - launches RuneLite with this plugin compiled in.
REM
REM  The first run downloads Gradle and the RuneLite client. That is a few
REM  hundred megabytes and takes a few minutes; every run after it starts in
REM  seconds. Your normal RuneLite settings and Plugin Hub plugins are all
REM  still there - this is the same client, just with one extra plugin.
REM
REM  JAGEX ACCOUNT: a client started this way cannot log in through the Jagex
REM  Launcher. It logs in with the session the launcher wrote to
REM  .runelite\credentials.properties (see README.md, "Jagex account").
REM
REM  YUBIKEY: that file is kept encrypted at rest as
REM  credentials.properties.age (see ..\YUBIKEY-CREDENTIALS.md). When the
REM  encrypted copy exists, this script decrypts it with the YubiKey (PIN +
REM  touch), starts the client and deletes the plaintext again. The plaintext
REM  only has to exist while the client starts; the session lives in memory
REM  after that.
REM
REM  The --insecure-write-credentials flag is deliberately NOT left in the
REM  Jagex Launcher on this machine. To renew the session: add the flag in
REM  "RuneLite (configure)" -> Client arguments, launch RuneLite once through
REM  the Jagex Launcher, remove the flag, then run ..\ENCRYPT-CREDENTIALS.bat.
REM ---------------------------------------------------------------------------

REM CREDS_MODE=env  : decrypt the session straight into this window's
REM                   environment (JX_SESSION_ID, JX_CHARACTER_ID, ...). The
REM                   client inherits it, exactly like it does from the Jagex
REM                   Launcher. No plaintext file is ever written.
REM CREDS_MODE=file : decrypt to .runelite\credentials.properties, start the
REM                   client, delete the file once the client has started
REM                   (EARLY_DELETE=1) and again when it exits. Proven to work.
REM If "env" does not log you in, switch to "file".
set "CREDS_MODE=env"
set "EARLY_DELETE=1"
REM --no-daemon is required in env mode: a Gradle daemon that is already
REM running keeps the environment it was started with, so the client would
REM not see the session (or would see an old one after a renewal).
set "GRADLE_ARGS=--no-daemon"

set "RL_DIR=%USERPROFILE%\.runelite"
set "CREDS=%RL_DIR%\credentials.properties"
set "CREDS_AGE=%CREDS%.age"
set "YK_ID=%RL_DIR%\yubikey-identity.txt"
set "CLIENT_LOG=%RL_DIR%\logs\client.log"
set "AGE_DIR=%USERPROFILE%\Tools\age"
if exist "%AGE_DIR%\age.exe" set "PATH=%AGE_DIR%;%PATH%"

cd /d "%~dp0"

REM Gradle needs a JDK; RuneLite's own bundled runtime is a JRE and cannot compile.
where javac >nul 2>&1 && goto haveJdk
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto haveJdk

REM Not on PATH - look where the usual installers put it before giving up.
for /D %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do if exist "%%~D\bin\javac.exe" set "JAVA_HOME=%%~D"
for /D %%D in ("%ProgramFiles%\Microsoft\jdk-*") do if exist "%%~D\bin\javac.exe" set "JAVA_HOME=%%~D"
for /D %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~D\bin\javac.exe" set "JAVA_HOME=%%~D"
for /D %%D in ("%ProgramFiles%\Amazon Corretto\jdk*") do if exist "%%~D\bin\javac.exe" set "JAVA_HOME=%%~D"
if defined JAVA_HOME if exist "!JAVA_HOME!\bin\javac.exe" (
  echo Using JDK at !JAVA_HOME!
  goto haveJdk
)

echo.
echo   No JDK found on this machine.
echo   Gradle needs javac to build the plugin - a JRE is not enough.
echo.
echo   Install one with:
echo.
echo       winget install EclipseAdoptium.Temurin.21.JDK
echo.
echo   then CLOSE this window, open a new one, and run RUN.bat again.
echo   (A new window is needed so it picks up the changed PATH.)
echo.
pause
exit /b 1
:haveJdk

REM ---------------------------------------------------------------------------
REM  Credentials: decrypt the YubiKey-protected copy if there is one.
REM ---------------------------------------------------------------------------
set "PROTECTED=0"
if not exist "%CREDS_AGE%" (
  if exist "%CREDS%" (
    echo.
    echo   NOTE: credentials.properties is stored in the clear. Run
    echo   ..\ENCRYPT-CREDENTIALS.bat once to protect it with the YubiKey.
    echo.
  )
  goto startClient
)

set "PROTECTED=1"
where age >nul 2>&1 || (
  echo   age.exe not found. Put it in %AGE_DIR% or on PATH.
  echo   See ..\YUBIKEY-CREDENTIALS.md
  pause
  exit /b 1
)
where age-plugin-yubikey >nul 2>&1 || (
  echo   age-plugin-yubikey.exe not found. Put it in %AGE_DIR% or on PATH.
  echo   See ..\YUBIKEY-CREDENTIALS.md
  pause
  exit /b 1
)
if not exist "%YK_ID%" (
  echo   %YK_ID% is missing.
  echo   Recreate it with: age-plugin-yubikey --identity ^> "%YK_ID%"
  echo   Run that in cmd, not PowerShell - PowerShell writes UTF-16.
  pause
  exit /b 1
)

if /i "%CREDS_MODE%"=="env" goto decryptToEnv

REM --- file mode --------------------------------------------------------------
if exist "%CREDS%" (
  echo.
  echo   A plaintext credentials.properties is still there from an earlier run
  echo   that did not finish cleanly. Using it; it is deleted when done.
  echo.
  goto startClient
)
call :touchHint
age -d -i "%YK_ID%" -o "%CREDS%" "%CREDS_AGE%"
if errorlevel 1 (
  REM age may leave an empty or partial output file behind on failure.
  if exist "%CREDS%" del /q "%CREDS%"
  echo.
  echo   Decryption failed or was cancelled. RuneLite was not started.
  echo.
  pause
  exit /b 1
)
echo   Decrypted to file. Starting RuneLite...
echo.
goto startClient

REM --- env mode ---------------------------------------------------------------
:decryptToEnv
REM A leftover plaintext is not needed in this mode; remove it so the client
REM cannot pick it up instead of the environment.
if exist "%CREDS%" (
  del /q "%CREDS%"
  echo   Deleted a leftover plaintext credentials.properties.
)
call :touchHint
REM The decrypted properties never touch the disk: each KEY=VALUE line of the
REM age output becomes a variable of this window. Lines starting with # are
REM the comment Java's Properties writer adds.
set "LOADED="
for /f "usebackq eol=# tokens=1* delims==" %%A in (`age -d -i "%YK_ID%" "%CREDS_AGE%"`) do (
  set "%%A=%%B"
  set "LOADED=!LOADED! %%A"
)
if not defined JX_SESSION_ID (
  echo.
  echo   Decryption failed or was cancelled, or the file holds no JX_SESSION_ID.
  echo   RuneLite was not started.
  echo.
  pause
  exit /b 1
)
echo   Decrypted into this window's environment:!LOADED!
echo   Nothing was written to disk. Starting RuneLite...
echo.

:startClient
if "%PROTECTED%"=="0" (
  call gradlew.bat run
  pause
  exit /b
)

REM The client runs in the background of this window, with its (very noisy)
REM output sent to build\run-output.log, so only this script's messages show
REM here. The same messages are appended to .runelite\run-bat.log.
REM Once the client has logged "Client initialization took" (EARLY_DELETE=1)
REM the plaintext is deleted, and it is deleted again when the client exits,
REM in case the client wrote it back in the meantime.
set "DONE_FLAG=%TEMP%\nightmare-curse-helper-run-done"
set "RUN_LOG=%~dp0build\run-output.log"
set "BAT_LOG=%RL_DIR%\run-bat.log"
if not exist "%~dp0build" mkdir "%~dp0build"
del /q "%DONE_FLAG%" 2>nul
set "BEFORE=0"
if exist "%CLIENT_LOG%" for /f %%N in ('type "%CLIENT_LOG%" ^| find /c "Client initialization took"') do set "BEFORE=%%N"
REM EARLY_DONE=1 means "nothing more to do before the client exits".
set "EARLY_DONE=1"
if "%EARLY_DELETE%"=="1" set "EARLY_DONE=0"
call :say Starting the client. Gradle and RuneLite output: %RUN_LOG%
start "" /b cmd /c "(call gradlew.bat run %GRADLE_ARGS% > "%RUN_LOG%" 2>&1 && echo ok> "%DONE_FLAG%") || echo fail> "%DONE_FLAG%""
set /a WAITED=0

:waitLoop
timeout /t 3 /nobreak >nul
set /a WAITED+=3
if exist "%DONE_FLAG%" goto clientExited
if "!EARLY_DONE!"=="1" goto waitLoop
set "NOW=0"
if exist "%CLIENT_LOG%" for /f %%N in ('type "%CLIENT_LOG%" ^| find /c "Client initialization took"') do set "NOW=%%N"
REM The log rolled over and started again - the earlier count is meaningless now.
if !NOW! LSS !BEFORE! set "BEFORE=0"
if !NOW! GTR !BEFORE! (
  call :say Client started after !WAITED! s.
  call :wipe
  set "EARLY_DONE=1"
)
REM After 20 minutes (first run can download a lot) stop waiting for the
REM start-up line; the file is still deleted when the client exits.
if !WAITED! GEQ 1200 (
  call :say No start-up line in client.log after 20 minutes. Deleting the plaintext now.
  call :wipe
  set "EARLY_DONE=1"
)
goto waitLoop

:clientExited
set "RESULT="
set /p RESULT=<"%DONE_FLAG%"
del /q "%DONE_FLAG%" 2>nul
call :say Client exited (gradle result: !RESULT!).
for %%V in (JX_SESSION_ID JX_CHARACTER_ID JX_DISPLAY_NAME JX_ACCESS_TOKEN JX_REFRESH_TOKEN) do set "%%V="
call :wipe
if not exist "%CREDS%" call :say No plaintext credentials.properties on disk.
if /i "!RESULT!"=="fail" (
  echo.
  echo   Gradle or the client ended with an error. Last lines of the output:
  echo   ---------------------------------------------------------------
  powershell -NoProfile -Command "Get-Content -Tail 40 '%RUN_LOG%'"
  echo   ---------------------------------------------------------------
  echo   Full output: %RUN_LOG%
)
echo.
pause
exit /b

:touchHint
echo.
echo   Insert the YubiKey and enter the PIV PIN when asked.
echo   RIGHT AFTER THE PIN the key starts blinking: TOUCH IT within 15 seconds.
echo   There is no text prompt for the touch. Missing it = "Failed to decrypt".
echo.
exit /b

:say
echo   [RUN.bat %TIME:~0,8%] %*
>> "%BAT_LOG%" echo %DATE% %TIME:~0,8% %*
exit /b

:wipe
if exist "%CREDS%" (
  del /q "%CREDS%"
  call :say Plaintext credentials.properties deleted.
)
exit /b
