FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=builder /app/target/eventx-backend-1.0.0.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=12 \
  CMD wget -qO- "http://127.0.0.1:${PORT:-8080}/api/health" >/dev/null || exit 1
# JAVA_OPTS is where the container memory limit is communicated to the JVM. A
# free instance has 512 MB, and the default heap sizing does not know that.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
