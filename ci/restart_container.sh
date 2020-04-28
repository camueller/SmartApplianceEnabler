#!/bin/sh
. ./settings.conf

sudo docker stop $CONTAINER_NAME
sudo docker volume rm -f $SAE_VOLUME
sudo docker volume create $SAE_VOLUME
sudo docker run -d --rm -v $SAE_VOLUME:/opt/sae/data -p$SAE_PORT:8080 --name=$CONTAINER_NAME $REPO_TAG
