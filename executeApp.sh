#!/bin/bash
cd store || exit 1

# Compilar el proyecto
echo "Building and generating .jar file"
./mvnw clean package || exit 1

# Renombrar el JAR a un nombre fijo
cp target/*.jar application.jar

# Ejecutar el JAR
echo "Executing application"
java -Duser.timezone=America/El_Salvador -jar application.jar
