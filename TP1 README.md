# Mini ESB (Enterprise Service Bus)

Un ESB minimaliste développé en Java 21 utilisant RabbitMQ comme bus de messages pivot.
## Développeur
- Khalil MZOUGHI
## Sommaire

1. [Développeur](#développeur)
2. [Architecture](#architecture)
3. [Structure du projet](#structure-du-projet)
4. [Installation et Exécution](#installation-et-exécution)
   - [Prérequis](#prérequis)
   - [Compilation](#compilation)
5. [Test du projet](#test-du-projet)
   - [Option 1: Test automatisé (recommandé)](#option-1-test-automatisé-recommandé)
   - [Option 2: Test manuel étape par étape](#option-2-test-manuel-étape-par-étape)
     - [Étape 1: Démarrage de RabbitMQ](#étape-1-démarrage-de-rabbitmq)
     - [Étape 2: Compilation du projet](#étape-2-compilation-du-projet)
     - [Étape 3: Test des endpoints](#étape-3-test-des-endpoints)
     - [Étape 4: Monitoring](#étape-4-monitoring)
     - [Étape 5: Nettoyage](#étape-5-nettoyage)
6. [Configuration](#configuration)
   - [Format YAML](#format-yaml)
   - [Types d'endpoints](#types-dendpoints)
   - [Paramètres par type](#paramètres-par-type)
     - [File endpoints](#file-endpoints)
     - [Socket endpoints](#socket-endpoints)

## Architecture
Ce mini-ESB implémente les composants suivants :
- **Bus MOM** : RabbitMQ pour le transport des messages
- **Endpoints entrants** : FileSystem, Socket TCP
- **Endpoints sortants** : FileSystem, Socket TCP  
- **Moteur de transformation** : JavaScript avec GraalVM
- **Service de management** : CLI pour orchestrer les canaux

## Structure du projet

```
src/main/java/fr/insa/esb/
├── core/                 # Interfaces et classes de base
├── bus/                  # Implémentation RabbitMQ
├── endpoints/            # Endpoints file et socket  
├── processors/           # Moteur de transformation JavaScript
├── config/               # Configuration et chargement
├── management/           # Gestionnaire de canaux
└── ManagementService.java # Service principal

config/                   # Fichiers de configuration
scripts/                  # Scripts JavaScript
docker-compose.yml        # Configuration Docker
```
## Installation et Exécution

### Prérequis
- Java 21+
- Maven 3.8+
- Docker (optionnel pour RabbitMQ)

### Compilation
```bash
mvn clean compile
mvn package
```

## Test  du projet

### Option 1: Test automatisé (recommandé)
```bash
# Exécution du script de test complet
# J'ai généré ce script via une IA 
# afin de vous faciliter le test de ma solution
./src/main/java/fr/insa/esb/test-esb.sh

# Nettoyage après tests
./src/main/java/fr/insa/esb/test-esb.sh clean
```

### Option 2: Test manuel étape par étape

#### Étape 1: Démarrage de RabbitMQ
```bash
# Démarrage avec Docker Compose
docker compose -f src/main/java/fr/insa/esb/docker-compose.yml up -d

# Vérification
docker compose -f src/main/java/fr/insa/esb/docker-compose.yml ps
```
Une fois RabbitMQ démarré, l'interface de management est accessible à :
- URL : http://localhost:15672
- Utilisateur : admin
- Mot de passe : admin123


#### Étape 2: Compilation du projet
```bash
# Compilation avec Maven
mvn clean compile package

# Vérification du JAR généré
ls -la target/mini-esb-1.0-SNAPSHOT.jar
```

#### Étape 3: Test des endpoints

**Test File → File :**
```bash
# 1. Création dossiers
mkdir -p test_input test_output

# 2. Création fichiers de test
echo "Contenu test 1" > test_input/test1.txt
echo "name,age\nJohn,25\nJane,30" > test_input/data.csv

# 3. Démarrage ESB
mvn exec:java -Dexec.mainClass="fr.insa.esb.ManagementService" -Dexec.args="config/channel.yml"

# 4. Vérification résultats (dans autre terminal)
ls -la test_output/
cat test_output/*
```

**Test Socket → File :**
```bash
# 1. Configuration socket-to-file
cp config/socket-to-file.yml config/test-socket.yml

# 2. Démarrage ESB  
mvn exec:java -Dexec.mainClass="fr.insa.esb.ManagementService" -Dexec.args="config/test-socket.yml"

# 3. Envoi données (dans autre terminal)
echo "Message socket test" | nc localhost 8080

# 4. Vérification
ls -la test_socket_output/
```

**Test File → Socket :**
```bash
# 1. Démarrage serveur réception
nc -l 9000 > received_data.txt &

# 2. Démarrage ESB avec config file-to-socket
mvn exec:java -Dexec.mainClass="fr.insa.esb.ManagementService" -Dexec.args="config/channel.yml"

# 3. Ajout fichier
echo "Test file to socket" > test_input/new_file.txt

# 4. Vérification réception
cat received_data.txt
```

#### Étape 4: Monitoring

**Interface RabbitMQ :**
- URL : http://localhost:15672
- Login : admin / admin123

**Dashboard ESB :**
Le dashboard s'affiche automatiquement dans la console toutes les 30s.

#### Étape 5: Nettoyage
```bash
# Arrêt des services
docker compose -f src/main/java/fr/insa/esb/docker-compose.yml down

# Nettoyage fichiers de test
rm -rf test_input test_output test_socket_output received_data.txt
```

## Configuration

### Format YAML
```yaml
channelName: "monChannel"
topic: "mon-topic"

source:
  type: "file-in"
  directory: "./input"
  extensions: [".txt", ".csv"]
  filterScript: "scripts/filter.js"
  transformScript: "scripts/transform.js"

destinations:
  - type: "socket-out"
    host: "127.0.0.1"
    port: 9000
  - type: "file-out"
    directory: "./output"
    keepOriginalName: true
```

### Types d'endpoints
- `file-in` : Lecture de fichiers
- `file-out` : Écriture de fichiers
- `socket-in` : Serveur TCP
- `socket-out` : Client TCP

### Paramètres par type

#### File endpoints
- `directory` : Chemin du dossier
- `extensions` : Liste des extensions autorisées
- `keepOriginalName` : Conserver le nom original (file-out)
- `deleteAfterProcessing` : Supprimer après traitement (file-in)

#### Socket endpoints
- `host` : Adresse IP/hostname
- `port` : Numéro de port
- `protocol` : Protocole (TCP par défaut)
- `keepConnectionAlive` : Connexion persistante (socket-out)