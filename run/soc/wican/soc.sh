#!/bin/bash
MQTT_SERVER=192.168.1.1
SUB="mosquitto_sub -h $MQTT_SERVER -t wican/5432048f421d/soc -C 1"
SOC_MESSAGE_FILE=/tmp/soc-message.txt

$SUB > $SOC_MESSAGE_FILE
SOC=`cat $SOC_MESSAGE_FILE | jq .soc`
echo $SOC
