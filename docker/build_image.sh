#!/bin/bash

usage() {
  echo "usage: $0 <repository> <version> <directory>"
  exit 1
}

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
    usage
fi
NAME="$1"
VERSION="$2"
DIRECTORY="$3"

echo "Build image $NAME:$VERSION"
docker image rmi -f $(docker images -qa $NAME | uniq) 2> /dev/null
docker build --tag=$NAME:$VERSION $DIRECTORY
docker tag $NAME:$VERSION $NAME:latest
docker push $NAME
