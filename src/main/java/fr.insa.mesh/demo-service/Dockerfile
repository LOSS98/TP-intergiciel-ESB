# Dockerfile Cas 1 : exécution d'un fat-jar pré-compilé
# Pré-requis : mvn clean package doit avoir été exécuté au préalable
# Le fat-jar (avec toutes les dépendances) doit être dans target/

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copie du fat-jar dans le conteneur
COPY target/demo-service-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
