#!/bin/bash

set -e

echo "Stopping Gatekeeper Services"
docker-compose -f local-docker-compose.yml down
echo "Successfully Stopped Gatekeeper"