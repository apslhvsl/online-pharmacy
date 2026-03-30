#!/bin/bash
set -e

echo "Building all services with Maven..."
mvn clean package -DskipTests

echo "Starting Docker Compose..."
docker compose up --build -d

if [[ "$1" == "--seed" ]]; then
  echo "Waiting for postgres to be ready..."
  until docker compose exec postgres-db pg_isready -U "${POSTGRES_USER:-postgres}" > /dev/null 2>&1; do
    sleep 2
  done
  echo "Seeding databases..."
  docker compose exec postgres-db psql -U "${POSTGRES_USER:-postgres}" -d auth_db    -f /docker-entrypoint-initdb.d/seed_auth.sql
  docker compose exec postgres-db psql -U "${POSTGRES_USER:-postgres}" -d catalog_db -f /docker-entrypoint-initdb.d/seed_catalog.sql
  docker compose exec postgres-db psql -U "${POSTGRES_USER:-postgres}" -d order_db   -f /docker-entrypoint-initdb.d/seed_order.sql
  echo "Seeding done."
fi
