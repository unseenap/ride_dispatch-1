# Build stage: compile the Spring Boot jar. Tests need no database (they run
# on in-memory H2) but are skipped here to keep image builds fast; run
# `mvn test` separately in CI.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Warm the dependency cache in its own layer so source-only changes don't
# re-download the world.
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Runtime stage: JRE only, non-root user.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/target/dispatchhub-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
