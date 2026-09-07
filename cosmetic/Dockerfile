FROM amazoncorretto:25-alpine-jdk

WORKDIR /app

COPY cosmetic-1.0.0.jar ./application.jar

EXPOSE 8080

CMD ["java", "-Duser.timezone=America/El_Salvador", "-Djava.io.tmpdir=/app/data", "-jar", "application.jar"]
