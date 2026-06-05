@echo off
rem ----------------------------------------------------------------------
rem Gradle start up script for Windows
rem ----------------------------------------------------------------------

set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set GRADLE_HOME=%DIRNAME%\gradle\wrapper

"%GRADLE_HOME%\gradle-8.4\bin\gradle.bat" %*
