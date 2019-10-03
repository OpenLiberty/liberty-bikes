#!/bin/bash

echo "Starting postgresql database"
docker stop lb-postgresql 2> /dev/null
docker run \
  --name lb-postgresql \
  --rm \
  -d \
  -p 5432:5432 \
  -e POSTGRES_DB=playerdb \
  -e POSTGRES_USER=lb_user \
  -e POSTGRES_PASSWORD=lb_password \
  postgres:11-alpine
  
echo "########################################################"  
echo "PostgreSQL database for player-service has been started"
echo "To test Liberty's connection to PostgreSQL, ping the URL:"
echo "  https://localhost:8444/ibm/api/validation/dataSource/DefaultDataSource"
echo "Using username/password credentials of admin/admin" 
echo "########################################################"
