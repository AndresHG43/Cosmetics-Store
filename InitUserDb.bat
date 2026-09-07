@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Load variables from .env
for /f "usebackq tokens=1,2 delims==" %%A in (".env") do (
    set "var=%%A"
    set "val=%%B"
    for /f "tokens=* delims= " %%x in ("!val!") do set "!var!=%%~x"
)

REM Create temporary SQL file
set "tmpfile=%cd%\grant_permissions.sql"
(
    echo CREATE USER "!POSTGRES_READ_USER!" WITH PASSWORD '!POSTGRES_READ_PASSWORD!';
    echo GRANT CONNECT ON DATABASE "!POSTGRES_DB!" TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE ON SCHEMA public TO "!POSTGRES_READ_USER!";
    echo GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO "!POSTGRES_READ_USER!";
    echo ALTER DEFAULT PRIVILEGES IN SCHEMA public
    echo GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "!POSTGRES_READ_USER!";

    echo GRANT USAGE, SELECT ON SEQUENCE users_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE product_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE sale_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE entry_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE sale_detail_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE entry_detail_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE roles_sequence TO "!POSTGRES_READ_USER!";
    echo GRANT USAGE, SELECT ON SEQUENCE users_roles_sequence TO "!POSTGRES_READ_USER!";

    echo ALTER DEFAULT PRIVILEGES IN SCHEMA public
    echo GRANT USAGE, SELECT ON SEQUENCES TO "!POSTGRES_READ_USER!";
) > "!tmpfile!"

REM Run the SQL file in Docker
echo Executing init userDB script
docker exec -i postgre-container psql -v ON_ERROR_STOP=1 -U !POSTGRES_USER! -d !POSTGRES_DB! < "!tmpfile!"

REM Delete temporary file
del "!tmpfile!"

endlocal
pause
exit
