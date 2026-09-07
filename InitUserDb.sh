#!/bin/bash

# -- Allow connection to the database

# -- Allow schema use (required to access tables)

# -- Allow SELECT, INSERT, UPDATE, and DELETE permissions on all existing tables

# -- Make future tables inherit these permissions as well

# -- Allow USAGE and SELECT permissions on all existing sequences


# Exit if error
set -e

# Load variables from the .env file
set -o allexport
source .env
set +o allexport

# Execute SQL commands inside the Docker container
echo "Executing init userDB script"
docker exec -i postgre-container psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOSQL
CREATE USER "$POSTGRES_READ_USER" WITH PASSWORD '$POSTGRES_READ_PASSWORD';

GRANT CONNECT ON DATABASE "$POSTGRES_DB" TO "$POSTGRES_READ_USER";

GRANT USAGE ON SCHEMA public TO "$POSTGRES_READ_USER";

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO "$POSTGRES_READ_USER";

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "$POSTGRES_READ_USER";

GRANT USAGE, SELECT ON SEQUENCE users_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE product_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE sale_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE entry_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE sale_detail_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE entry_detail_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE roles_sequence TO "$POSTGRES_READ_USER";
GRANT USAGE, SELECT ON SEQUENCE users_roles_sequence TO "$POSTGRES_READ_USER";

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO "$POSTGRES_READ_USER";
EOSQL
