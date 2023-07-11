# Skoda
Skoda stellt leider kein offizielles API zur Verfügung, jedoch kann mittels Skoda Connect / MySkoda auf die Daten der Fahrzeugs zugegriffen werden.
Voraussetzung ist ein Account, der via www.skoda-connect.com genutzt werden kann. Dies gilt auch für den Citigo E iV, bei dem im Portal nichts angezeigt wird.  

**Hinweis:** Die Tests wurde mit einem Citigo E iV durchgeführt!  
**Dank an:** Die hier vorgestellte Lösung nutzt das API skodaconnect www.pypi.org/project/skodaconnect, da die Authentisierung und Autorisierung bei Skoda deutlich anders durchgeführt werden muss als bei Volkswagen: 

## Python-Implementierung
### Installation
Zunächst muss der Python-Package-Manager installiert werden.  
Entweder man arbeitet direkt auf dem Raspberry oder man nutzt den Webmin Zugang https://raspberrypi:10000 und startet dort die Command Shell, um die folgenden zwei Befehle nacheinander auszuführen: 

```bash
$ yes | sudo apt install python3-pip
``` 

Und nun noch die genutzte Bibliothek `skodaconnect` installieren:  

```bash
$ sudo pip3 install skodaconnect
``` 

Die jeweilige Installation kann durchaus etwas dauern, da die Packages erst aus dem Internet geladen werden müssen.  

### Script ###

Jetzt kann das Verzeichnis für das SOC-Script und Konfigurationsdatei angelegt und dorthin gewechselt werden:  

```bash
mkdir /opt/sae/soc
cd /opt/sae/soc
```  

Das eigentliche SOC-Python-Script sollte mit dem Namen [`/opt/sae/soc/Skoda_soc.py`](Skoda_soc.py) und folgendem Inhalt angelegt werden (Username und Password noch anpassen!):

```python
#!/usr/bin/env python
#coding: utf8

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

Damit das SOC-Python-Script aufgerufen werden kann, hilft folgendes kleine Shell-Script `/opt/sae/soc/Skoda_soc.sh`, das später vom *Smart Appliance Enabler* aufgerufen wird:

```bash
#!/bin/sh
python3 /opt/sae/soc/Skoda_soc.py
```

Das Script muss noch ausführbar gemacht werden:

```bash
$ chmod +x /opt/sae/soc/Skoda_soc.sh
```

### Ausführung
Um zu testen, ob alles korrekt ist, kann folgender Aufruf ausgeführt werden:  

```bash
$ /opt/sae/soc/Skoda_soc.sh
Initiating new session to Skoda Connect with XXX as username
state_of_charge 66
```

Im *Smart Appliance Enabler* wird das SOC-Script `/opt/sae/soc/Skoda_soc.sh` angegeben.
Außerdem muss der nachfolgende *reguläre Ausdruck* angegeben werden, um aus den Ausgaben den eigentlichen Zahlenwert zu extrahieren:

```
.*state_of_charge (\d+)
```
