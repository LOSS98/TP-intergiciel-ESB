# Dockerfile Multi-Stage – result-frontend
# Compatible OCI pour déploiement Kubernetes

# ─── Étape 1 : Build Maven ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ─── Étape 2 : Runtime JRE 21 ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S labotrack && adduser -S labotrack -G labotrack
USER labotrack

COPY --from=build /app/target/result-frontend-1.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
