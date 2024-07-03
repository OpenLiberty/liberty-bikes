#!/bin/bash

LOCAL_HOST=`ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1'`
echo "This hostname is: $LOCAL_HOST"

PROM_LOCAL_CONFIG=`pwd`/build/monitoring/prometheus-local
mkdir -p $PROM_LOCAL_CONFIG
rm $PROM_LOCAL_CONFIG/prometheus.yml 2> /dev/null
echo "Building local config file prometheus config at: $PROM_LOCAL_CONFIG"
sed \
  -e 's/'game:/'game.1ibd03sq8zc5.eu-gb.codeengine.appdomain.cloud/g' \
  -e 's/'player:/'player.1ibd03sq8zc5.eu-gb.codeengine.appdomain.cloud/g' \
  -e 's/'auth:/'127.0.0.1:/g' \
  -e 's/'frontend:/'frontend.1ibd03sq8zc5.eu-gb.codeengine.appdomain.cloud/g' \
  monitoring/prometheus/prometheus.yml > \
  $PROM_LOCAL_CONFIG/prometheus.yml

echo "Starting prometheus"
docker stop lb-prometheus 2> /dev/null
docker run \
  --name lb-prometheus \
  -d \
  -p 9088:9088 \
  -l PROMETHEUS_EXPORTER_PORT=9088 \
  -v $PROM_LOCAL_CONFIG:/etc/prometheus \
  prom/prometheus --config.file=/etc/prometheus/prometheus.yml --web.listen-address=:9088
  

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
  grafana/grafana:10.1.10

echo "########################################################"  
echo "Metrics dashboard available at http://localhost:3000"
echo "Log in with user=admin password=admin"
echo "########################################################"
