# Fragen / Probleme

Wenn sich der *Smart Appliance Enabler* nicht starten lässt oder der *SMA Home Manager* die vom *Smart Appliance Enabler* verwalteten Geräte nicht finden kann, sollen folgende Punkte geprüft werden:

## Läuft der Smart Appliance Enabler?
Mit folgedem Befehl läßt sich überprüfen, ob der *Smart Appliance Enabler* läuft:
```console
pi@raspberrypi:~ $ sudo systemctl status smartapplianceenabler.service
● smartapplianceenabler.service - Smart Appliance Enabler
   Loaded: loaded (/lib/systemd/system/smartapplianceenabler.service; enabled; vendor preset: enabled)
   Active: active (running) since Mon 2019-12-23 18:06:40 CET; 1min 30s ago
  Process: 3890 ExecStart=/opt/sae/smartapplianceenabler start (code=exited, status=0/SUCCESS)
 Main PID: 3897 (sudo)
    Tasks: 34 (limit: 2200)
   Memory: 83.1M
   CGroup: /system.slice/smartapplianceenabler.service
           ├─3897 sudo -u sae /usr/bin/java -Dsae.discovery.disable=true -Djava.awt.headless=true -Xmx256m -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid -Dsae.home=/opt/sae -jar /op
           └─3903 /usr/bin/java -Dsae.discovery.disable=true -Djava.awt.headless=true -Xmx256m -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid -Dsae.home=/opt/sae -jar /opt/sae/SmartA

Dec 23 18:05:56 raspberrypi systemd[1]: Starting Smart Appliance Enabler...
Dec 23 18:05:56 raspberrypi smartapplianceenabler[3890]: Starting smartapplianceenabler (/opt/sae/SmartApplianceEnabler-1.5.1.war)
Dec 23 18:05:56 raspberrypi sudo[3897]:     root : TTY=unknown ; PWD=/opt/sae ; USER=sae ; COMMAND=/usr/bin/java -Dsae.discovery.disable=true -Djava.awt.headless=true -Xmx256m -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfil
Dec 23 18:05:56 raspberrypi sudo[3897]: pam_unix(sudo:session): session opened for user sae by (uid=0)
Dec 23 18:06:40 raspberrypi smartapplianceenabler[3890]: ............................................
Dec 23 18:06:40 raspberrypi smartapplianceenabler[3890]: Started
Dec 23 18:06:40 raspberrypi systemd[1]: Started Smart Appliance Enabler.
```
Der *Smart Appliance Enabler* läuft, wenn in der mit `Active:` beginnenden Zeile steht `active (running)`.

