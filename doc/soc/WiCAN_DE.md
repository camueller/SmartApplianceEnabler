# WiCAN ODB2 Adapter
Leider können die meisten heimischen Wallboxen noch kein ISO 15118 (umfangreiche Kommunikation über Powerline Communication (PLC) zum Fahrzeug), weshalb die Wallbox nicht den SOC des Fahrzeugs kennt. Für einige Fahrzeuge existieren sogenannte *SOC-Scripts*, welche meist einen Zugriff der Hersteller-Homepage emulieren, um an den SOC zu kommen. Die Scripts sind meist **nicht dauerhaft stabil**, weil jede Änderung der Hersteller-Homepage eine Anpassung der SOC-Scripts bzw. der verwendeten Bibliotheken nach sich zieht. Manche Hersteller bieten den Zugriff auf Fahrzeugdaten auch über **kostenpflichtige Dienste** an.

Diese Probleme lassen sich umgehen, indem der SOC im Fahrzeug über die in jedem Fahrzeug vorhandene **ODB2-Schnittstelle** ausgelesen wird.

![meatPi](../../pics/meatPi.png)

Der [WiCAN ODB2 Adapter von MeatPi](https://www.meatpi.com/products/wican) **verbindet den CAN-Bus des Fahrzeugs mit dem WLAN und ermöglicht die Kommunikation per MQTT**.

## Funktionsweise
Der Adapter bleibt **permanent eingesteckt im Fahrzeug**.

Wenn das Fahrzeug sich dem Haus nähert, verbindet sich der **WiCAN mit dem WLAN** und sendet eine MQTT-Nachticht, dass er **online** ist. Darauf wartet das [wican-status.sh](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-status.sh)-Script und **fordert den SOC an** mittels einer MQTT-Nachricht. Auf die Antwort wartet das [wican-soc.sh](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-soc.sh)-Script, welches den **SOC extrahiert und zusammen mit einem Timestamp als MQTT-Nachricht (mit Retained-Flag) publiziert**.

Sobald das **Fahrzeug mit der Wallbox verbunden** wird, wird vom *Smart Appliance Enabler* das [SOC-Script für WiCAN ODB2-Adapter](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/soc.sh) ausgeführt, welches vom MQTT-Server die MQTT-Nachricht mit SOC und Timestamp erhält.

Ca. 3 Minuten nach dem Abschalten des Fahrzeugs **schaltet sich auch der WiCAN ODB2-Adapter aus**, um die Fahrzeug-Batterie nicht zu belasten.

## Konfiguration WiCAN ODB2-Adapter

Wichtige Einstellungen der Konfiguration des WiCAN sind:

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

**Die Scripts `wican-status.sh` und `wican-soc.sh` müssen für das jeweilige Fahrzeug angessat werden**: In `wican-status.sh` muss die CAN-Nachricht zu Anfordern des SOC eingetragen werden.  In `wican-soc.sh` muss aus der Antwort der SOC extrahiert werden. Ohne Anpassungen funktionieren beide Script aktuell für den Nissan Leaf ZE1. Die CAN-Nachrichten für ein bestimmtes Fahrzeug lassen sich meist in einschlägigen Internet-Foren finden oder auch mittels **Apps wie "Car Scanner"**, welche vom WiCAN-OBD2-Adapter unterstützt wird.

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

## Konfiguration des Smart Appliance Enabler
In der Konfiguration muss unter "Wallbox" konfiguriert werden:

- Script zum Auslesen des SOC: `/opt/sae/soc/soc.sh`
- Ausführungswiederholung: nach SOC-Änderung [%]: `100`

Damit wird ereicht, dass das Script nur einmalig nach dem Verbinden mit der Wallbox ausgeführt wird, weil das SOC-Script nur zu diesem Zeitpunkt den korrekten Wert liefern kann.
