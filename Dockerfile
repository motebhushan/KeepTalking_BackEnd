# Use a Maven image to build the application with Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build-stage
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY ./src ./src
RUN mvn clean install -DskipTests

# Use a lightweight Java 21 runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build-stage /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
