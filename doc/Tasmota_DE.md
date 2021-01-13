# Alternative Firmware Tasmota
Adapter, mit denen via WLAN beliebige Geräte ein- und ausgeschaltet werden können und die teilweise auch einen Stromzähler integriert haben, basieren oft auf dem Mikrokontroller [ESP8266](https://de.wikipedia.org/wiki/ESP8266).

Meist können diese Geräte nur mit den Cloud-Diensten des Adapter-Herstellers verwendet werden. Glücklicherweise existiert die alternative Firmware [Tasmota](https://github.com/arendst/Sonoff-Tasmota), die ursprünglich für diverse Sonoff-Adapter entwickelt wurde, inzwischen aber für eine Vielzahl von Adaptern verwendet werden kann.

Dazu muss die Tasmota-Firmware allerdings in den Flash-Speicher des Mikrokontrollers geschrieben ("geflasht") werden. Um den Mikrocontroller zum Flashen mit einem PC oder Raspberry Pi zu verbinden ist ein FT232RL-Adapters (kostet zwischen 2 und 5 Euro ) erforderlich.

## Flashen
Zum eigentlichen Flashen benötigt man ein Programm wie [ESPEasy](https://www.heise.de/ct/artikel/ESPEasy-installieren-4076214.html).

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

Die aktuelle Leistungsaufnahme kann wie folgt abgefragt werden:
```
curl http://192.168.1.1/cm?cmnd=Status%208
{"StatusSNS":{"Time":"2019-09-06T20:06:19","ENERGY":{"TotalStartTime":"2019-08-18T11:07:55","Total":0.003,"Yesterday":0.000,"Today":0.003,"Power":26,"ApparentPower":25,"ReactivePower":25,"Factor":0.06,"Voltage":239,"Current":0.106}}}
```

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler*:

| Feld                                            | Wert                                  |
| ----------------------------------------------- | ------------------------------------- |
| URL                                             | http://192.168.1.1/cm?cmnd=Status%208 |
| Regulärer Ausdruck zum Extrahieren der Leistung | .\*"Power":(\d+).\*       |

Für jede Zähler-Abfrage finden sich in der [Log-Datei](Support.md#Log) folgende Zeilen:
```
2017-06-03 18:39:55,125 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:101] F-00000001-000000000001-00: Sending HTTP request
2017-06-03 18:39:55,125 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:102] F-00000001-000000000001-00: url=http://192.168.1.1/cm?cmnd=Status%208
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:103] F-00000001-000000000001-00: data=null
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:104] F-00000001-000000000001-00: contentType=null
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: username=null
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:106] F-00000001-000000000001-00: password=null
2017-06-03 18:39:55,146 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:118] F-00000001-000000000001-00: Response code is 200
2017-06-03 18:39:55,147 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:119] F-00000001-000000000001-00: HTTP response: STATUS8 = {"StatusSNS":{"Time":"2019-09-06T19:19:50","ENERGY":{"TotalStartTime":"2019-08-18T11:30:33","Total":27.772,"Yesterday":1.046,"Today":0.980,"Power":26,"ApparentPower":47,"ReactivePower":47,"Factor":0.05,"Voltage":231,"Current":0.204}}}
2017-06-03 18:39:55,147 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:120] F-00000001-000000000001-00: Power value extraction regex: .*"Power":(\d+).*
2017-06-03 18:39:55,153 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:119] F-00000001-000000000001-00: Power value extracted from HTTP response: 26
```

*Webmin*: In [View Logfile](Logging_DE.md#webmin-logs) gibt man hinter `Only show lines with text` ein `Http` und drückt Refresh.

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

| Zustand / Aktion | URL                                    | Regex für Extraktion
| ----             | ----                                   | ----
| Einschalten      | http://192.168.1.1/cm?cmnd=Power%20On  |
| Ausschalten      | http://192.168.1.1/cm?cmnd=Power%20Off |
| Eingeschaltet    | http://192.168.1.1/cm?cmnd=Power       | :.ON

Für jeden Schaltvorgang finden sich in der [Log-Datei](Support.md#Log) folgende Zeilen:
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
Rule1 ON Button1#State=2 DO WebSend [192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600 ENDON
```
... wobei
- `Button1` ist der Name des Tasters, mit dem das Ereignis verknüpft wird
- `State=2` definiert, dass das Ereignis beim Zustandswechsel (Toggle) ausgelöst wird
- `192.168.0.1` die IP-Adresse ist, unter welcher der *Smart Appliance Enabler* erreichbar ist
- `F-xxxxxxxx-xxxxxxxxxxxx-xx` die Appliance-ID ist
- `3600` die gewünschte Laufzeit in Sekunden ist
- `21600` der späteste Zeitpunkt in Sekunden ab jetzt ist, zu dem die Laufzeit beendet sein muss

Diese Regel muss jetzt noch aktiviert werden:
```
Rule1 1
```

Bei Drücken des Tasters erfolgt jetzt die gewünschte Laufzeitanforderung:
```
11:01:28 APP: Knopf1 Mehrfachdruck 1
11:01:29 WIF: Prüfe Verbindung...
11:01:29 WIF: verbunden
11:01:29 RUL: BUTTON1#STATE=2 performs "WebSend [192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600"
11:01:29 SRC: Rule
11:01:29 CMD: Gruppe 0, Index 1, Befehl "WEBSEND", Daten "[192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600"
11:01:29 RSL: RESULT = {"WebSend":"Done"}
```

Bei Bedarf kann die Regel jederzeit wieder deaktiviert werden:
```
Rule1 0
```
