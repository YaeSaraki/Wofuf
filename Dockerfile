# ======================== Stage 1: Build ========================
FROM gradle:9.2.1-jdk17 AS builder

WORKDIR /app

# Copy Gradle wrapper and project files first (layer caching)
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts build.gradle.kts ./

# Copy shared modules (dependencies of business modules)
COPY Wofuf-shared/ Wofuf-shared/
COPY Wofuf-modules/ Wofuf-modules/
COPY Wofuf-infra/ Wofuf-infra/

# Copy root source (monolithic application)
COPY src/ src/

# Build the monolithic fat JAR
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test && \
    find build/libs -name '*.jar' -not -name '*-plain.jar' -exec cp {} /app/app.jar \;

# ======================== Stage 2: Runtime ========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r wofuf && useradd -r -g wofuf -d /app -s /sbin/nologin wofuf

# Copy fat JAR from builder
COPY --from=builder /app/app.jar app.jar

# Create directory for uploads
RUN mkdir -p /app/uploads && chown -R wofuf:wofuf /app

USER wofuf

EXPOSE 8005

ENTRYPOINT ["java", "-jar", "app.jar"]
