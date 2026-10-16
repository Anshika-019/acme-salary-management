# One image serves both the API and the UI: Spring Boot hosts the built Angular files,
# so the app runs as a single service on a single origin (no CORS, one deploy).

# 1. Build the Angular app
FROM node:24-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build

# 2. Build the Spring Boot jar with the UI bundled as static resources
FROM eclipse-temurin:25-jdk AS backend
WORKDIR /app/backend
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN sh mvnw -q -B dependency:go-offline
COPY backend/src src
COPY --from=frontend /app/frontend/dist/frontend/browser src/main/resources/static
# Tests need Docker (Testcontainers) and run in CI instead of inside this build.
RUN sh mvnw -q -B package -DskipTests

# 3. Slim runtime image, running as a non-root user
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --no-create-home app
COPY --from=backend /app/backend/target/*.jar app.jar
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
