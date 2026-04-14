FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built jar file from the build directory
COPY build/logBuddyProcessing-exec.jar logBuddyProcessing-exec.jar

# Expose the port your app listens on (default Spring Boot is 8080)
EXPOSE 6969

# Run the application
ENTRYPOINT ["java", "-jar", "logBuddy-exec.jar"]

