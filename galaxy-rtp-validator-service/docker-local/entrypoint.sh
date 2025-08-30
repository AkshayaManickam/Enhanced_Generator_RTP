#!/bin/bash
set -e

# Define database connection parameters
DB_URL=jdbc:mysql://host.docker.internal:3309/galaxy_rtp_validator
DB_USERNAME=root
DB_PASSWORD=root

# Create the databases if they do not exist
mysql -h host.docker.internal -P 3309 -u root -proot <<EOF
CREATE DATABASE IF NOT EXISTS galaxy_rtp_validator_finzly;
CREATE DATABASE IF NOT EXISTS galaxy_rtp_validator_banka;
EOF

# Run Liquibase update command
liquibase --changeLogFile=/liquibase/rtp-validator-db-changelog.xml \
          --url=$DB_URL \
          --username=$DB_USERNAME \
          --password=$DB_PASSWORD \
          --classpath=/app/liquibase \
          --driver=com.mysql.cj.jdbc.Driver \
          update


# starts the application with remote debugging enabled
exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5012 -jar galaxy-rtp-validator.jar --spring.config.name=application-local