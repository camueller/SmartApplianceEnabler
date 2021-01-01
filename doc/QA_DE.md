# Fragen / Probleme und Antworten

## Fragen / Probleme
### Sunny Portal
- Verbraucher läßt sich nicht im Sunny Portal hinzufügen ---> [SP1](#sp1)
- Leistung des Verbrauchers wird nicht im Sunny Portal angezeigt ---> [SEMP2](#semp2)
- Wie kann ich den Verbraucher im Sunny Portal schalten? ---> [SP2](#sp2)

### Sunny Home Manager
- Das Gerät wird nicht eingeschaltet ---> [SEMP3](#semp3), [SAE4](#sae4)

### Smart Appliance Enabler
- Läuft der *Smart Appliance Enabler*? ---> [SAE1](#sae1)
- Fehler beim Start des *Smart Appliance Enabler* ---> [SAE2](#sae2)


## Antworten

### SP1
... fehlt noch

### SP2
Geräte, die über den *Smart Appliance Enabler* verwaltet werden, sind aus Sicht des *Sunny Home Manager* **Verbraucher**. Einige Parameter dieser Verbaucher (z.B. Anteil der PV-Energie) können über das *Sunny Portal* konfiguriert werden, aber geschaltet werden kann das Gerät nicht über das *Sunny Portal*. Stattdessen kann das Gerät aber über [Status-Seite](Status_DE.md) der Web-Oberfläche des *Smart Appliance Enabler* geschaltet werden.

### SEMP1
Wenn der *Sunny Home Manager* den *Smart Appliance Enabler* im Netz gefunden hat, fragt er nachfolgend dessen Status **alle 60 Sekunden** ab. Diese Abfragen werden der Log-Datei des *Smart Appliance Enabler* protokolliert und sehen so aus:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```
Wenn diese Einträge nicht vorhanden sind, funktioniert die Kommunikation zwischen *Sunny Home Manager* und *Smart Appliance Enabler* nicht.

Folgende Punkte prüfen:
- Funktioniert das [SEMP-Protokoll](SEMP_DE.md) korrekt?
- Ist der *Smart Appliance Enabler* gestartet?
- Läßt sich der Host mit *Smart Appliance Enabler* pingen?
- Läßt sich der *Sunny Home Manager* pingen?

### SEMP2
Zunächst muss sichergestellt sein, dass der *Smart Appliance Enabler* vom *Sunny Home Manager* gefunden wird ---> [SEMP1](#semp1)

Wenn Zählerwerte nicht im *Sunny Portal* angezeigt werden, müssen folgende Werte in der [SEMP-Schnittstelle](SEMP_DE.md) geprüft werden:
- im `DeviceStatus` unter `PowerInfo` muss `AveragePower` grösser als 0 sein. Falls das nicht so ist, kann die Leisungsaufnahme möglicherweise nicht bestimmt werden. ---> [SAE3](#sae3)
- im `DeviceStatus` muss der `Status` den Wert `On` haben, sonst werden die Leistungswerte vom *Sunny Home Manager* ignoriert

### SEMP3
Zunächst muss sichergestellt sein, dass der *Smart Appliance Enabler* vom *Sunny Home Manager* gefunden wird ---> [SEMP1](#semp1)

Wenn Geräte vom *Sunny Home Manager* nicht geschaltet werden, muss das [SEMP-XML](SEMP_DE.md#xml) geprüft werden:
- im `DeviceStatus` muss `EMSignalsAccepted` auf `true` stehen
- es muss ein `PlanningRequest` mit einem `Timeframe` existieren, bei dem
  - `EarliestStart` den Wert `0` hat
  - `minRunningTime` grösser als `0` ist  

Sind diese Vorausetzungen erfüllt, **kann** der *Sunny Home Manager* einen Einschaltbefehl jederzeit senden.

**Spätestens** dann wird er einen Einschaltbefehl senden, wenn im `Timeframe` des `PlanningRequest` der Wert von `LatestEnd` nur unwesentlich (ca. 60-300) grösser ist, als der Wert von `minRunningTime`.

Wird ein Schaltbefehl vom *Sunny Home Manager*, [wird dafür ein Log-Eintrag erzeugt](Logging_DE.md#schaltbefehl-vom-sunny-home-manager).

### SAE1
Der Befehl zur Prüfung, ob der *Smart Appliance Enabler* läuft, findet sich in der [Installationsanleitung](Installation_DE.md#status) bzw. in der [Docker-Anleitung](Docker_DE.md#status-des-containers).

### SAE2
Falls sich der *Smart Appliance Enabler* nicht starten läßt und man keine Hinweise im [Log](Logging_DE.md) findet, ist es sinnvoll, ihn testweise in der aktuellen Shell zu starten. Dadurch kann man etwaige Fehler auf der Konsole sehen. Die Shell muss dabei dem User gehören, der auch sonst für den *Smart Appliance Enabler*-Prozess verwendet wird - normalerweise ist das der User `sae`.
Der Befehl dafür entspricht genau dem, was sonst das Start-Script macht und sieht wie folgt aus:
```console
sae@raspberrypi:~ $ /usr/bin/java -Djava.awt.headless=true -Xmx256m -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid -Dsae.home=/opt/sae -jar /opt/sae/SmartApplianceEnabler-1.6.1.war
```  
Die Versionsnummer im Namen der war-Datei muss natürlich entsprechend der verwendeten Version angepasst werden!

### SAE3
Die Leistungaufname des Gerätes, die an den *Sunny Home Manager* übermittelt wird, wird über den im *Smart Appliance Enabler* konfigurierten Zähler bestimmt. In Abhängkeit von dessen Typ kann man im Log die Leistungaufname sehen:
- [S0](SOMeter_DE.md#log)
- [HTTP](HttpMeter_DE.md#log)
- [Modbus](ModbusMeter_DE.md#log)

### SAE4
Wenn ein Schaltbefehl vom *Sunny Home Manager* empfangen wird, wird dieser an den für das Gerät im *Smart Appliance Enabler* konfigurierten Schalter weitergegeben. In Abhängigkeit In Abhängkeit von dessen Typ kann man im Log den Schaltbefehl sehen:  
- [GPIO-Switch](GPIOSwitch_DE.md#log)
- [HTTP](HttpSwitch_DE.md#log)
- [Modbus](ModbusSwitch_DE.md#log)
