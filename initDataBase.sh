#!/bin/bash

set -o allexport
source .env
set +o allexport

structure_file="$(pwd)/sql/structure.sql"
triggers_file="$(pwd)/sql/triggers.sql"

echo "Creating unaccent extension"
docker exec -i postgre-container sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE EXTENSION IF NOT EXISTS unaccent;"'

echo "Executing init sql for structure"
docker exec -i postgre-container sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < "$structure_file"

echo "Executing init sql for triggers"
docker exec -i postgre-container sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < "$triggers_file"
