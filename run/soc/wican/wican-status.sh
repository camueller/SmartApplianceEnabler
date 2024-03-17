#!/bin/bash
MQTT_SERVER=192.168.1.1
SUB="mosquitto_sub -h $MQTT_SERVER -t wican/12345678901d/status -C 1"
PUB="mosquitto_pub -h $MQTT_SERVER -t wican/12345678901d/can/tx"
CAN_STATUS_FILE=/tmp/can-status.txt

# The Li-ion Battery Controller (LBC) of the Nissan Leaf ZE1 accepts the multi-message query only.
# In order to request the status the following 2 CAN messages have to be sent with a small delay of 100ms.
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
    requestSoc
  fi
  echo "Sleeping ..."
  sleep 60
done
