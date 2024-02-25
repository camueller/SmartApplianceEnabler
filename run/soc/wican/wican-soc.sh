#!/bin/bash
#
# Waits for 8 MQTT messages containing data from the Li-Ion Batterty Controller (LBC) of the Nissa Leaf ZE1.
# Once messages are received the SOC will be extracted and published in another (retained) MQTT message along with timestamp.
#
MQTT_SERVER=192.168.1.1
SUB="mosquitto_sub -h $MQTT_SERVER -t wican/5432048f421d/can/rx -C 8"
PUB="mosquitto_pub -h $MQTT_SERVER -t wican/5432048f421d/soc"
CAN_MESSAGE_FILE=/tmp/can-message.txt

while true; do
  echo "Waiting for messages ..."
  $SUB > $CAN_MESSAGE_FILE
  echo "Message received:"
  cat $CAN_MESSAGE_FILE
  BYTE1=`cat $CAN_MESSAGE_FILE | sed -n 5p | jq .frame[0].data[7]`
  BYTE2=`cat $CAN_MESSAGE_FILE | sed -n 6p | jq .frame[0].data[1]`
  BYTE3=`cat $CAN_MESSAGE_FILE | sed -n 6p | jq .frame[0].data[2]`
  SOC=$((($BYTE1 << 16 | $BYTE2 << 8 | $BYTE3) / 10000))
  TIMESTAMP=`date --iso-8601=seconds`
  $PUB -r -m "{\"soc\":$SOC, \"time\":\"$TIMESTAMP\"}"
done
