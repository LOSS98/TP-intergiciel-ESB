FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/mini-esb-1.0-SNAPSHOT.jar app.jar
COPY config/ config/
COPY scripts/ scripts/

EXPOSE 8080

CMD ["java", "-jar", "app.jar", "config/channel.yml"]