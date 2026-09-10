# Production Runtime Container
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Create a non-root system user for security compliance
RUN groupadd -r appgroup && useradd -r -g appgroup -u 1001 appuser

# Copy the pre-built application JAR
COPY target/nnp-config-*.jar app.jar

USER appuser

# Expose HTTP port
EXPOSE 8888

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
