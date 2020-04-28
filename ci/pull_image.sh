#!/bin/sh
. ./settings.conf

sudo docker system prune -f
sudo docker pull $REPO_TAG
