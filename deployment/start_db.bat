@echo off
echo Starting MySQL Server...
echo Please wait while the database initializes...

REM Check if MySQL is already running
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo MySQL is already running.
    goto end
)

REM Start MySQL
start /B "MySQL" "mysql\bin\mysqld.exe" --defaults-file="mysql\my.ini" --console

echo Waiting for MySQL to start...
timeout /t 10 /nobreak >nul

REM Test MySQL connection
mysql\bin\mysqladmin -u root -pcomputerengineering ping >nul 2>&1
if errorlevel 1 (
    echo Failed to start MySQL. Please check the error messages above.
) else (
    echo MySQL Server started successfully!
    echo You can now start the application.
)

:end
pause
