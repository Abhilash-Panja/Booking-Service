# ---- Build stage ----
# Needs access to your local Maven cache (~/.m2) because this service
# depends on com.rideflow:Rideflow-EntityService via mavenLocal().
# Requires: DOCKER_BUILDKIT=1 and Compose's `additional_contexts` (see docker-compose.yml)
FROM gradle:8.10-jdk17 AS build
WORKDIR /app
COPY . .
RUN --mount=type=bind,from=m2cache,target=/root/.m2 \
    gradle bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
