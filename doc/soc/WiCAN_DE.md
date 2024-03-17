# WiCAN ODB2 Adapter

![meatPi](../../pics/meatPi.png)

Der [WiCAN ODB2 Adapter von MeatPi](https://www.meatpi.com/products/wican) **verbindet den CAN-Bus des Fahrzeugs mit dem WLAN und ermöglicht die Kommunikation per MQTT**.

## Funktionsweise
Der Adapter bleibt **permanent eingesteckt im Fahrzeug**.

Wenn das Fahrzeug sich dem Haus nähert, verbindet sich der **WiCAN mit dem WLAN** und sendet eine MQTT-Nachticht, dass er **online** ist. Darauf wartet das [wican-status.sh](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-status.sh)-Script und **fordert den SOC an** mittels einer MQTT-Nachricht. Auf die Antwort wartet das [wican-soc.sh](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-soc.sh)-Script, welches den **SOC extrahiert und zusammen mit einem Timestamp als MQTT-Nachricht (mit Retained-Flag) publiziert**.

Sobald das **Fahrzeug mit der Wallbox verbunden** wird, wird vom *Smart Appliance Enabler* das [SOC-Script für WiCAN ODB2-Adapter](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/soc.sh) ausgeführt, welches vom MQTT-Server die MQTT-Nachricht mit SOC und Timestamp erhält.

Ca. 3 Minuten nach dem Abschalten des Fahrzeugs **schaltet sich auch der WiCAN ODB2-Adapter aus**, um die Fahrzeug-Batterie nicht zu belasten.

## Konfiguration des WiCAN ODB2-Adapter

Wichtige Einstellungen der Konfiguration des WiCAN ODB2-Adapter sind:

### AP Config
- Mode: AP+Station

### Station Config
- SSID: SSID des WLANs vom Haus
- Password: Passwort des WLANs vom Haus

### CAN
- Protocol: elm327
- MQTT: Enable

### BLE
- BLE Status: Disable

### Sleep Mode
- Sleep: Enable

### MQTT
- MQTT URL: IP-Adresse des MQTT-Servers
- MQTT Port: Port des MQTT-Servers

## Installation für Smart Appliance Enabler

Zunächst müssen die MQTT-Clients installiert werden, damit den Shell-Scripts die Interaktion mit dem MQTT-Server möglich ist:

```bash
sudo apt install mosquitto-clients
```
Die  Installation

- des Scripts für das Monitoring des WiCAN-Status
- des Scripts zum Abruf des SOC
- SOC-Script für WiCAN ODB2-Adapter

erfolgt mit folgenden Befehlen:

```bash
$ mkdir /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-status.sh -P /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-soc.sh -P /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/soc.sh -P /opt/sae/soc
$ chmod +x /opt/sae/soc/*.sh
```

**Die Scripts `wican-status.sh` und `wican-soc.sh` müssen für das jeweilige Fahrzeug angepasst werden**: In `wican-status.sh` muss die CAN-Nachricht zu Anfordern des SOC eingetragen werden.  In `wican-soc.sh` muss aus der Antwort der SOC extrahiert werden. Ohne Anpassungen funktionieren beide Script aktuell für den Nissan Leaf ZE1. Die CAN-Nachrichten für ein bestimmtes Fahrzeug lassen sich meist in einschlägigen Internet-Foren finden oder auch mittels **Apps wie "Car Scanner"**, welche vom WiCAN-OBD2-Adapter unterstützt wird.

Zum Starten der ersten beiden Scripts werden Systemd-Services verwendet: 
```bash
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/wican-status.service -P /lib/systemd/system
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/wican-soc.service -P /lib/systemd/system
$ sudo systemctl daemon-reload
```

Die nachfolgende Befehle sind nur für `wican-status` beschrieben. Für `wican-soc` gelten sie analog.

Zum Start genügt:

```bash
$ sudo service wican-status start
```

Der Status lässt sich wie folgt anzeigen:

```bash
$ sudo service wican-status status
● wican-status.service - WiCan status monitor
     Loaded: loaded (/etc/systemd/system/wican-status.service; enabled; vendor preset: enabled)
     Active: active (running) since Sun 2024-02-25 14:36:10 CET; 1h 40min ago
   Main PID: 27260 (wican-status.sh)
      Tasks: 2 (limit: 4915)
        CPU: 431ms
     CGroup: /system.slice/wican-status.service
             ├─27260 /bin/bash /opt/sae/soc/wican-status.sh
             └─27261 mosquitto_sub -h 192.168.1.1 -t wican/5432048f421d/status -C 1

Feb 25 14:36:10 raspi2 systemd[1]: Started WiCan status monitor.
Feb 25 14:36:10 raspi2 wican-status.sh[27260]: Waiting for message ...
```

Zum Beenden genügt:

```bash
$ sudo service wican-status stop
```

Damit die Services auch nach einem Reboot gestartet werden, müssen sie entsprechend aktiviert werden:
```bash
$ sudo systemctl enable wican-soc
Created symlink /etc/systemd/system/multi-user.target.wants/wican-soc.service → /etc/systemd/system/wican-soc.service.
$ sudo systemctl enable wican-status
Created symlink /etc/systemd/system/multi-user.target.wants/wican-status.service → /etc/systemd/system/wican-status.service.
```

Die Konsole-Ausgaben der Scripts sind durch den Befehl `journalctl` verfügbar:
```bash
sudo journalctl _SYSTEMD_UNIT=wican-soc.service
-- Journal begins at Fri 2023-09-08 09:07:38 CEST, ends at Sun 2024-03-17 08:08:08 CET. --
Feb 25 14:39:03 raspi2 wican-soc.sh[27329]: Waiting for messages ...
Feb 25 17:18:45 raspi2 wican-soc.sh[27329]: Message received:
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6802,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[16,53,97,1,255,255,252,24]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6919,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[33,2,175,255,255,252,79,255]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6929,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[34,255,244,72,6,138,48,212]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6939,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[35,148,76,56,207,3,145,0]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6942,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[36,1,112,0,36,0,0,11]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6958,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[37,179,232,0,15,180,27,128]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6962,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[38,0,5,255,255,252,79,255]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6978,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[39,255,252,170,1,174,255,255]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[27329]: Waiting for messages ...
```

## Konfiguration des Smart Appliance Enabler
In der Konfiguration muss unter "Wallbox" konfiguriert werden:

- Script zum Auslesen des SOC: `/opt/sae/soc/soc.sh`
- Ausführungswiederholung: [] Aktiviert (nicht ausgewählt)

Damit wird ereicht, dass das Script nur einmalig nach dem Verbinden mit der Wallbox ausgeführt wird, weil das SOC-Script nur zu diesem Zeitpunkt den korrekten Wert liefern kann.
