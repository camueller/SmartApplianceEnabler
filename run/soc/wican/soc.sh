#!/bin/bash
MQTT_SERVER=192.168.1.1
SUB="mosquitto_sub -h $MQTT_SERVER -t wican/48ca4330cd5d/automate -C 1"
SOC_MESSAGE_FILE=/tmp/soc-message.txt

$SUB > $SOC_MESSAGE_FILE
SOC=`cat $SOC_MESSAGE_FILE | jq .SOC`
if [ -n "$SOC" ] && [ "$SOC" != "null" ] ; then
  SOC_FIXED=$SOC
else
  SOC_FIXED=0
fi
echo ${SOC_FIXED%.*}
