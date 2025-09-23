# ---- Stage 1: Build ----
FROM FROM docker.io/library/gradle:8.10.2-jdk17 AS builder
WORKDIR /app

# Copy build scripts first to leverage Gradle cache
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# Copy source and build fat jar
COPY . .
RUN ./gradlew shadowJar --no-daemon

# ---- Stage 2: Runtime ----
FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /home/

# Copy jar from build stage
COPY --from=builder /app/build/libs/keykeeper-1.0.jar /home/keykeeper.jar

# Create user and fix permissions as root
RUN useradd -ms /bin/bash appuser \
    && mkdir -p /home/keyKeeper \
    && chown -R appuser:appuser /home/keyKeeper

# Only switch user after permissions are fixed
USER appuser

# Expose internal port
EXPOSE 8080

# Volume for persistent data
VOLUME ["/home/keyKeeper"]

ENTRYPOINT ["java","-Duser.home=/home/","-jar","/home/keykeeper.jar"]
