# Skoda
Skoda stellt leider auch kein offizielles API zur Verfügung, jedoch kann mittels Skoda Connect / MySkoda auf die Daten der Fahrzeugs zugegriffen werden.
Voraussetzung ist ein Account, der via www.skoda-connect.com genutzt werden kann. Dies gilt auch für den Citigo E iV, bei dem im Portal nichts angezeigt wird.  

**Hinweis:** Die Tests wurde mit einem Citigo E iV durchgeführt!  
**Dank an:** Die hier vorgestellte Lösung nutzt das API skodaconnect https://pypi.org/project/skodaconnect/, da die Authentisierung bei Skoda deutlich anders durchgeführt wird als bei Volkswagen: 

## Python-Implementierung
### Installation
Zunächst muss der Python-Package-Manager installiert werden.  
Entweder man arbeitet direkt auf dem Raspberry oder man nutzt den Webmin Zugang https://raspberrypi:10000 und startet dort die Command Shell, um die folgenden zwei Befehle nacheinander auszuführen. Die jeweilige installation kann durchaus etwas dauern, da die Packages erst aus dem Internet geladen werden müssen: 
```console
yes | sudo apt install python3-pip
```
```console
sudo pip3 install skodaconnect
```
Jetzt kann das Verzeichnis für das SOC-Script und Konfigurationsdatei angelegt und dorthin gewechselt werden:
```console
sae@raspberrypi ~ $ mkdir /opt/sae/soc
sae@raspberrypi ~ $ cd /opt/sae/soc
```
Das eigentliche SOC-Python-Script sollte mit dem Namen `Skoda_soc.py` und folgendem Inhalt angelegt werden (Username und Password noch anpassen!):
```console
#!/usr/bin/env python
# coding: utf8

import asyncio
import logging

from aiohttp import ClientSession
from skodaconnect import Connection

logging.basicConfig(level=logging.ERROR)

USERNAME = 'XXX'
PASSWORD = 'YYY'
PRINTRESPONSE = False


async def main():
    """Main method."""
    async with ClientSession(headers={'Connection': 'keep-alive'}) as session:
        print(f"Initiating new session to Skoda Connect with {USERNAME} as username")
        connection = Connection(session, USERNAME, PASSWORD, PRINTRESPONSE)
        if await connection._login():
            vehicle = connection.vehicles[0]
            print("state_of_charge %s" % str(vehicle.battery_level))


if __name__ == "__main__":
    asyncio.run(main())
```

Damit das SOC-Python-Script von überall aus aufgerufen werden kann, hilft folgendes kleine Shell-Script `/opt/sae/soc/Skoda_soc.sh`, das vom *Smart Appliance Enabler* aufgerufen wird:

```console
#!/bin/sh
python3 /opt/sae/soc/Skoda_soc.py
```

Das Script muss noch ausführbar gemacht werden:
```console
sae@raspberrypi:/opt/sae/soc $ chmod +x soc.sh
```
