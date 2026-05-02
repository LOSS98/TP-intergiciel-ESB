# Dockerfile ESB – doit être buildé depuis la RACINE du projet :
#   docker build -f src/main/java/fr/insa/esb/Dockerfile -t mini-esb:latest .
#
# Le contexte de build est la racine du projet (.), ce qui rend les chemins
# ci-dessous relatifs à la racine (config/, scripts/, target/).

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# fat-jar compilé par mvn package
COPY target/mini-esb-1.0-SNAPSHOT.jar app.jar

# Fichiers de configuration et scripts JS (relatifs à la racine du projet)
COPY config ./config
COPY scripts ./scripts

EXPOSE 8080

CMD ["java", "-jar", "app.jar", "config/channel.yml"]
