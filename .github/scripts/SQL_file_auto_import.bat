@echo off
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%..\..\"
set DB_USER=root
set DB_NAME=inventory_management_system_database
set SQL_FILE=src\main\resources\database\inventory_management_system_database.sql
echo Importing database from %SQL_FILE%...
if not exist "%SQL_FILE%" (
    echo ERROR: SQL file not found at %SQL_FILE%
    pause
    exit /b
)
mysql -u %DB_USER% -p %DB_NAME% < "%SQL_FILE%"
echo Import completed.
pause