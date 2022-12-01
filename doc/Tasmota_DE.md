# Alternative Firmware Tasmota
Adapter, mit denen via WLAN beliebige Geräte ein- und ausgeschaltet werden können und die teilweise auch einen Stromzähler integriert haben, basieren oft auf dem Mikrokontroller [ESP8266](https://de.wikipedia.org/wiki/ESP8266).

Meist können diese Geräte nur mit den Cloud-Diensten des Adapter-Herstellers verwendet werden. Glücklicherweise existiert die alternative Firmware [Tasmota](https://github.com/arendst/Sonoff-Tasmota), die ursprünglich für diverse Sonoff-Adapter entwickelt wurde, inzwischen aber für eine Vielzahl von Adaptern verwendet werden kann.

Dazu muss die Tasmota-Firmware allerdings in den Flash-Speicher des Mikrokontrollers geschrieben ("geflasht") werden. Um den Mikrocontroller zum Flashen mit einem PC oder Raspberry Pi zu verbinden ist ein FT232RL-Adapters (kostet zwischen 2 und 5 Euro ) erforderlich.

## Flashen
Zum eigentlichen Flashen benötigt man ein Programm wie [ESPEasy](https://www.heise.de/ct/artikel/ESPEasy-installieren-4076214.html) oder das darauf aufbauende [Tasmotizer](https://github.com/tasmota/tasmotizer).

Vor dem Flashen löscht man zunächst die alte Firmware:
```console
pi@raspberrypi:~ $ esptool.py --port /dev/ttyUSB0 erase_flash
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
```console
pi@raspberrypi:~ $ esptool.py --port /dev/ttyUSB0 write_flash -fs 1MB -fm dout 0x00000 sonoff-DE.bin
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
Wenn sich auf dem Adapter bereits Tasmota-Firmware befindet, kann man diese über den Menüpunkt `Firmware update` aktualsieren. Falls dabei ein Fehler auftritt (`Upload-buffer-Vergleich weicht ab`), sollte man zunächst die `tasmota-minimal.bin` aufspielen und danach erst die gewünschte Vollversion. [Quelle](https://www.schimmer-media.de/forum/index.php?thread/223-sonoff-basic-update-nicht-m%C3%B6glich/)

## Geräte mit Tasmota-Firmware als Stromzähler 
Mit dem folgenden Befehl kann der Status von Tasmota-Adaptern abgefragt werden, der auch den Zählerstand und die Leistung beinhaltet:
```console
pi@raspberrypi:~ $ curl http://192.168.1.1/cm?cmnd=Status%208
```

Tasmota liefert die Antwort im JSON-Format, die formatiert wie folgt aussieht:
```
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

Der `Zählerstand` wird standardmässig von Tasmota nur mit 3 Nachkommastellen geliefert. Damit der *Smart Appliance Enabler* aus Zählertandsdifferenzen die Leistung möglich genau berechnen kann, muss der Tasmota-Adapter **auf 5 Nachkomstellen konfiguriert** werden.
Dazu geht man auf die Tasmota-Web-Konsole des Adapters und gibt den Befehl `EnergyRes 5` ein und schliesst die Eingabe mit `Enter` ab:
```
17:14:25 RSL: RESULT = {"EnergyRes":5}
```

Für jede Zähler-Abfrage finden sich im [Log](Logging_DE.md) folgende Zeilen:
```
2021-02-08 00:09:54,324 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000014-00: Executing timer task name=PollEnergyMeter id=13049675
2021-02-08 00:09:54,324 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:107] F-00000001-000000000014-00: Sending GET request url=http://kuehltruhe/cm?cmnd=Status%208
2021-02-08 00:09:54,459 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:168] F-00000001-000000000014-00: Response code is 200
2021-02-08 00:09:54,462 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:86] F-00000001-000000000014-00: url=http://kuehltruhe/cm?cmnd=Status%208 httpMethod=GET data=null path=$.StatusSNS.ENERGY.Total
2021-02-08 00:09:54,463 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:89] F-00000001-000000000014-00: Response: {"StatusSNS":{"Time":"2021-02-08T00:09:54","ENERGY":{"TotalStartTime":"2020-01-05T17:01:57","Total":56.00865,"Yesterday":0.53820,"Today":0.00005,"Power":0,"ApparentPower":5,"ReactivePower":5,"Factor":0.06,"Voltage":237,"Current":0.021}}}
2021-02-08 00:09:54,464 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:58] F-00000001-000000000014-00: value=56.00865 protocolHandlerValue=56.00865 valueExtractionRegex=null extractedValue=56.00865
2021-02-08 00:09:54,465 DEBUG [Timer-0] d.a.s.m.PollEnergyMeter [PollEnergyMeter.java:120] F-00000001-000000000014-00: Adding value: timestamp=2021-02-08T00:09:54.324795 value=56.00865
```

*Webmin*: In [View Logfile](Logging_DE.md#user-content-webmin-logs) gibt man hinter `Only show lines with text` ein `Http` und drückt Refresh.

## Geräte mit Tasmota-Firmware als Schalter

Der Schaltzustand kann wie folgt geändert werden:

_Einschalten_
```console
axel@p51:~$ curl http://192.168.1.1/cm?cmnd=Power%20On
```

_Ausschalten_
```console
axel@p51:~$ curl http://192.168.1.1/cm?cmnd=Power%20Off
```

_Abfrage des Schaltzustandes_
```console
$ curl http://192.168.1.1/cm?cmnd=Power
{"POWER":"OFF"}
```

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler*:

| Feld                      | URL                                    | Regex für Extraktion |
|---------------------------| ----                                   | ----                 |
| Aktion "Einschalten"      | http://192.168.1.1/cm?cmnd=Power%20On  |                      |
| Aktion "Ausschalten"      | http://192.168.1.1/cm?cmnd=Power%20Off |                      |
| Parameter "Eingeschaltet" | http://192.168.1.1/cm?cmnd=Power       | :.ON                 |

Für jeden Schaltvorgang finden sich im [Log](Logging_DE.md) folgende Zeilen:
```
2020-01-06 14:51:22,817 INFO [http-nio-8080-exec-4] d.a.s.c.HttpSwitch [HttpSwitch.java:128] F-00000001-000000000001-00: Switching on
2020-01-06 14:51:22,817 DEBUG [http-nio-8080-exec-4] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: Sending GET request url=http://192.168.1.1/cm?cmnd=Power%20On
2020-01-06 14:51:22,984 DEBUG [http-nio-8080-exec-4] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:160] F-00000001-000000000001-00: Response code is 200
```

*Webmin*: In [View Logfile](Logging_DE.md#webmin-logs) gibt man hinter `Only show lines with text` ein `Http` und drückt Refresh.

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
- `192.168.0.1` der Hostname oder die IP-Adresse ist, unter welcher der *Smart Appliance Enabler* erreichbar ist
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

Weitere Möglichkeiten und eine genaue Beschreibung der Parameter findet man in der Tasmota-Dokumentation unter https://tasmota.github.io/docs/Buttons-and-Switches/#button
