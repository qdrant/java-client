# Use OpenJDK 11 as base image
FROM eclipse-temurin:11-jdk AS build

# Set working directory
WORKDIR /app

# Install Gradle
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.4-bin.zip && \
    unzip gradle-8.4-bin.zip && \
    rm gradle-8.4-bin.zip && \
    mv gradle-8.4 /opt/gradle && \
    ln -s /opt/gradle/bin/gradle /usr/local/bin/gradle && \
    apt-get remove -y wget unzip && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy source code and build files
COPY . .

# Build the application fat jar
RUN gradle --no-daemon clean shadowJar

# Create a runtime image
FROM eclipse-temurin:11-jre

WORKDIR /app

# Copy the built JAR from the build stage (versioned shaded jar)
COPY --from=build /app/build/libs/app-*-all.jar /app/app.jar

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
