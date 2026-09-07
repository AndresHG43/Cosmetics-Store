@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Read variables from the .env file
for /f "usebackq tokens=1,2 delims==" %%A in (".env") do (
    set "var=%%A"
    set "val=%%B"
    REM Remove possible quotes and spaces
    for /f "tokens=* delims= " %%x in ("!val!") do set "!var!=%%~x"
)

set "data_file=%cd%\sql\data.sql"

echo Executing init sql for data
docker exec -i postgre-container psql -U !POSTGRES_USER! -w !POSTGRES_DB! < "%data_file%"

endlocal
pause
exit
