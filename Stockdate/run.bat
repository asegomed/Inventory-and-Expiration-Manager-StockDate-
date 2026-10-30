@echo off
setlocal
cd /d "%~dp0"
if errorlevel 1 exit /b %errorlevel%

set "STOCKDATE_JDK="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "STOCKDATE_JDK=%JAVA_HOME%"

if not defined STOCKDATE_JDK if defined USERPROFILE (
    for /d %%J in ("%USERPROFILE%\.jdks\*") do (
        if not defined STOCKDATE_JDK if exist "%%~fJ\bin\javac.exe" set "STOCKDATE_JDK=%%~fJ"
    )
)

if not defined STOCKDATE_JDK if defined ProgramFiles (
    for %%R in ("%ProgramFiles%\Eclipse Adoptium" "%ProgramFiles%\Java" "%ProgramFiles%\Microsoft") do (
        if exist "%%~R" (
            for /d %%J in ("%%~R\jdk*" "%%~R\openjdk*") do (
                if not defined STOCKDATE_JDK if exist "%%~fJ\bin\javac.exe" set "STOCKDATE_JDK=%%~fJ"
            )
        )
    )
)

if defined STOCKDATE_JDK (
    set "JAVA_HOME=%STOCKDATE_JDK%"
    set "PATH=%STOCKDATE_JDK%\bin;%PATH%"
) else (
    where java >nul 2>nul
    if errorlevel 1 (
        echo No se encontro un JDK instalado. Configura JAVA_HOME o instala un JDK para ejecutar StockDate.
        exit /b 1
    )
)

call gradlew.bat --console=plain bootRun %*
exit /b %errorlevel%