## Log
Der *Smart Appliance Enabler* schreibt seine Log-Daten in das Verzeichnis ```/tmp```, wobei die Dateinamen mit ```rolling``` beginnen gefolgt vom jeweilgen Datum.
Mit dem folgenden Befehl kann man die Log-Dtei _live_ verfolgen, d.h. neue Einträge erscheinen automatisch:
```
pi@raspi ~ $ tail -f /tmp/rolling-2019-12-30.log
2019-12-30 11:46:51,367 DEBUG [Timer-0] d.a.s.u.TimestampBasedCache [TimestampBasedCache.java:62] F-00000001-000000000014-00: cache=Power added value=1.0 timestamp=1577702811300  removed/total: 1/6
2019-12-30 11:46:51,408 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000015-00: Executing timer task name=PollEnergyMeter id=4447659
2019-12-30 11:46:51,411 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000015-00: Executing timer task name=PollPowerMeter id=29541189
2019-12-30 11:46:51,412 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000015-00: Sending GET request url=http://trockner/cm?cmnd=Status%208
2019-12-30 11:46:51,501 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:160] F-00000001-000000000015-00: Response code is 200
2019-12-30 11:46:51,535 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:86] F-00000001-000000000015-00: url=http://trockner/cm?cmnd=Status%208 httpMethod=GET data=null path=null
2019-12-30 11:46:51,536 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:89] F-00000001-000000000015-00: Response: {"StatusSNS":{"Time":"2019-12-30T11:46:52","ENERGY":{"TotalStartTime":"2019-08-18T11:21:09","Total":1.580,"Yesterday":0.008,"Today":0.004,"Power":2,"ApparentPower":49,"ReactivePower":49,"Factor":0.04,"Voltage":233,"Current":0.209}}}
2019-12-30 11:46:51,539 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:58] F-00000001-000000000015-00: value=2.0 protocolHandlerValue={"StatusSNS":{"Time":"2019-12-30T11:46:52","ENERGY":{"TotalStartTime":"2019-08-18T11:21:09","Total":1.580,"Yesterday":0.008,"Today":0.004,"Power":2,"ApparentPower":49,"ReactivePower":49,"Factor":0.04,"Voltage":233,"Current":0.209}}} valueExtractionRegex=,.Power.:(\d+) extractedValue=2
2019-12-30 11:46:51,541 DEBUG [Timer-0] d.a.s.u.TimestampBasedCache [TimestampBasedCache.java:62] F-00000001-000000000015-00: cache=Power added value=2.0 timestamp=1577702811412  removed/total: 1/6
2019-12-30 11:46:51,713 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000019-00: Executing timer task name=PollPowerMeter id=10020238
2019-12-30 11:46:51,716 DEBUG [Timer-0] d.a.s.m.ModbusSlave [ModbusSlave.java:76] F-00000001-000000000019-00: Connecting to modbus modbus@127.0.0.1:502
2019-12-30 11:46:51,776 DEBUG [Timer-0] d.a.s.m.e.ReadFloatInputRegisterExecutorImpl [ReadInputRegisterExecutor.java:57] F-00000001-000000000019-00: Input register=52 value=[0, 0, 0, 0]
2019-12-30 11:46:51,779 DEBUG [Timer-0] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:196] F-00000001-000000000019-00: Float value=0.0
```
Um nur die Einträge für ein bestimmtes Gerät zu sehen, eignet sich der folgende Befehl:
```
pi@raspi ~ $ tail -f /tmp/rolling-2019-12-30.log | grep --line-buffered F-00000001-000000000019-00
2019-12-30 11:41:41,109 DEBUG [http-nio-8080-exec-8] d.a.s.s.w.SempController [SempController.java:221] F-00000001-000000000019-00: Received control request: on=false
2019-12-30 11:41:41,111 DEBUG [http-nio-8080-exec-8] d.a.s.a.Appliance [Appliance.java:379] F-00000001-000000000019-00: Setting appliance state to OFF
2019-12-30 11:41:41,112 INFO [http-nio-8080-exec-8] d.a.s.c.e.ElectricVehicleCharger [ElectricVehicleCharger.java:370] F-00000001-000000000019-00: Switching off
2019-12-30 11:41:41,114 DEBUG [http-nio-8080-exec-8] d.a.s.c.e.ElectricVehicleCharger [ElectricVehicleCharger.java:558] F-00000001-000000000019-00: Stop charging process
2019-12-30 11:41:41,116 DEBUG [http-nio-8080-exec-8] d.a.s.m.EVModbusControl [EVModbusControl.java:210] F-00000001-000000000019-00: Stop charging
2019-12-30 11:41:41,117 DEBUG [http-nio-8080-exec-8] d.a.s.u.RequestCache [RequestCache.java:66] F-00000001-000000000019-00: Cache cleared
2019-12-30 11:41:41,119 DEBUG [http-nio-8080-exec-8] d.a.s.m.ModbusSlave [ModbusSlave.java:76] F-00000001-000000000019-00: Connecting to modbus wallbox@wallbox:502
```

Der *Log-Level* steht defaultmäßig auf ```debug```, um im Fehlerfall detaillierte Informationen zu haben. Falls weniger geloggt werden soll, kann der Log-Level auf ```info``` geändert werden in der Datei ```/opt/sae/logback-spring.xml```:
```
...
<logger name="de.avanux" level="debug" additivity="false">
...
```

## Historie des SmartApplianceEnabler-Service
```console
sae@raspi ~ $ sudo journalctl -u smartapplianceenabler.service
-- Logs begin at Sun 2019-12-22 18:21:01 CET, end at Mon 2019-12-23 17:52:01 CET. --
```

## Version des Smart Appliance Enabler
Direkt nach dem Start schreibt der *Smart Appliance Enabler* die Version in die Log-Datei:
```
2018-04-08 10:17:29,973 INFO [main] d.a.s.Application [Application.java:45] Running version 1.2.0-SNAPSHOT 2017-12-23 19:16
```

