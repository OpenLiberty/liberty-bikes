#!/bin/bash

LOCAL_HOST=`ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1'`
echo "This hostname is: $LOCAL_HOST"

PROM_LOCAL_CONFIG=`pwd`/build/monitoring/prometheus-local
mkdir -p $PROM_LOCAL_CONFIG
rm $PROM_LOCAL_CONFIG/prometheus.yml 2> /dev/null
echo "Building local config file prometheus config at: $PROM_LOCAL_CONFIG"
sed \
  -e "s/'game:/'$LOCAL_HOST:/g" \
  -e "s/'player:/'$LOCAL_HOST:/g" \
  -e "s/'auth:/'$LOCAL_HOST:/g" \
  -e "s/'frontend:/'$LOCAL_HOST:/g" \
  monitoring/prometheus/prometheus.yml > \
  $PROM_LOCAL_CONFIG/prometheus.yml

echo "Starting prometheus"
docker stop lb-prometheus 2> /dev/null
docker run \
  --name lb-prometheus \
  --rm \
  -d \
  -p 9090:9090 \
  -v $PROM_LOCAL_CONFIG:/etc/prometheus \
  prom/prometheus:v2.4.0
  
echo "Starting grafana"
docker stop lb-grafana 2> /dev/null
docker run \
  --name lb-grafana \
  --rm \
  -d \
  -p 3000:3000 \
  -e  GF_INSTALL_PLUGINS=flant-statusmap-panel \
  -v `pwd`/monitoring/datasource-local:/etc/grafana/provisioning/datasources \
  -v `pwd`/monitoring/dashboardList:/etc/grafana/provisioning/dashboards \
  -v `pwd`/monitoring/grafanaDashboardConfig:/var/lib/grafana/dashboards \
  grafana/grafana:5.2.4

echo "########################################################"  
echo "Metrics dashboard available at http://localhost:3000"
echo "Log in with user=admin password=admin"
echo "########################################################"
