#!/bin/bash

usage() {
  echo "usage: $0 <version>"
  exit 1
}

if [ -z "$1" ]; then
    usage
fi
VERSION="$1"
NAME="avanux/smartapplianceenabler-arm32"

echo "Build image $NAME:$VERSION"
docker image rmi $(docker images -qa $NAME) 2> /dev/null
docker build --tag=$NAME:$VERSION ./sae-arm32
docker tag $NAME:$VERSION $NAME:latest
docker push $NAME
