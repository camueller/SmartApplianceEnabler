# Alternative Firmware Tasmota
Adapter, mit denen via WLAN beliebige Geräte ein- und ausgeschaltet werden können und die teilweise auch einen Stromzähler integriert haben, basieren oft auf dem Mikrokontroller [ESP8266](https://de.wikipedia.org/wiki/ESP8266).

Meist können diese Geräte nur mit den Cloud-Diensten des Adapter-Herstellers verwendet werden. Glücklicherweise existiert die alternative Firmware [Tasmota](https://github.com/arendst/Sonoff-Tasmota), die ursprünglich für diverse Sonoff-Adapter entwickelt wurde, inzwischen aber für eine Vielzahl von Adaptern verwendet werden kann.

Dazu muss die Tasmota-Firmware allerdings in den Flash-Speicher des Mikrokontrollers geschrieben ("geflasht") werden. Um den Mikrocontroller zum Flashen mit einem PC oder Raspberry Pi zu verbinden, ist ein FT232RL-Adapter (kostet zwischen 2 und 5 Euro ) erforderlich.

## Flashen
Zum eigentlichen Flashen benötigt man ein Programm wie [ESPEasy](https://www.heise.de/ct/artikel/ESPEasy-installieren-4076214.html) oder das darauf aufbauende [Tasmotizer](https://github.com/tasmota/tasmotizer).

Vor dem Flashen löscht man zunächst die alte Firmware:

```bash
$ esptool.py --port /dev/ttyUSB0 erase_flash
esptool.py v2.7
Serial port /dev/ttyUSB0
Connecting....
Detecting chip type... ESP8266
Chip is ESP8266EX
Features: WiFi
Crystal is 26MHz
MAC: bc:dd:c2:23:23:84
Uploading stub...
Running stub...
Stub running...
Erasing flash (this may take a while)...
Chip erase completed successfully in 4.0s
Hard resetting via RTS pin...
```

Danach kann man die Tastmota-Firmaware flashen:

```bash
$ esptool.py --port /dev/ttyUSB0 write_flash -fs 1MB -fm dout 0x00000 sonoff-DE.bin
esptool.py v2.7
Serial port /dev/ttyUSB0
Connecting....
Detecting chip type... ESP8266
Chip is ESP8266EX
Features: WiFi
Crystal is 26MHz
MAC: bc:dd:c2:23:23:84
Uploading stub...
Running stub...
Stub running...
Configuring flash size...
Compressed 517008 bytes to 356736...
Wrote 517008 bytes (356736 compressed) at 0x00000000 in 33.1 seconds (effective 124.9 kbit/s)...
Hash of data verified.

Leaving...
Hard resetting via RTS pin...
```

## Aktualisieren der Firmware
Wenn sich auf dem Adapter bereits Tasmota-Firmware befindet, kann man diese über den Menüpunkt `Firmware update` aktualisieren. Falls dabei ein Fehler auftritt (`Upload-buffer-Vergleich weicht ab`), sollte man zunächst die `tasmota-minimal.bin` aufspielen und danach erst die gewünschte Vollversion. [Quelle](https://www.schimmer-media.de/forum/index.php?thread/223-sonoff-basic-update-nicht-m%C3%B6glich/)

## Kalibrieren der Firmware
Vor der Benutzung sollte die Tasmota-Firmware [kalibriert werden](https://tasmota.github.io/docs/Power-Monitoring-Calibration/), da korrekte Messungen durch Hardware- und Timing-Unterschiede beeinflusst werden.

## Verwendung von Tasmota-Adaptern im Smart Appliance Enabler
Der *Smart Appliance Enabler* kann mit Tasmota-Adaptern via MQTT oder HTTP kommunizieren. Es empfiehlt sich allerdings MQTT, da es auf dem Adapter selbst und auch im Netzwerk weniger Resourcen beansprucht und stabiler ist. 

## Konfigurieren der Firmware
### Verwendung von Sommerzeit
Die Verwendung von Sommerzeit ist in Tasmota standardmässig nicht aktiv. Um bei Zeitangaben die Sommerzeit zu nutzen muss man auf der Tasmota-Web-Konsole des Adapters den Befehl `timezone 99` eingeben und die Eingabe mit `Enter` abschliessen:

```
10:46:24 CMD: timezone 99
10:46:24 MQT: stat/tasmota/RESULT = {"Timezone":99}
```

### Anzahl der Nachkommastellen für den Zählerstand (nur erforderlich bei Verwendung von "Zählerstand" als Parameter)
Der `Zählerstand` wird standardmässig von Tasmota nur mit 3 Nachkommastellen geliefert. Damit der *Smart Appliance Enabler* aus Zählertandsdifferenzen die Leistung möglichst genau berechnen kann, muss der Tasmota-Adapter **auf 5 Nachkomstellen konfiguriert** werden. Dazu geht man auf die Tasmota-Web-Konsole des Adapters und gibt den Befehl `EnergyRes 5` ein und schliesst die Eingabe mit `Enter` ab:

```
17:14:25 CMD: EnergyRes 5
17:14:25 RSL: RESULT = {"EnergyRes":5}
```

### MQTT (nur bei Verwendung von MQTT als Kommunikationsprotokoll)
Zunächst muss der Tasmota-Adapter für die [Nutzung von MQTT konfiguriert](https://tasmota.github.io/docs/MQTT/) werden, was vor allem `Host` und `Port` des MQTT-Brokers sowie das `Topic` betrifft.

#### Häufigkeit der MQTT-Nachrichten
Die Telemetriedaten (dazu gehören der Zählerstand und die aktuelle Leistungsaufnahme) werden von Tasmota standardmässig nur alle 5 Minuten versendet. Um diese alle 60 Sekunden zu versenden muss man auf der Tasmota-Web-Konsole des Adapters den Befehl `TelePeriod 60` eingeben und die Eingabe mit `Enter` abschliessen:

```
10:50:37 CMD: TelePeriod 60
10:50:37 MQT: stat/tasmota/RESULT = {"TelePeriod":60}
```

## Geräte mit Tasmota-Firmware als Stromzähler 
### MQTT
Tasmota versendet die Telemetriedaten im JSON-Format, welches formatiert wie folgt aussieht:

```json
{
  "Time": "2023-04-13T11:15:37",
  "ENERGY": {
    "TotalStartTime": "2021-03-29T17:27:44",
    "Total": 0.00512,
    "Yesterday": 0.00021,
    "Today": 0.00512,
    "Period": 0,
    "Power": 0,
    "ApparentPower": 0,
    "ReactivePower": 0,
    "Factor": 0,
    "Voltage": 236,
    "Current": 0
  }
}
```

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler* (wobei Parameter `Zählerstand` empfohlen wird und "tasmota" durch den für den Tasmota-Adapter konfigurierten Topic-Namen ersetzt werden muss):

| Feld                                              | Wert                  |
|---------------------------------------------------|-----------------------|
| Topic                                             | tele/tasmota/SENSOR   |           
| Format                                            | JSON                  |
| Pfad für Extraktion (bei Parameter `Zählerstand`) | $.ENERGY.Total        |
| Pfad für Extraktion (bei Parameter `Leistung`)    | $.ENERGY.Power        |
| Pfad für Zeit-Extraktion                          | $.Time                |

**Achtung:** Voraussetzung für die Angabe einer Zeit-Extraktion ist die Angabe der Zeit als lokale Ortszeit (nicht UTC!) in den MQTT-Nachrichten.

### HTTP
Mit dem folgenden Befehl kann der Status von Tasmota-Adaptern abgefragt werden, der auch den Zählerstand und die Leistung beinhaltet:

```bash
$ curl http://192.168.1.1/cm?cmnd=Status%208
```

Tasmota liefert die Antwort im JSON-Format, welche formatiert wie folgt aussieht:

```json
{
  "StatusSNS": {
    "Time": "2021-02-03T15:12:52",
    "Switch1": "ON",
    "ENERGY": {
      "TotalStartTime": "2020-01-05T12:41:22",
      "Total": 13.48712,
      "Yesterday": 0.000,
      "Today": 0.000,
      "Power": 0,
      "ApparentPower": 0,
      "ReactivePower": 0,
      "Factor": 0.00,
      "Voltage": 0,
      "Current": 0.000
    }
  }
}
```

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler*:

| Feld                                              | Wert                                  |
|---------------------------------------------------| ------------------------------------- |
| Format                                            | JSON                                  |
| URL                                               | http://192.168.1.1/cm?cmnd=Status%208 |
| Pfad für Extraktion (bei Parameter `Zählerstand`) | $.StatusSNS.ENERGY.Total              |
| Pfad für Extraktion (bei Parameter `Leistung`)    | $.StatusSNS.ENERGY.Power              |


## Geräte mit Tasmota-Firmware als Schalter
### MQTT
Für die Verwendung von MQTT als Schalter ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler* (wobei "tasmota" durch den für den Tasmota-Adapter konfigurierten Topic-Namen ersetzt werden muss):

| Feld                         | Wert               |
|------------------------------|--------------------|
| Topic                        | cmnd/tasmota/Power |
| Payload beim Einschalten     | ON                 |
| Payload beim Ausschalten     | OFF                |
| Status-Topic                 | stat/tasmota/POWER |
| Regex für Status-Extraktion  | ON                 |

### HTTP
Der Schaltzustand kann wie folgt geändert werden:

_Einschalten_

```bash
$ curl http://192.168.1.1/cm?cmnd=Power%20On
```

_Ausschalten_
```bash
$ curl http://192.168.1.1/cm?cmnd=Power%20Off
```

_Abfrage des Schaltzustandes_
```bash
$ curl http://192.168.1.1/cm?cmnd=Power
{"POWER":"OFF"}
```

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler*:

| Feld                      | URL                                     | Regex für Extraktion  |
|---------------------------|-----------------------------------------|-----------------------|
| Aktion "Einschalten"      | http://192.168.1.1/cm?cmnd=Power%20On   |                       |
| Aktion "Ausschalten"      | http://192.168.1.1/cm?cmnd=Power%20Off  |                       |
| Parameter "Eingeschaltet" | http://192.168.1.1/cm?cmnd=Power        | :.ON                  |

## Laufzeitanforderung bei Tastendruck erstellen
Tasmota ermöglicht das Anlegen von Regeln, die bei bestimmten Ereignissen ausgelöst werden.
Entsprechend kann man eine Regel definieren, dass beim Druck auf eine Taste des Tasmota-Adapters eine Laufzeitanforderung an den *Smart Appliance Enabler* übermittelt wird. Dadurch kann (und muss!) die Verwendung der Anlaufstromerkennung entfallen. Ohne diese würde das Gerät immer entsprechend konfigurierter Zeitpläne eingeschaltet werden, weshalb diese ebenfalls nicht verwendet werden sollten. Als Konsequenz müssen beim Druck auf die Tasts des Tasmota-Adapters alle Angaben übermittelt werden, die sonst über Zeitpläne bereitgestellt werden.  

Zum Anlegen einer Regel muss man auf der Tasmota-Konsole eingeben:

```
Rule1 ON Button1#State=3 DO WebSend [192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600 ENDON
```

... wobei
- `Button1` ist der Name des Tasters, mit dem das Ereignis verknüpft wird
- `State=3` definiert, dass das Ereignis bei einem langen Tastendruck (Hold) ausgelöst wird
- `192.168.0.1` ist Hostname oder IP-Adresse, unter welcher der *Smart Appliance Enabler* erreichbar ist
- `F-xxxxxxxx-xxxxxxxxxxxx-xx` die Appliance-ID ist
- `3600` die gewünschte Laufzeit in Sekunden ist
- `21600` der späteste Zeitpunkt in Sekunden ab jetzt ist, zu dem die Laufzeit beendet sein muss

Diese Regel muss jetzt noch aktiviert werden:

```
Rule1 1
```

Bei langem Drücken (> 4 Sekunden) des Tasters erfolgt jetzt die gewünschte Laufzeitanforderung:

```
11:01:28 APP: Knopf1 Mehrfachdruck 1
11:01:29 WIF: Prüfe Verbindung...
11:01:29 WIF: verbunden
11:01:29 RUL: BUTTON1#STATE=3 performs "WebSend [192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600"
11:01:29 SRC: Rule
11:01:29 CMD: Gruppe 0, Index 1, Befehl "WEBSEND", Daten "[192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600"
11:01:29 RSL: RESULT = {"WebSend":"Done"}
```

Bei Bedarf kann die Regel jederzeit wieder deaktiviert werden:

```
Rule1 0
```

Weitere Möglichkeiten und eine genaue Beschreibung der Parameter findet man in der Tasmota-Dokumentation unter [Buttons and Switches](https://tasmota.github.io/docs/Buttons-and-Switches/#button).
