@REM ====================================================================
@REM ChronoTrack Maven Wrapper Launcher
@REM Uses installed IntelliJ Apache Maven 3.9.9 or system mvn
@REM ====================================================================
@echo off
setlocal

set "IDEA_MAVEN=E:\jetbrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd"

if exist "%IDEA_MAVEN%" (
    call "%IDEA_MAVEN%" %*
) else (
    call mvn %*
)

exit /b %ERRORLEVEL%
