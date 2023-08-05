# Installation via Script
Die hier beschriebene Installation via Script benötigt einen SSH-Zugriff auf den Raspberry Pi, so dass das Installationsscript gestartet werden kann. Es sind keine weiteren Linux-Kenntnisse erforderlich.

Nachdem der Login per SSH als User "pi" erfolgt ist, muss eine Root-Shell gestartet werden:
```bash
pi@raspberrypi:~ $ sudo bash
```

In dieser Root-Shell wird die eigentliche Installation wie folgt gestartet:
```bash
root@raspberrypi:/home/pi# curl -sSL https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/setup.sh | sh
```

Dabei lässt sich in der Console der Fortschritt der Installation verfolgen.

Wenn die Installation beendet ist, wird die **rote LED für eine Stunde ausgeschaltet**.

Der *Smart Appliance Enabler* läuft jetzt und es kann mit der [Konfiguration](Configuration_DE.md) fortgefahren werden werden.

Auch die Software zur Administration via Web-Browser (*webmin*) sollte jetzt laufen - siehe [Hinweise zur Nutzung von webmin für *Smart Appliance Enabler*](Webmin_DE.md).
