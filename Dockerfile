# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first so dependency downloads are cached between builds
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user (good DevSecOps practice, and Trivy/Sonar will notice)
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/trainerstacks-portfolio.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
