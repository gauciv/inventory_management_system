# Use Eclipse Temurin JDK 24 as base image
FROM eclipse-temurin:24-jdk

# Set the working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY pom.xml pom.xml
COPY .mvn .mvn

# Copy source code and resources
COPY src src
COPY lib lib

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the project (skip tests for faster build, adjust if needed)
RUN ./mvnw clean package -DskipTests

# Find the built JAR (assuming default Maven target dir and only one jar)
RUN cp target/*.jar app.jar

# Expose JavaFX ports if needed (not strictly necessary for desktop apps)
# EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
