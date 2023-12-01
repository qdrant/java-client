#!/bin/bash

set -ex

function stop_docker()
{
  echo "Stopping qdrant_test"
  docker stop qdrant_test
}

# Ensure current path is project root
cd "$(dirname "$0")/../"

QDRANT_VERSION='v1.6.1'

QDRANT_HOST='localhost:6333'

docker run -d --rm \
           -p 6333:6333 \
           -p 6334:6334 \
           --name qdrant_test qdrant/qdrant:${QDRANT_VERSION}

trap stop_docker SIGINT
trap stop_docker ERR

until curl --output /dev/null --silent --get --fail http://$QDRANT_HOST/collections; do
  printf 'Waiting for the Qdrant server to start...'
  sleep 5
done

mvn test

stop_docker