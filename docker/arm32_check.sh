#!/bin/sh
IMAGE_RELEASE_FILE=image.release
RELEASES_FILE=/tmp/releases
curl -o $RELEASES_FILE https://api.github.com/repos/camueller/SmartApplianceEnabler/releases
LATEST_RELEASE=`grep tag_name $RELEASES_FILE | awk -F ':' '{print $2}' | awk -F '"' '{print $2}'i | head -n 1`
IMAGE_RELEASE=`cat $IMAGE_RELEASE_FILE`
if [ "$LATEST_RELEASE" != "$IMAGE_RELEASE" ]; then
  echo "New release detected: $LATEST_RELEASE"
  ./build_image.sh avanux/smartapplianceenabler-arm32 $LATEST_RELEASE ./sae-arm32
  echo $LATEST_RELEASE > $IMAGE_RELEASE_FILE
else
  echo "Release not new: $LATEST_RELEASE"
fi

