# ==========================
# Stage 1: Build the project using Maven
# ==========================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application JAR (skip tests if desired)
RUN mvn clean package -DskipTests


# ==========================
# Stage 2: Run the app using a lightweight JRE
# ==========================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
