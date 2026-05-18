@echo off
cd /d "%~dp0"
echo =======================================================
echo Launching BudgetFit (JavaFX Edition) via Maven Wrapper...
echo =======================================================

call mvnw.cmd compile javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo =======================================================
    echo Launch Failed. Please check the Maven error messages above.
    echo =======================================================
    pause
)
