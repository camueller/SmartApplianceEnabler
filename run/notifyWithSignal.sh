#!/bin/sh

#Diese Daten hier sind individuell auszufuellen
#Telefonnummer eingeben im Format +490123456789
phone=
#Apikey von callmebot.com
apikey=

# für Telegram muss die senderId auf den von BotFather für den Bot zugewiesener Token gesetzt sein
senderId=$1
# Name des Gerätes in Device2EM.xml
deviceName=$2
# Typ des Gerätes in Device2EM.xml
deviceType=$3
# Hersteller des Gerätes in Device2EM.xml
deviceVendor=$4
# Seriennummer des Gerätes in Device2EM.xml
deviceSerial=$5
# Benachrichtigungs-Key des Ereignisses (z.B. CONTROL_OFF)
key=$6
# Benachrichtigungs-Text des Ereignisses (z.B. "Das Gerät wurde ausgeschaltet")
text=$7

#Variablen zum testen
#text="Hallo von SAE"
#deviceName="Device01"

#Entfernen der Leerzeichen
sendText=$(echo "$text" | tr ' ' +)

#Zusammensetzen des Sendestrings
sendText=$deviceName+":"+$sendText

sendMessage="https://api.callmebot.com/signal/send.php?phone=$phone&apikey=$apikey&text=$sendText"
curl $sendMessage
