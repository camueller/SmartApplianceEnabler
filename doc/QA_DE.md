# Fragen / Probleme und Antworten

## Fragen / Probleme
### Sunny Portal
- Verbraucher lässt sich nicht im Sunny Portal hinzufügen ---> [SEMP1](#semp1), [SP1](#sp1)
- Leistung des Verbrauchers wird nicht im Sunny Portal angezeigt ---> [SEMP2](#semp2)
- Wie kann ich den Verbraucher im Sunny Portal schalten? ---> [SP2](#sp2)
- Im Anlagenlogbuch erscheinen oft Einträge wie z.B.: "EM-Gateway nicht gefunden", "EM-Gerät nicht gefunden". ---> [SP3](#sp3)

### Sunny Home Manager
- Gerät wird nicht eingeschaltet ---> [SEMP3](#semp3), [SEMP4](#semp4), [SEMP5](#semp5), [SAE4](#sae4), [SAE7](#sae7)
- Gerät wird unerwartet eingeschaltet ---> [SEMP4](#semp4)

### Smart Appliance Enabler
- Läuft der *Smart Appliance Enabler*? ---> [SAE1](#sae1)
- Fehler beim Start des *Smart Appliance Enabler* ---> [SAE2](#sae2)
- Wo kann man einen anderen Port als 8080 einstellen? ---> [SAE5](#sae5)
- Wie kann man die Konfiguration des *Smart Appliance Enabler* sichern und wiederherstellen, beispielsweise für eine Neuinstallation? ---> [SAE6](#sae6)

## Antworten

### SP1
Neue Geräte können nur hinzugefügt werden, solange die [maximale Anzahl von Geräten nicht überschritten wird](SunnyPortal_DE.md#max-devices).

Um den *Sunny Home Manager* zu zwingen, erneut nach neuen Geräten lokalen Netz zu suchen, kann man diesen kurz stromlos machen. Wenn er wieder vollsändig gestartet ist, muss im *Sunny Portal* [erneut der Prozess zum Hinzufügen neuer Geräte durchlaufen werden](SunnyPortal_DE.md).

### SP2
Geräte, die über den *Smart Appliance Enabler* verwaltet werden, sind aus Sicht des *Sunny Home Manager* **Verbraucher**. Einige Parameter dieser Verbaucher (z.B. Anteil der PV-Energie) können über das *Sunny Portal* konfiguriert werden, aber geschaltet werden kann das Gerät nicht über das *Sunny Portal*. Stattdessen kann das Gerät aber über die [Status-Seite](Status_DE.md) der Web-Oberfläche des *Smart Appliance Enabler* geschaltet werden.

### SP3
Im Anlangenlogbuch wird das Gateway und die Geräte dauernd nicht gefunden und anschließend wieder gefunden. Wenn sich dieser Prozess öfters wiederholt, liegt es meistens daran, dass eins der anzusteuernden (mit WLAN eingebundenen) Geräte einen schlechten Empfang hat und somit im ganzen Prozess einen Timeout produziert. Durch WLAN-Repeater oder Umstellung auf kabelgebundene Kommunikation lässt sich dieses Problem in der Regel beheben.

### SEMP1
Wenn der *Sunny Home Manager* den *Smart Appliance Enabler* im Netz gefunden hat, fragt er nachfolgend dessen Status **alle 60 Sekunden** ab. Diese Abfragen werden in der Log-Datei des *Smart Appliance Enabler* protokolliert und sehen so aus:

```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```

Wenn diese Einträge nicht vorhanden sind, funktioniert die Kommunikation zwischen *Sunny Home Manager* und *Smart Appliance Enabler* nicht.

Folgende Punkte prüfen:
- Funktioniert das [SEMP-Protokoll](SEMP_DE.md) und ist insbesondere die [SEMP-URL](SEMP_DE.md#url) korrekt?
- Ist der *Smart Appliance Enabler* gestartet?
- Läßt sich der Host mit *Smart Appliance Enabler* pingen?
- Läßt sich der *Sunny Home Manager* pingen?

### SEMP2
Zunächst muss sichergestellt sein, dass der *Smart Appliance Enabler* vom *Sunny Home Manager* gefunden wird ---> [SEMP1](#semp1)

Wenn Zählerwerte nicht im *Sunny Portal* angezeigt werden, müssen folgende Werte in der [SEMP-Schnittstelle](SEMP_DE.md#xml) geprüft werden:
- im `DeviceStatus` unter `PowerInfo` muss `AveragePower` grösser als 0 sein. Falls das nicht so ist, kann die Leistungsaufnahme möglicherweise nicht bestimmt werden. ---> [SAE3](#sae3)
- im `DeviceStatus` muss der `Status` den Wert `On` haben, sonst werden die Leistungswerte vom *Sunny Home Manager* ignoriert

### SEMP3
Zunächst muss sichergestellt sein, dass der *Smart Appliance Enabler* vom *Sunny Home Manager* gefunden wird ---> [SEMP1](#semp1)

Der *Sunny Home Manager* wird nur dann einen Einschaltbefehl für eine Gerät senden, wenn ihm ein (Laufzeit-/Energie-) Bedarf gemeldet wurde. Der *Smart Appliance Enabler* macht das, wenn 
- ein [Zeitplan angelegt wurde](Schedules_DE.md), der **aktiv** und **zutreffend (Wochentag und Zeit)** ist
- oder durch [Klick auf das grüne Ampel-Licht](Status_DE.md#click-green) ein **ad-hoc Bedarf** entsteht

Ob dem *Sunny Home Manager* ein Bedarf gemeldet wird, kann im [SEMP-XML](SEMP_DE.md#xml) geprüft werden:
- im `DeviceStatus` muss `EMSignalsAccepted` auf `true` stehen
- es muss ein `PlanningRequest` mit einem `Timeframe` existieren, bei dem
  - `EarliestStart` den Wert `0` hat
  - `minRunningTime` (bzw. `minEnergy` bei Wallboxen) grösser als `0` ist, wenn der Verbraucher laufen **muss**. Wenn er laufen **kann** (zur Nutzung von Überschussenergie) muss `minRunningTime` (bzw. `minEnergy` bei Wallboxen) gleich `0` sein

Sind diese Vorausetzungen erfüllt, **kann** der *Sunny Home Manager* einen Einschaltbefehl jederzeit senden.

Wenn der Verbraucher laufen **muss**, wird er **spätestens** dann einen Einschaltbefehl senden, wenn im `Timeframe` des `PlanningRequest` der Wert von `LatestEnd` nur unwesentlich (ca. 60-300) grösser ist, als der Wert von `minRunningTime`.

Ob ein Schaltbefehl vom *Sunny Home Manager* empfangen wird, kann man [im Log prüfen](Control_DE.md#control-request). Wenn sich ein entsprechender Log-Eintrag findet und trotzdem das Gerät nicht geschaltet wird, liegt es nicht am *Sunny Home Manager*.  ---> [SAE4](#sae4)

### SEMP4
Wenn Schaltvorgänge nicht den Erwartungen entsprechen sollte zunächst geprüft werden, welche Informationen der *Smart Appliance Enabler* dem *Sunny Home Manager* via [SEMP-Schnittstelle](SEMP_DE.md#xml) unmittelbar vor dem unerwarteten Schaltvorgang übermittelt hat. Dazu muss man in der [Log-Datei](Logging_DE.md) zuerst den [unerwarteten Schaltbefehl finden](#sae4). Von dieser Stelle sucht mach **rückwärts** nach `Reporting device status from control`. Diese Zeile markiert den Beginn einer Abfrage durch den *Sunny Home Manager* und beinhaltet
- den *aktuellen Schaltzustand* (`status=...`)
- die Information, ob aktuell Schalbefehle akzeptiert werden (`eMSignalsAccepted=`)
- Anforderungen für Laufzeit oder Energie (`Timeframe added to PlanningRequest`)

*Beispiel*

Nachfolgendes Log zeigt, dass das Gerät nicht eingeschaltet ist (`status=Off`), aber Schaltbefehle akzeptiert werden (`eMSignalsAccepted=true`). Außerdem wird eine Energieanforderung gestellt für 0Wh (`0Wh`) bis zu 9360Wh (`172096s`), die ab sofort (`0s`) bis in 172096 Sekunden (`172096s`) zu erfüllen ist.
```
2024-03-07 18:27:55,157 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:336] F-00000001-000000000019-00: Reporting device status from control
2024-03-07 18:27:55,157 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:345] F-00000001-000000000019-00: DeviceStatus{eMSignalsAccepted=true, status=Off, errorCode=null}
2024-03-07 18:27:55,157 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:351] F-00000001-000000000019-00: Reporting power info from meter.
2024-03-07 18:27:55,158 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:365] F-00000001-000000000019-00: PowerInfo{averagePower=0, minPower=null, maxPower=null, timestamp=0, averagingInterval=60}
2024-03-07 18:27:55,158 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:435] F-00000001-000000000019-00: Timeframe created: 0s-172096s:0Wh/9360Wh
2024-03-07 18:27:55,159 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:387] F-00000001-000000000019-00: Timeframe added to PlanningRequest: 0s-172096s:0Wh/9360Wh
```

### SEMP5
Aus Sicht von SMA ist bei der Fehleranalyse relevant, welche Informationen der *Sunny Home Manager* erhalten hat. Dessen [SEMP-Logs lassen sich ebenfalls abrufen](ConnectionAssist_DE.md) und sollten für eventuelle Service-Anfragen bei SMA verwendet werden. Mit Logs des *Smart Appliance Enabler* wird man sich bei SMA nicht auseinandersetzen.

### SAE1
Der Befehl zur Prüfung, ob der *Smart Appliance Enabler* läuft, findet sich in der [Installationsanleitung](InstallationManual_DE.md#status) bzw. in der [Docker-Anleitung](Docker_DE.md#container-status).

### SAE2
Falls sich der *Smart Appliance Enabler* nicht starten läßt und man keine Hinweise im [Log](Logging_DE.md) findet, ist es sinnvoll, ihn testweise in der aktuellen Shell zu starten. Dadurch kann man etwaige Fehler auf der Konsole sehen. Die Shell muss dabei dem User gehören, der auch sonst für den *Smart Appliance Enabler*-Prozess verwendet wird - normalerweise ist das der User `sae`.

Der Befehl dafür entspricht genau dem, was sonst das Start-Script macht und sieht wie folgt aus:

```bash
sae@raspberrypi:~ $ /usr/bin/java \
    -Djava.awt.headless=true \
    -Xmx256m \
    -Duser.language=de \
    -Duser.country=DE \
    -DPIGPIOD_HOST=localhost \
    -Dlogging.config=/opt/sae/logback-spring.xml \
    -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid \
    -Dsae.home=/opt/sae \
    -jar /opt/sae/SmartApplianceEnabler-2.1.0.war
```
Die Versionsnummer im Namen der war-Datei muss natürlich entsprechend der verwendeten Version angepasst werden!

### SAE3
Die Leistungaufname des Gerätes, die an den *Sunny Home Manager* übermittelt wird, wird über den im *Smart Appliance Enabler* [konfigurierten Zähler bestimmt](Meter_DE.md). In Abhängkeit von dessen Typ kann man im Log die Leistungaufname sehen.

### SAE4
Wenn ein Schaltbefehl vom *Sunny Home Manager* empfangen wird, wird dieser an den für das Gerät im *Smart Appliance Enabler* konfigurierten Schalter weitergegeben. In Abhängkeit von dessen Typ kann man im Log den Schaltbefehl sehen:  
- [GPIO-basierter Schalter](GPIOSwitch_DE.md#log)
- [HTTP-basierter Schalter](HttpSwitch_DE.md#log)
- [Modbus-basierter Schalter](ModbusSwitch_DE.md#log)
- [PWM-Schalter](PwmSwitch_DE.md#log)
- [Wallbox](EVCharger_DE.md#log)

### SAE5
In der [Server-Konfiguration](ConfigurationFiles_DE.md#user-content-etc-default-smartapplianceenabler) kann der Standardport geändert werden.

### SAE6
Die gesamte Konfiguration des *Smart Appliance Enabler* ist in [zwei XML-Dateien enthalten](ConfigurationFiles_DE.md). Diese kann man [auf einen anderen Computer sichern](ConfigurationFiles_DE.md#user-content-scp).

### SAE7
Ist für das Gerät ein [Zeitplan zur Nutzung von Überschussenergie](Schedules_DE.md) konfiguriert, wird der *Sunny Home Manager* nur dann einen Einschaltbefehl senden, wenn genügend Überschuss vorhanden ist und erwartbar ist, dass dies entsprechend der konfigurierten Bedingungen auch so bleibt. Für maximale Chancen auf einen Einschaltbefehl sollten diese Bedingungen anfangs nicht restriktiv sein. Deshalb sollte mit folgenden Werten gestartet werden:
- [Appliance-Konfiguration: Min. Einschaltdauer](Appliance_DE.md) leer lassen
- [Appliance-Konfiguration: Unterbrechnung erlaubt](Appliance_DE.md) aktivieren
 