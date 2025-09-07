FROM maven:3.8.4-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create a runtime image
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port (adjust if needed)
EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/app.jar"]