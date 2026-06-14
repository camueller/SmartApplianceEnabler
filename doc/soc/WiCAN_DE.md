# WiCAN PRO OBD2 Adapter

![WiCANPRO](../../pics/wicanpro.png)

Der [WiCAN PRO OBD2 Adapter von MeatPi](https://www.meatpi.com/products/wican-pro) **verbindet den CAN-Bus des Fahrzeugs mit dem WLAN und ermöglicht die Kommunikation per MQTT**. Dadurch muss man nicht eine Fahrzeughersteller-API oder kostenpflichtigen Dienst nutzen, um den SOC zu ermitteln. Nebenbei kann der Adapter auch dazu verwendet werden, den SOC während der Fahrt via Bluetooth für Apps wie "A Better Router Planner" für die Ladeplanung verfügbar zu machen.

Zur Verwendung des WinCAN PRO mit dem *Smart Appliance Enabler* habe ich ein [Youtube-Video](https://www.youtube.com/watch?v=VgXChG5o-E8) gmeacht.

## Funktionsweise
Der Adapter bleibt **permanent eingesteckt** im Fahrzeug. Damit die Fahrzeug-Batterie nicht dauerhaft belastet wird, schaltet der **Sleep Mode** den WiCAN PRO wenige Minuten nach dem Abschalten des Fahrzeugs ab. Beim Einschalten des Fahrzeugs wacht auch der WiCAN PRO wieder auf. Die Abschaltung des WiCAN PRO ist vor allem deswegen sinnvoll, weil sich der CAN-Bus bei ausgeschaltetem Fahrzeug ebenfalls nach kurzer Zeit abschaltet und deshalb keine Daten mehr für den WiCAN PRO bereitstellen kann.

Wenn das Fahrzeug sich dem Haus nähert, verbindet sich der WiCAN PRO mit dem Haus-WLAN. Der Automate-Mode sorgt dafür, dass die im Fahrzeugprofil konfigurierten Werte periodisch vom CAN-Bus abgefagt werden und an die konfigurierten Ziele - bei mir ist das ein MQTT-Topic - übermittelt werden. Nach dem Abschalten und Abschliessen des Fahrzeugs bleibt der CAN-Bus noch einige Zeit aktiv. Wie lange, hängt vom Fahrzeug und sogar von der Software-Version ab. Nach einem Software-Update bei meinem Fahrzeug ist dieser Wert von ca. 5 Minuten auf 15 Minuten gestiegen. Wenn der CAN-BUs inaktiv wird, fällt die Bordspannung unter den Schwellwert für den Sleep-Mode des WiCAN PRO. Bei meinem Fahrzeug fällt sie von ca. 15V auf ca. 12V. Wenn die Spannung für die konfugurierte Zeit unterhalb des Schwellwertes bleibt, schaltet sich der WiCAN PRO ab.

Sobald das Fahrzeug irgendwann wieder eingeschaltet wird, wacht der WiCAN PRO aus dem Sleep-Mode auf und durch den Automate-Mode werden wieder periodisch die konfigurierten Werte an die konfigurierten Ziele übertragen. Wenn man den Bereich des WLANs des Hauses verlassen hat, werden logischerweise keine Werte mehr an die konfigurierten Ziele übertragen werden. Sobald man sich wieder dem Haus nähert beginnt der hier beschriebene Auflauf erneut.

## Konfiguration des WiCAN PRO OBD2-Adapter

Wichtige Einstellungen der Konfiguration des WiCAN PRO OBD2-Adapter sind:

### Tab: Settings
#### AP Config
- Mode: AP+Station oder BLE+Station
- AP Password: Passwort für den Zugriff auf des WLANs des WiCAN PRO, wenn dieser nicht mit dem WLAN des Hauses verbunden ist 

#### Network Configuration
- SSID: SSID des WLANs vom Haus
- Password: Passwort des WLANs vom Haus
- Security: Security des WLANs vom Haus 

#### CAN
- Protocol: AutoPID
- MQTT: Enable

#### MQTT
- MQTT URL: Hostname oder IP-Adresse des MQTT-Servers
- MQTT Port: Port des MQTT-Servers
- TX-Topic: wird automatisch vorgegeben
- RX-Topic: wird automatisch vorgegeben
- Status-Topic: z.B. wie TX-Topic, aber "tx" ersetzen durch "status"

### Tab:Automate
#### User Destinations: 
MQTT-Topic: z.B. wie TX-Topic, aber "tx" ersetzen durch "automate"

#### Automate Parameters
Vehicle Specific

#### Vehicle Specific PIDs
Vehicle Specific PIDs: Enable
Vehicle Model: Profil für das Fahrzeug auswählen

### Tab:Power Saving
#### Sleep Mode
- Sleep: Enable
- Sleep Voltage: Dieser Wert hängt vom Fahrzeug und Zustand der 12V-Batterie ab und muss ggf. angepasst werden, falls der WiCAN PRO nicht in den Sleep Mode geht.
- Sleep After: z.B. 5 min
- Perdiodic wake up: Disable

## Installation für Smart Appliance Enabler

Zunächst müssen die MQTT-Clients installiert werden, damit den Shell-Scripts die Interaktion mit dem MQTT-Server möglich ist:

```bash
sudo apt install mosquitto-clients
```

Die  Installation des Scripts zum Abruf des SOC erfolgt mit folgenden Befehlen:

```bash
$ mkdir /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/soc.sh -P /opt/sae/soc
$ chmod +x /opt/sae/soc/*.sh
```

## Konfiguration des Smart Appliance Enabler
In der Konfiguration muss unter "Wallbox" konfiguriert werden:

- Script zum Auslesen des SOC: `/opt/sae/soc/soc.sh`
- Ausführungswiederholung: [] Aktiviert (nicht ausgewählt)

Damit wird ereicht, dass das Script nur einmalig nach dem Verbinden mit der Wallbox ausgeführt wird, weil das SOC-Script nur zu diesem Zeitpunkt den korrekten Wert liefern kann.
