@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Read variables from the .env file
for /f "usebackq tokens=1,2 delims==" %%A in (".env") do (
    set "var=%%A"
    set "val=%%B"
    REM Remove possible quotes and spaces
    for /f "tokens=* delims= " %%x in ("!val!") do set "!var!=%%~x"
)

set "structure_file=%cd%\sql\structure.sql"
set "triggers_file=%cd%\sql\triggers.sql"

echo Creating unaccent extension
docker exec -i postgre-container psql -U !POSTGRES_USER! -w !POSTGRES_DB! -c "CREATE EXTENSION IF NOT EXISTS unaccent;"

echo Executing init sql for structure
docker exec -i postgre-container psql -U !POSTGRES_USER! -w !POSTGRES_DB! < "%structure_file%"

echo Executing init sql for triggers
docker exec -i postgre-container psql -U !POSTGRES_USER! -w !POSTGRES_DB! < "%triggers_file%"

endlocal
pause
exit
