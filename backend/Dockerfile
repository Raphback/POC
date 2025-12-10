# Backend Dockerfile - build multi-stage optimisé
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Pré-chauffer le cache Maven (BuildKit recommandé)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests dependency:go-offline

# Copier le code et construire
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests clean package

# Runtime léger
FROM eclipse-temurin:17-jre-jammy

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=docker

WORKDIR /app

# Dépendance minimale pour healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Utilisateur non-root
RUN groupadd -r app && useradd -r -g app app \
    && mkdir -p /app/data \
    && chown -R app:app /app

COPY --from=builder /app/target/*.jar /app/app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

