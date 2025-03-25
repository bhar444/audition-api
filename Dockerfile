# Stage 1: Build Stage
FROM openjdk:17-jdk-slim AS build

# Install necessary tools (git and dependencies for Gradle)
RUN apt-get update && apt-get install -y \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory for the build
WORKDIR /app

# Clone the repository
RUN git clone https://github.com/bhar444/audition-api.git .

# Install Gradle
ARG GRADLE_VERSION=8.10.2
RUN curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip \
    && unzip gradle.zip \
    && rm gradle.zip \
    && mv gradle-${GRADLE_VERSION} /opt/gradle

# Add Gradle to PATH
ENV PATH="/opt/gradle/bin:${PATH}"

# Build the application using Gradle
RUN gradle clean build -x test

# Stage 2: Runtime Stage
FROM openjdk:17-jdk-slim AS runtime

# Set the working directory in the runtime container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/audition-api-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your application runs on
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]