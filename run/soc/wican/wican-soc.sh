#!/bin/bash
MQTT_SERVER=192.168.1.1
SUB="mosquitto_sub -h $MQTT_SERVER -t wican/12345678901d/can/rx -C 8"
PUB="mosquitto_pub -h $MQTT_SERVER -t wican/12345678901d/soc"
CAN_MESSAGE_FILE=/tmp/can-message.txt

# The status of the Li-Ion Battery Controller (LBC) of the Nissan Leaf ZE1 is contained in 8 CAN messages:
#
# {"bus":"0","type":"rx","ts":42623,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[16,53,97,1,0,0,0,0]}]}
# {"bus":"0","type":"rx","ts":42735,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[33,2,175,0,0,2,189,255]}]}
# {"bus":"0","type":"rx","ts":42743,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[34,255,252,24,23,30,48,212]}]}
# {"bus":"0","type":"rx","ts":42755,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[35,135,8,56,44,3,143,0]}]}
# {"bus":"0","type":"rx","ts":42764,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[36,1,169,198,36,47,0,5]}]}
# {"bus":"0","type":"rx","ts":42775,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[37,9,224,0,15,179,52,128]}]}
# {"bus":"0","type":"rx","ts":42783,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[38,0,5,0,0,2,189,0]}]}
# {"bus":"0","type":"rx","ts":42795,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[39,0,3,171,1,174,255,255]}]}

while true; do
  echo "Waiting for messages ..."
  $SUB > $CAN_MESSAGE_FILE
  echo "Message received:"
  cat $CAN_MESSAGE_FILE
  BYTE1=`cat $CAN_MESSAGE_FILE | grep "data\":\[36" | jq .frame[0].data[7]`
  BYTE2=`cat $CAN_MESSAGE_FILE | grep "data\":\[37" | jq .frame[0].data[1]`
  BYTE3=`cat $CAN_MESSAGE_FILE | grep "data\":\[37" | jq .frame[0].data[2]`
  SOC=$((($BYTE1 << 16 | $BYTE2 << 8 | $BYTE3) / 10000))
  TIMESTAMP=`date --iso-8601=seconds`
  $PUB -r -m "{\"soc\":$SOC, \"time\":\"$TIMESTAMP\"}"
done
