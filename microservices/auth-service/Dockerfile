# ═══ STAGE 1 — BUILD ═══
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# copier le pom.xml en premier
# pour profiter du cache Docker (dépendances)
COPY pom.xml .

# télécharger les dépendances sans le code source
# si pom.xml ne change pas → cette couche est cachée
RUN mvn dependency:go-offline -B

# copier le code source
COPY src ./src

# builder le jar sans les tests
# les tests sont faits séparément
RUN mvn clean package -DskipTests -B

# ═══ STAGE 2 — RUNTIME ═══
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# copier uniquement le jar depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

# exposer le port
EXPOSE 8081

# lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]