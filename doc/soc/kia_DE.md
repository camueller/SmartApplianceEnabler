# Kia (e-Niro)
Kia stellt ein API namens UVO bereit, für das im Folgenden eine Python-Implementierung beschrieben wird.
Für die Nutzung des Scripts ist eine Registrierung bei UVO unerlässlich. Es basiert auf einem im [openWB.de-Forum geposteten Script](https://openwb.de/forum/viewtopic.php?p=11877#p11877).

## Python-Installation
Zunächst muss Python installiert werden:

```bash
$ sudo apt-get -y install python3
```
 
## SOC-Script
Als Erstes muss das Verzeichnis für das SOC-Script und Konfigurationsdatei angelegt und dorthin gewechselt werden:

```bash
$ mkdir /opt/sae/soc
$ cd /opt/sae/soc
```

Die Konfigurationsdatei muss den Namen `soc.ini` mit folgendem Inhalt (Zeilen, die mit # beginnen, nicht ändern!) haben:

```
#email
DeineUVO-Mailadresse
#password
DeineUVO-Passwort
#pin
DeinePINinderUVO-App
#vin
DieKompletteFahrgestell-Nr.deinesNiro
#end
```

Das eigentliche SOC-Python-Script sollte mit dem Namen `soc.py` und folgendem Inhalt angelegt werden:

```python
import sys
import requests
import uuid
import json
import urllib.parse as urlparse
from urllib.parse import parse_qs

datei = open('soc.ini','r')
fertig = 0
temp = ""
while fertig == 0:
    temp = datei.readline()
    temp=temp.rstrip("\n")
    if temp == "#email":
        email = datei.readline()
        email=email.rstrip("\n")
    if temp == '#password':
        password = datei.readline()
        password=password.rstrip("\n")
    if temp == '#pin':
        pin = datei.readline()
        pin=pin.rstrip("\n")
    if temp == '#vin':
        vin = datei.readline()
        vin=vin.rstrip("\n")
    if temp == '#end':
        fertig=1
datei.close()


def main():
#diviceID
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/spa/notifications/register'
    headers = {
    'ccsp-service-id': 'fdc85c00-0a2f-4c64-bcb4-2cfb1500730a',
    'Content-type': 'application/json;charset=UTF-8',
    'Content-Length': '80',
    'Host': 'prd.eu-ccapi.kia.com:8080',
    'Connection': 'close',
    'Accept-Encoding': 'gzip, deflate',
    'User-Agent': 'okhttp/3.10.0'}
    data = {"pushRegId":"1","pushType":"GCM","uuid": str(uuid.uuid1())}
    response = requests.post(url, json = data, headers = headers)
    if response.status_code == 200:
        response = json.loads(response.text)
        deviceId = response['resMsg']['deviceId']
        #print(deviceId)
    else:
        #print('NOK diviceID')
        return

    #cookie für login
    session = requests.Session()
    response = session.get('https://prd.eu-ccapi.kia.com:8080/api/v1/user/oauth2/authorize?response_type=code&state=test&client_id=fdc85c00-0a2f-4c64-bcb4-2cfb1500730a&redirect_uri=https://prd.eu-ccapi.kia.com:8080/api/v1/user/oauth2/redirect')
    if response.status_code == 200:
        cookies = session.cookies.get_dict()
        #print(cookies)
    else:
        #print('NOK cookie fÃ¼r login')
        return

    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/user/language'
    headers = {'Content-type': 'application/json'}
    data = {"lang": "en"}
    response = requests.post(url, json = data, headers = headers, cookies = cookies)

    #login
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/user/signin'
    headers = {'Content-type': 'application/json'}
    data = {"email": email,"password": password}
    response = requests.post(url, json = data, headers = headers, cookies = cookies)
    if response.status_code == 200:
        response = json.loads(response.text)
        response = response['redirectUrl']
        parsed = urlparse.urlparse(response)
        authCode = ''.join(parse_qs(parsed.query)['code'])
        #print(authCode)

    else:
        #print('NOK login')
        return

    #token
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/user/oauth2/token'
    headers = {
        'Authorization': 'Basic ZmRjODVjMDAtMGEyZi00YzY0LWJjYjQtMmNmYjE1MDA3MzBhOnNlY3JldA==',
        'Content-type': 'application/x-www-form-urlencoded',
        'Content-Length': '150',
        'Host': 'prd.eu-ccapi.kia.com:8080',
        'Connection': 'close',
        'Accept-Encoding': 'gzip, deflate',
        'User-Agent': 'okhttp/3.10.0'}
    data = 'grant_type=authorization_code&redirect_uri=https%3A%2F%2Fprd.eu-ccapi.kia.com%3A8080%2Fapi%2Fv1%2Fuser%2Foauth2%2Fredirect&code=' + authCode
    response = requests.post(url, data = data, headers = headers)
    if response.status_code == 200:
        response = json.loads(response.text)
        access_token = response['token_type'] + ' ' + response['access_token']
        #print(access_token)
    else:
        #print('NOK token')
        return

    #vehicles
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/spa/vehicles'
    headers = {
        'Authorization': access_token,
        'ccsp-device-id': deviceId,
        'ccsp-application-id': '693a33fa-c117-43f2-ae3b-61a02d24f417',
        'offset': '1',
        'Host': 'prd.eu-ccapi.kia.com:8080',
        'Connection': 'close',
        'Accept-Encoding': 'gzip, deflate',
        'User-Agent': 'okhttp/3.10.0'}
    response = requests.get(url, headers = headers)
    if response.status_code == 200:
        response = json.loads(response.text)
        vehicleId = response['resMsg']['vehicles'][0]['vehicleId']
        #print(vehicleId)
    else:
        #print('NOK vehicles')
        return

    #vehicles/profile

    #prewakeup
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/spa/vehicles/' + vehicleId + '/control/engine'
    headers = {
        'Authorization': access_token,
        'ccsp-device-id': deviceId,
        'ccsp-application-id': '693a33fa-c117-43f2-ae3b-61a02d24f417',
        'offset': '1',
        'Content-Type': 'application/json;charset=UTF-8',
        'Content-Length': '72',
        'Host': 'prd.eu-ccapi.kia.com:8080',
        'Connection': 'close',
        'Accept-Encoding': 'gzip, deflate',
        'User-Agent': 'okhttp/3.10.0'}
    data = {"action":"prewakeup","deviceId": deviceId}
    response = requests.post(url, json = data, headers = headers)
    if response.status_code == 200:
        #print(response.text)
        response = ''
    else:
        #print('NOK prewakeup')
        return

    #pin
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v1/user/pin'
    headers = {
        'Authorization': access_token,
        'Content-type': 'application/json;charset=UTF-8',
        'Content-Length': '64',
        'Host': 'prd.eu-ccapi.kia.com:8080',
        'Connection': 'close',
        'Accept-Encoding': 'gzip, deflate',
        'User-Agent': 'okhttp/3.10.0'}
    data = {"deviceId": deviceId,"pin": pin}
    response = requests.put(url, json = data, headers = headers)
    if response.status_code == 200:
        response = json.loads(response.text)
        controlToken = 'Bearer ' + response['controlToken']
        #print(controlToken)
    else:
        #print('NOK pin')
        return

    #status
    url = 'https://prd.eu-ccapi.kia.com:8080/api/v2/spa/vehicles/' + vehicleId + '/status'
    headers = {
        'Authorization': controlToken,
        'ccsp-device-id': deviceId,
        'Content-Type': 'application/json'}
    response = requests.get(url, headers = headers)
    if response.status_code == 200:
        statusresponse = json.loads(response.text)
        #log (statusresponse)
        soc = statusresponse['resMsg']['evStatus']['batteryStatus']
        print('soc:',soc)
        charging = statusresponse['resMsg']['evStatus']['batteryCharge']
        #print('charging: ', charging)

    else:
        #print('NOK status')
        return

if __name__ == '__main__':
    main()
```

Damit das SOC-Python-Script von überall aus aufgerufen werden kann und trotzdem die `soc.ini` gefunden wird, hilft folgendes kleine Shell-Script `/opt/sae/soc/soc.sh`, das vom *Smart Appliance Enabler* aufgerufen wird:

```bash
#!/bin/sh
cd /opt/sae/soc
python3 ./soc.py
```

Das Script muss noch ausführbar gemacht werden:

```bash
$ chmod +x /opt/sae/soc/soc.sh
```

### Ausführung
Die Antwortzeit kann sehr unterschiedlich sein (1 bis 30 Sekunden).

```bash
$ /opt/sae/soc/soc.sh
soc: 65
```

Im *Smart Appliance Enabler* wird als SOC-Script `/opt/sae/soc/soc.sh` angegeben.
Außerdem muss der nachfolgende *reguläre Ausdruck* angegeben werden, um aus den Ausgaben den eigentlichen Zahlenwert zu extrahieren:

```
.*soc: (\d+).*
```
