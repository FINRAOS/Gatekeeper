#!/bin/bash

set -e

if [[ -z "${http_proxy}" ]]; then
    echo "No Proxy provided, setting http_proxy to be blank"
    export http_proxy=""
fi

if [[ -z "${AWS_DIRECTORY}" ]]; then
    echo "Please set AWS_DIRECTORY environment before proceeding"
    exit 1;
fi

chmod 600 certs/*
pushd containers
echo "Building base gatekeeper containers"
docker-compose build
popd
pushd demo-services/fake-account-service
echo "Building gatekeeper demo services"
mvn clean install
popd
pushd services
echo "Building gatekeeper ec2 + rds services"
mvn clean install
popd
pushd ui
echo "Building gatekeeper ui"
npm install
npm run build
popd
echo "Building gatekeeper containers"
docker-compose -f local-docker-compose.yml build
echo "Starting gatekeeper local environment"
docker-compose -f local-docker-compose.yml up -d
echo "Gatekeeper local environment is running, to stop it simply run stop.sh"
echo "https://localhost:443 => Regular User"
echo "https://localhost:444 => Ops/Support User"
echo "https://localhost:445 => Approver"
