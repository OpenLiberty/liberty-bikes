#!/bin/sh
# This script installs the required features when the server is deployed as a Cloud Foundry application
export PATH=$PATH:$HOME/.java/jre/bin
echo "Path is $PATH"
java -version

echo "Installing required features for server..."
$HOME/.liberty/bin/installUtility install $HOME/wlp/usr/servers/defaultServer/server.xml --acceptLicense
