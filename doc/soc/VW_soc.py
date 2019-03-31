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
