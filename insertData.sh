#!/bin/bash

set -o allexport
source .env
set +o allexport

data_file="$(pwd)/sql/data.sql"

echo "Executing init sql for data"
docker exec -i postgre-container sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < "$data_file"
