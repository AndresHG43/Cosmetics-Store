@echo off
setlocal

cd store || exit /b 1

echo Building and generating .jar file
call mvnw clean package || exit /b 1

REM Rename/copy the .jar to a constant name
for %%f in (target\*.jar) do (
    copy /Y "%%f" application.jar
)

echo Executing application
java -Duser.timezone=America/El_Salvador -jar application.jar

endlocal

pause
exit
