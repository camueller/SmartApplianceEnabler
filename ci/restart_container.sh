#!/bin/sh
REPO_TAG="avanux/smartapplianceenabler-amd64:ci"
SAE_CONTAINER_NAME="sae"
SAE_VOLUME="sae"
SAE_PORT=80
MOSQUITTO_CONTAINER_NAME="mosquitto"

sudo docker stop $MOSQUITTO_CONTAINER_NAME
sudo docker stop $SAE_CONTAINER_NAME
sudo docker volume rm -f $SAE_VOLUME
sudo docker volume create $SAE_VOLUME
sudo docker run -d --rm -p 1883:1883 --name =$MOSQUITTO_CONTAINER_NAME eclipse-mosquitto mosquitto -c /mosquitto-no-auth.conf
sudo docker run -d --rm -v $SAE_VOLUME:/opt/sae/data -p$SAE_PORT:8080 --name=$SAE_CONTAINER_NAME $REPO_TAG
