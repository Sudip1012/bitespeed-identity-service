# ─────────────────────────────────────────────────────────────
# 1️⃣  BUILD STAGE ─ use Maven image to compile the project
# ─────────────────────────────────────────────────────────────
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package -DskipTests

# ─────────────────────────────────────────────────────────────
# 2️⃣  RUNTIME STAGE ─ slim JRE image to run the jar
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the fat jar from the build stage
COPY --from=build /app/target/identity-reconciliation-0.0.1-SNAPSHOT.jar app.jar

# Render (and most PaaS) inject a PORT env var; fall back to 8083 locally
ENV SERVER_PORT=${PORT:-8083}

# Expose for local runs (Render ignores EXPOSE but it's handy for you)
EXPOSE 8083

# Run the application, forcing server.port to the value above
ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${SERVER_PORT}"]
