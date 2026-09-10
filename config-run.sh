#!/bin/bash
set -e

server_port="${SERVER_PORT:-8888}"
active_profiles="${SPRING_PROFILES_ACTIVE:-jdbc,local}"
db_user="${DB_USER:-postgres}"
db_password="${DB_PASSWORD:-postgres}"
db_host="${DB_HOST:-localhost}"
db_port="${DB_PORT:-5432}"
db_database="${DB_NAME:-nnp-core-comp}"
logging_level="${LOGGING_LEVEL:-INFO}"
quantum_safe_service_url="${QUANTUM_SAFE_SERVICE_URL:-http://localhost:8080}"
app_jar="${APP_JAR:-nnp-config-1.0.0.jar}"

COMMAND="java -Dserver.port=$server_port -Dprofiles=$active_profiles -Ddb.user=$db_user -Ddb.password=$db_password -Ddb.url=jdbc:postgresql://$db_host:$db_port/$db_database -Dlogging_level=$logging_level -Dquantum.safe.service.url=$quantum_safe_service_url -jar $app_jar"

exec $COMMAND
