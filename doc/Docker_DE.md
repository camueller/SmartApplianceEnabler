# Installation als Docker-Container

Die nachfolgenden Kapitel beschreiben die Installation des *Smart Appliance Enabler* als __Docker Container__ und sollten in der angegebenen Reihenfolge umgesetzt werden.

## Host-Betriebssystem
Als Host-Betriebssystem für den Raspberry eignet sich insbesondere das schlanke __[Resin OS](https://resinos.io/)__ auf das sich diese Anleitung bezieht. Von der [Download-Seite](https://resinos.io/#downloads-raspberrypi) kann eine ZIP-Datei für das entsprechende Raspberry-Pi-Model heruntergeladen werden.

Auf der [Getting Started](https://resinos.io/docs/raspberry-pi2/gettingstarted/)-Seite ist die Installation beschrieben, wobei weder die Installation des Resin CLI noch die Anpassung der Datei ```/boot/config.json``` (da im Image bereits mit richtigem Inhalt vorhanden) erforderlich ist, wenn man den Raspberry Pi mittels Ethernet anschließt. Nur falls man statt Ethernet doch WLAN verwenden möchte, muss die Datei ```/boot/system-connections/resin-sample``` entsprechend der Dokumentation angepasst werden.

Das in der ZIP-Datei enthaltene Image muss auf ein Micro-SD-Karte geflasht werden, die mindestens 4GB haben sollte. Unter Linux kann das mit folgendem Befehl gemacht werden:
```
$ sudo dd if=resin.img of=/dev/mmcblk0 status=progress && sync
```

Danach muss man entsprechend der Dokumentation auf [Getting Started](https://resinos.io/docs/raspberry-pi2/gettingstarted/) die Micro-SD-Karte nur noch in den Raspberry Pi einsetzen und diesen damit booten. Auch der in der Dokumentation gezeigte ```ping resin.local``` sollte funktionieren, damit sichergestellt ist, dass man sich per SSH mit dem Raspberry Pi verbinden kann. Zu beachten ist, dass der SSH-Daemon von Resin OS auf Port 22222 horcht (anstelle des Standard-Ports 22). Dementsprechend sieht der Befehl unter Linux wie folgt aus:
```
$ ssh root@resin.local -p22222
```

## Docker-Image
Für den *Smart Appliance Enabler* gibt es ein Basis-Image (avanux/smartapplianceenabler) und darauf aubauend ein Image für Deutschland (avanux/smartapplianceenabler-de). Letzteres setzt lediglich die Zeitzone auf Ortszeit für Deutschland, was bei der Auswertung von Logs weniger Irritationen mit sich bringt.
Zum Installieren des Images genügt folgender Befehl:
```
docker pull avanux/smartapplianceenabler-de
```

## Docker-Konfiguration
### SAE-Volume
Der *Smart Appliance Enabler* benötigt ein schreibbares Verzeichnis, in dem er seine Dateien ablegen kann. Dazu wird in Docker das Volume *sae* erzeugt.
```
root@resin:/# docker volume create sae
sae
```
Danach sollte das neue Volume in der Liste der vorhanden Volumes enthalten sein:
```
root@resin:/# docker volume ls
DRIVER              VOLUME NAME
local               sae
```

Der *Smart Appliance Enabler* benötig für die Konfiguration des Loggings die Datei ```logback-spring.xml```. Um diese in das soeben erzeugte Volume zu laden, starten wir einen temporären Container, wobei das Volume unter ```/app``` gemountet wird. Im Container wird ```wget``` aufgerufen, um die Logging-Konfigurationsdatei herunterzuladen in das Verzeichnis ```/app``` (und damit auf das Volume).
```console
$ docker run -v sae:/app alpine:3.8 wget https://github.com/camueller/SmartApplianceEnabler/raw/master/logback-spring.xml -O /app/logback-spring.xml
Connecting to github.com (140.82.118.4:443)
Connecting to raw.githubusercontent.com (151.101.112.133:443)
logback-spring.xml   100% |*******************************|  2103   0:00:00 ETA
```

Jetzt kann man überprüfung, ob das Volume die benötigen Dateien enthält:
```console
$ docker run -v sae:/app alpine:3.8 ls /app
logback-spring.xml
```

### Init-System
Damit der SAE-Container nach jedem Boot des Raspberry Pi automatisch gestartet wird, muss eine Unit-Datei für SAE in systemd installiert werden.
Weil das root-Filesystem von ResinOS read-only ist, muss dieses zunächst read-write gemountet werden:
```
root@resin:/# mount -o remount,rw /dev/mmcblk0p2 /
```
Jetzt kann die Unit-Datei für SAE in systemd installiert werden:
TODO: **************** Ersetzen durch wget-Aufruf: *******************
```
scp -P22222 run/etc/systemd/system/sae.service root@resin:/lib/systemd/system
```
Jetzt muss systemd dazu gebracht werden, die Unit-Dateien neu einzulesen:
```
root@resin:/# systemctl daemon-reload
```
Abschliessend muss der SAE-Service noch aktiviert werden, damit der Container mit dem *Smart Appliance Enabler* beim Boot automatisch gestartet wird:
```
root@resin:/# systemctl enable sae.service
Created symlink /etc/systemd/system/multi-user.target.wants/sae.service → /lib/systemd/system/sae.service.
```

## Betrieb
### Manueller Start
Zum direkten Staren des *Smart Appliance Enabler* eignet sich folgender Befehl:
```console
docker run -v sae:/app -p 8080:8080 sae
```
Dabei wird das SAE-Volume gemounted und der Por 8080 des SAE auf dem localhost ebenfalls als Port 8080 verfügbar gemacht.

### Manueller Start über Init-System
Zum manuellen Starten des Container mit dem *Smart Appliance Enabler* via Init-System genügt folgender Befehl:
```
systemctl start sae
```
Alternativ kann auch der in der Unit-Datei ```/lib/systemd/system/sae.service``` enthaltende docker-Befehl verwendet werden.

### Konsole-Log anzeigen
Folgender Befehl zeigt die Ausgaben des *Smart Appliance Enabler* auf der Konsole an:
```
docker logs $(docker ps -q)
```

### SAE-Logdatei anzeigen
Zusätzlich zum Konsole-Log erzeugt der *Smart Appliance Enabler* für jeden Tag eine Log-Datei im ```/tmp```-Verzeichnis.
Mit dem nachfolgenden Befehl kann dieses angezeigt werden, wobei das Datum entsprechend angepasst werden muss:
```
docker container exec $(docker ps -q) tail -f /tmp/rolling-2017-10-30.log
```

### Befehl im SAE-Container ausführen
Falls man einen Befehl im laufenden Container des *Smart Appliance Enabler* ausführen möchte, kann man mit nachfolgendem Befehl eine entsprechend Shell erzeugen:
```
docker exec -i -t $(docker ps -q) /bin/bash
```