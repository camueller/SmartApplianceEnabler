#!/bin/sh
REPO_TAG="avanux/smartapplianceenabler-amd64:ci"
CONTAINER_NAME="sae"
SAE_VOLUME="sae"
SAE_PORT=80

sudo docker pull $REPO_TAG
sudo docker stop $CONTAINER_NAME
sudo docker volume rm -f $SAE_VOLUME
sudo docker volume create $SAE_VOLUME
sudo docker run -d --rm -v $SAE_VOLUME:/opt/sae/data -p$SAE_PORT:8080 --name=$CONTAINER_NAME $REPO_TAG
