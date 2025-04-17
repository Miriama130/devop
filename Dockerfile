FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/Foyer-0.0.1.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
