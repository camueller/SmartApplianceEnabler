# Installation via Script
Die hier beschriebene manuelle Installation benötigt einen SSH-Zugriff auf den Raspberry Pi und die Interaktion mit der Shell. Falls möglich, sollte stattdessen die [Standard-Installation](Installation_DE.md) gewählt werden, die automatisch abläuft und keine Linux-Kenntnisse erfordert.

Nachdem der Login per SSH als User "pi" erfolgt ist, muss eine Root-Shell gestartet werden:
```console
pi@raspberrypi:~ $ sudo bash
```

In dieser Root-Shell wird die eigentliche Installation wie folgt gestartet:
```console
root@raspberrypi:/home/pi# curl -sSL https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/setup.sh | sh
```

Dabei lässt sich in der Console der Fortschritt der Installation verfolgen.

Wenn die Installation beendet ist, wird die **rote LED für eine Stunde ausgeschaltet**.

Der *Smart Appliance Enabler* läuft jetzt und es kann mit der [Konfiguration](Configuration_DE.md) fortgefahren werden werden.

Auch die Software zur Administration via Web-Browser (*webmin*) soll jetzt laufen - siehe [Hinweise zur Nutzung von webmin für *Smart Appliance Enabler*](Webmin_DE.md).
