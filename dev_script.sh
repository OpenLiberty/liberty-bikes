#!/bin/bash

echo "Checking all containers are down"
podman compose down
echo "Force deleting all required podman images"
podman rmi docker.io/library/libertybikes-player --force
podman rmi docker.io/library/libertybikes-game --force
podman rmi docker.io/library/libertybikes-frontend --force
podman rmi icr.io/appcafe/open-liberty --force
echo "Cleaning Gradle dirs"
./gradlew cleanDirs
echo "Re-assembling project"
./gradlew assemble
echo "Creating new WAR"
./gradlew war
echo "Running podman compose up"
podman compose up