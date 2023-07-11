# Volkswagen

Volkswagen stellt keine offizielle API für Fremdsoftware bereit, sie untersagt es sogar in den AGB für CarNet. Daher erfolgt die Benutzung dieser Anleitung auf eigenes Risiko. 
Änderungen an den inoffiziellen Schnittstellen bei VW können jederzeit das Abfragen der nötigen Werte beeinflussen.

## Vorraussetzung

Die Vorraussetzung zur Nutzung dieser Anleitung ist ein aktiver CarNet Account mit den kostenpflichtigen Service "Security & Service Plus" oder "e-Remote".

## Installation

Um das SoC aus dem CarNet abzufragen, habe ich mich einen GitHub Projektes bedient. Eine detailierte Installationsanleitung erhaltet ihr in [diesem Projekt](https://github.com/wez3/volkswagen-carnet-client). Hier findet ihr auch das original Script.

In groben Zügen braucht es eine Python Installation mit dem Python Modul "Requests".

## Script

Hier das Script für Copy&Paste oder als [Download](VW_soc.py)
Einfach nur Username und Passwort eintragen und unter `/opt/sae/soc/VW_soc.py` speichern.

```python
#!/usr/bin/python
# coding: utf8

import sys
import requests
import json

# Login information for the VW CarNet app
CARNET_USERNAME = 'XXX'
CARNET_PASSWORD = 'YYY'

# Fake the VW CarNet mobile app headers
HEADERS = { 'Accept': 'application/json',
			'X-App-Name': 'eRemote',
			'X-App-Version': '1.0.0',
			'User-Agent': 'okhttp/2.3.0' }

def carNetLogon():
	#print "Logging in"
	r = requests.post('https://msg.volkswagen.de/fs-car/core/auth/v1/VW/DE/token', data = {'grant_type':'password',
												'username':CARNET_USERNAME,
												'password':CARNET_PASSWORD}, headers=HEADERS)
	responseData = json.loads(r.content)
	token = responseData.get("access_token")
	HEADERS["Authorization"] = "AudiAuth 1 " + token
	#print "token: " + token
	return token

def retrieveVehicles():
	#print "Retrieving verhicle"
	r = requests.get('https://msg.volkswagen.de/fs-car/usermanagement/users/v1/VW/DE/vehicles', headers=HEADERS)
	responseData = json.loads(r.content)
	VIN = responseData.get("userVehicles").get("vehicle")[0]
	return VIN

def retrieveSOC(VIN):
        r = requests.get('https://msg.volkswagen.de/fs-car/bs/batterycharge/v1/VW/DE/vehicles/' + VIN + '/charger', headers=HEADERS)
        #print "Charger request: " + r.content
        responseData = json.loads(r.content)
        #print "Retrieving CarNetInfo"
	stateOfCharge = responseData.get("charger").get("status").get("batteryStatusData").get("stateOfCharge").get("content")
        remainingChargingTime = responseData.get("charger").get("status").get("batteryStatusData").get("remainingChargingTime").get("content")
        remainingChargingTimeTargetSOC = responseData.get("charger").get("status").get("batteryStatusData").get("remainingChargingTimeTargetSOC").get("content")
	primaryEngineRange = responseData.get("charger").get("status").get("cruisingRangeStatusData").get("primaryEngineRange").get("content")
	print str(stateOfCharge) 

token = carNetLogon()
VIN = retrieveVehicles()
retrieveSOC(VIN)
``` 

Nach dem Speichern der Datei ein `chmod +x /opt/sae/soc/VW_soc.py` machen. Danach kann man das Script mit `$ /opt/sae/soc/soc.py` testen.
Es wird ein ganzzahliger Wert zurück geliefert, der direkt vom SAE verarbeitet werden kann.

## Anbindung an SAE

Im SAE kann man das Script direkt in der jeweiligen Fahrzeugkonfiguration eintragen, z.b. `/opt/sae/soc/VW_soc.py`.

## Anmerkungen / Einschränkungen

- Das Abfragen des SoC kann einige Zeit (30sek) dauern. Das beeinflusst u.a. die Operationen innerhalb des SAE.
- Im CarNet Account darf nur 1 Fahrzeug registriert sein!