## Netzwerkverbindung zwischen Smart Appliance Enabler und Sunny Home Manager
Home Manager auf den *Smart Appliance Enabler* müssen sich im gleichen Netz befinden!
Wenn der Log-Level mindestens auf DEBUG gesetzt wurde, kann man in der Log-Datei sehen, wenn der Home Manager auf den *Smart Appliance Enabler* zugreift:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```

## Datenaustausch zwischen Smart Appliance Enabler und Sunny Home Manager
Bei Problemen wird oft nur der offensichtliche Fehler betrachtet (_"Im Sunny Portal werden keine Messwerte angezeigt"_). Dabei läßt sich relativ leicht herausfinden, ob der Fehler im *Smart Appliance Enabler* liegt oder am Sunny Home Manager bzw. Sunny Portal.

Die Kommunikation zwischen den beiden besteht darin, dass der Sunny Home Manager **alle 60 Sekunden** den *Smart Appliance Enabler* über die **URL http://raspi:8080/semp (wobei "raspi" durch den Hostnamen oder IP-Adresse des Raspberry Pi zu ersetzen ist)** aufruft. Diese URL kann man auch in einen ganz normalen Web-Browser eingeben und sich anzeigen lassen, welche Informationen der *Smart Appliance Enabler* an den Sunny Home Manager überträgt.

Die URL liefert ein SEMP-XML-Dokument, in der für jedes Gerät ein *DeviceInfo* und ein *DeviceStatus* enthalten ist. Optional können auch *PlanningRequest* enthalten sein.

Jedes Gerät wird über eine **DeviceId** identifiziert - das ist die (Appliance-) ID, die beim Anlegen des Gerätes eingetragen werden musste.

Bei der Fehlersuche wird man also denjenigen *DeviceStatus* betrachten, in dem die *DeviceId* des problematischen Gerätes enthalten ist.

Wenn **Zählerwerte nicht im Sunny Portal** angezeigt werden, müssen zwei Dinge geprüft werden:
- Im *DeviceStatus* unter *PowerInfo* muss bei *AveragePower* die aktuelle Leistungsaufnahme enthalten sein, die größer als 0 sein muss
- Im *DeviceStatus* muss der *Status* auf _On_ stehen, sonst werden die Leistungswerte vom Sunny Home Manager ignoriert

Wenn Geräte vom Sunny Home Manager **nicht geschaltet werden**, müssen folgende Dinge geprüft werden:
- Im *DeviceStatus* muss *EMSignalsAccepted* auf _true_ stehen
- Der Sunny Home Manager schickt nur einen Schaltbefehl, wenn ein *PlanningRequest* mit einem *Timeframe* für die betreffene *DeviceId* existiert. Ein Schaltbefehl kann nur kommen, wenn *EarliestStart* den Wert 0 hat!

## Analyse der Log Dateien des SEMP Moduls im Sunny Home Manager
Siehe https://www.photovoltaikforum.com/thread/104060-ger%C3%A4te-mit-home-manager-koppeln-via-semp-ethernet/?postID=1396300#post1396300

## Fehler "Timeframe for unknown or unrequested device" im Log des SEMP Moduls vom Sunny Home Manager
Der Fehler tritt dann auf wenn sich die interne UID des *Smart Appliance Enabler* ändert. Dies passiert immer dann wenn man die Hardware auf der der *Smart Appliance Enabler* läuft austauscht (zB neuer Raspberry Pi), da sie von der Hardware-MAC-Adresse der ersten Netzwerkstelle abhängt. Einfachste Abhilfe ist ein Neustart des Sunny Home Managers, danach werden die Timeframes wieder akzeptiert.

## Anwender-Forum
Fragen zur Verwendung des *Smart Appliance Enabler* sollten im SEMP-Thread des *photovoltaik-forums* im SMA Herstellerbereich gestellt werden: https://www.photovoltaikforum.com/geraete-mit-home-manager-koppeln-via-semp-ethernet-t104060.html.

Bitte keine Fragen direkt per Email an mich stellen! Wenn die Fragen im Forum gestellt werden, haben alle was davon und die Chance auf Antworten ist deutlich größer.

## Fehler melden
Bei Fehlern im *Smart Appliance Enabler* sollte ein [Issue](https://github.com/camueller/SmartApplianceEnabler/issues) erstellt werden.
