# ── Stage 1: Frontend (Vue 3 + Vite) ────────────────────────
FROM node:20-alpine AS frontend
WORKDIR /build/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
# vite.config.js grava em ../src/main/resources/static
COPY src/main/resources/static /build/src/main/resources/static
RUN npm run build

# ── Stage 2: Backend (Spring Boot) ──────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
COPY --from=frontend /build/src/main/resources/static ./src/main/resources/static
RUN mvn package -DskipTests -q

# ── Stage 3: Runtime ────────────────────────────────────────
# Imagem multi-arquitetura: roda em Intel/AMD (KVM) e em ARM (Oracle Cloud grátis)
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system fiosmj && useradd --system --no-create-home -g fiosmj fiosmj \
 && mkdir -p /app/data/uploads && chown -R fiosmj:fiosmj /app/data
COPY --from=backend /app/target/*.jar app.jar
# Imagens iniciais (copiadas para o volume na primeira execução)
COPY --chown=fiosmj:fiosmj seed/uploads /app/seed/uploads
COPY --chown=fiosmj:fiosmj docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh
USER fiosmj

ENV DB_PATH=/app/data/fiosmj.db \
    UPLOADS_PATH=/app/data/uploads \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC"
VOLUME /app/data
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8081/api/health >/dev/null || exit 1
ENTRYPOINT ["/app/docker-entrypoint.sh"]
