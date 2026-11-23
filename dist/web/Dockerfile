# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

# libpq for the govapi binary, curl for the healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends libpq5 curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system app && useradd --system --gid app app
RUN mkdir -p /workdir/app/govapi /workdir/instance
WORKDIR /workdir

COPY --from=build /build/target/keyscout24.jar /workdir/app.jar
COPY src/main/resources/govapi/binary /workdir/app/govapi/binary
RUN chmod +x /workdir/app/govapi/binary && chown -R app:app /workdir/instance

EXPOSE 10088

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fsS "http://localhost:10088/healthz" || exit 1

USER app

CMD ["java", "-jar", "/workdir/app.jar"]
