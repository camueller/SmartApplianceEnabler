#!/bin/sh
REPO_TAG="avanux/smartapplianceenabler-amd64:ci"

sudo docker system prune -f
sudo docker pull $REPO_TAG
