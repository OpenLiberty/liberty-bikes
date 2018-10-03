#!/bin/sh
# This script installs the required features when the server is deployed as a Cloud Foundry application
export PATH=$PATH:$HOME/.java/jre/bin
echo "Path is $PATH"
java -version

export WLP_USER_DIR=$HOME/wlp/usr
echo "WLP_USER_DIR is $WLP_USER_DIR"

echo "Installing required features for server..."
$HOME/.liberty/bin/installUtility install $HOME/wlp/usr/servers/defaultServer/server.xml --acceptLicense
