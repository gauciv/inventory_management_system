# Use Maven with JDK 24 as base image
FROM maven:3.9-eclipse-temurin-24 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml and source
COPY pom.xml .
COPY src src
COPY lib lib

# Build the project (skip tests for faster build)
RUN mvn clean package -DskipTests

# Find the built JAR (assuming default Maven target dir and only one jar)
RUN cp target/*.jar app.jar

# Expose JavaFX ports if needed (not strictly necessary for desktop apps)
# EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
