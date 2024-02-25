#!/bin/bash
#
# Waits for a MQTT message indicating that the WiCAN OBD2 adapter is online.
# Once this happens it will request the data from the Li-Ion Batterty Controller (LBC) of the Nissa Leaf ZE1.
#
MQTT_SERVER=192.168.1.1
SUB="mosquitto_sub -h $MQTT_SERVER -t wican/5432048f421d/status -C 1"
PUB="mosquitto_pub -h $MQTT_SERVER -t wican/5432048f421d/can/tx"
CAN_STATUS_FILE=/tmp/can-status.txt

requestSoc() {
echo "Requesting SOC ..."
$PUB -m "{\"bus\":\"0\",\"type\":\"tx\",\"frame\":[{\"id\":1947,\"dlc\":8,\"rtr\":false,\"extd\":false,\"data\":[2,33,1,0,0,0,0,0]}]}"
sleep 0.1
$PUB -m "{\"bus\":\"0\",\"type\":\"tx\",\"frame\":[{\"id\":1947,\"dlc\":8,\"rtr\":false,\"extd\":false,\"data\":[48,0,0,0,0,0,0,0]}]}"
}

while true; do
  echo "Waiting for message ..."
  $SUB > $CAN_STATUS_FILE
  echo "Message received:"
  cat $CAN_STATUS_FILE
  STATUS=`cat $CAN_STATUS_FILE | jq .status | tr -d '"'` 
  if [ "$STATUS" = "online" ]; then
    echo "Requesting SOC ..."
    requestSoc
  fi
  echo "Sleeping ..."
  sleep 60
done
