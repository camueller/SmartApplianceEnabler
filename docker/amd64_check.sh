#!/bin/sh
NAME="avanux/smartapplianceenabler-amd64"
TAGS_FILE=amd64.tags
SAE_VERSION=`grep 'ENV SAE_VERSION' sae-amd64/Dockerfile | awk -F '=' '{print $2}'`
echo "Version from Dockerfile: $SAE_VERSION"

curl -o $TAGS_FILE https://hub.docker.com/v2/repositories/$NAME/tags

if grep -q -v "name\":\"$SAE_VERSION" $TAGS_FILE; then
  echo "No image for version $SAE_VERSION found on dockerhub."
  cp ../target/SmartApplianceEnabler-$SAE_VERSION.war sae-amd64/
  ./build_image.sh $NAME $SAE_VERSION ./sae-amd64
fi
