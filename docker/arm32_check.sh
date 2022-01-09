#!/bin/sh
NAME="avanux/smartapplianceenabler-arm32"
TAGS_FILE=arm32.tags
SAE_VERSION=`grep 'ENV SAE_VERSION' sae-arm32/Dockerfile | awk -F '=' '{print $2}'`
echo "Version from Dockerfile: $SAE_VERSION"

curl -o $TAGS_FILE https://hub.docker.com/v2/repositories/$NAME/tags >/dev/null 2>&1

if grep -q -v "name\":\"$SAE_VERSION" $TAGS_FILE; then
  echo "No image for version $SAE_VERSION found on dockerhub."
  ./build_image.sh avanux/smartapplianceenabler-arm32 $SAE_VERSION ./sae-arm32
fi
