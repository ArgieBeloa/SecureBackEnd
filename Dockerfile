# Stage 1: Build the project using Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy the project files (including src/, pom.xml)
COPY src .

# Build the application JAR (skip tests if desired)
RUN mvn clean package -DskipTests

# Stage 2: Run the app using a lightweight image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/ThesisBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]