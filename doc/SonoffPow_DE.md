# Sonoff Pow

Der [Sonoff Pow der Firma ITead](https://www.itead.cc/sonoff-pow.html) ist ein preisgünstiger Schalter, der mit dem WLAN verbunden ist und auch den aktuellen Stromverbrauch des geschalteten Gerätes messen kann.

Der Sonoff Pow basiert auf einm ESP8266-Mikrocontroller, dessen Originial-Firmware mit nur zusammen mit der eWeLink-App des Herstellers verwendet werden kann. Zur Verwendung mit dem *Smart Appliance Enabler* muss die alternative Firmware [Sonoff-Tasmota](https://github.com/arendst/Sonoff-Tasmota) geflasht werden, durch die der *Smart Appliance Enabler* über HTTP mit Sonoff Pow kommunizieren kann. Im Tasmota-Wiki ist beschrieben, wie zum Flashen der Firmware und zur Konfiguration von Tasmota vorgegangen werden muss. Punkte, die dabei leicht übersehen werden können, sind:
- Nach dem Flashen muss über die Web-Oberfläche der Typ des Sonoff-Gerätes (6 für Sonoff Pow) gesetzt werden, sonst wird die aktuelle Leistungsaufnahme falsch gemessen
- Vor _jedem_ Flashen nach Änderungen an user_config.h muss der Wert für CFG_HOLDER geändert werden, sonst bleiben die Änderungen ohne Wirkung!

Ich konnte das Flashen der Tasmota-Firmware mit der Arduino-IDE 1.6.11 erfolgreich durchführen.

## WLAN konfigurieren
Zur Konfiguration des WLAN wird ein Android-Gerät benötigt, auf dem die App [ESP8266 SmartConfig](https://play.google.com/store/apps/details?id=com.cmmakerclub.iot.esptouch) installiert und das in dem gewünschten WLAN eingebucht ist. Nach dem Starten der App _ESP8266 SmartConfig_ sollte die SSID des WLAN angezeigt werden. In das Feld _Passwort_ muss das Passwort dieses WLANs eingegeben werden.

Am Sonoff Pow muss der Taster dreimal hintereinander kurz gedrück werden, woraufhin die blaue LED schnell blinkt und damit anzeigt, dass der *Wifi SmartConfig-Modus* aktiv ist.

In der App _ESP8266 SmartConfig_ sollte jetzt die Schaltfläche _Confirm_ betätigt werden. Die blaue LED sollte dann etwas länger ausgehen, einmal etwas länger angehen und dann noch zweimal kurz blinken, bevor sie dauerhaft ausgeht. Danach sollte man auf seinem Router nachsehen, um die IP-Adresse herauszufinden, unter dem der Sonoff Pow im WLAN erreichbar ist.

## Sonoff Pow als Stromzähler
Die aktuelle Leistungsaufnahme des Sonoff Pow kann wie folgt abgefragt werden:
```
curl http://192.168.1.1/cm?cmnd=Status%208
STATUS8 = {"StatusPWR":{"Total":0.000, "Yesterday":0.000, "Today":0.000, "Power":27, "Factor":0.94, "Voltage":234, "Current":0.122}}
```
Damit der *Smart Appliance Enabler* in dieser JSON-Antwort den eigentlichen Wert für die Leistungsaufnahme findet (hier: 27W), muss als Regulärer Ausdruck ```.*Power.:(\d+).*``` angegeben werden:
```
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <HttpElectricityMeter url="http://192.168.1.1/cm?cmnd=Status%208" powerValueExtractionRegex=".*Power.:(\d+).*"/>
    </Appliance>
</Appliances>
```
In der Log-Datei ```/var/log/smartapplianceenabler.log``` sollten sich dann für jede Abfrage folgende Zeilen finden:
```
2017-06-03 18:39:55,125 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:101] F-00000001-000000000001-00: Sending HTTP request
2017-06-03 18:39:55,125 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:102] F-00000001-000000000001-00: url=http://192.168.1.1/cm?cmnd=Status%208
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:103] F-00000001-000000000001-00: data=null
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:104] F-00000001-000000000001-00: contentType=null
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: username=null
2017-06-03 18:39:55,126 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:106] F-00000001-000000000001-00: password=null
2017-06-03 18:39:55,146 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:118] F-00000001-000000000001-00: Response code is 200
2017-06-03 18:39:55,147 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:119] F-00000001-000000000001-00: HTTP response: STATUS8 = {"StatusPWR":{"Total":0.000, "Yesterday":0.000, "Today":0.000, "Power":26, "Factor":0.94, "Voltage":234, "Current":0.122}}
2017-06-03 18:39:55,147 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:120] F-00000001-000000000001-00: Power value extraction regex: .*Power.:(\d+).*
2017-06-03 18:39:55,153 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:119] F-00000001-000000000001-00: Power value extracted from HTTP response: 26
```

## Sonoff Pow als Schalter
Der Schaltzustand des Sonoff Pow kann wie folgt geändert werden:

_Einschalten_
```
curl http://192.168.1.1/cm?cmnd=Power%20On
```

_Ausschalten_
```
curl http://192.168.1.1/cm?cmnd=Power%20Off
```

Entsprechend sieht die Konfiguration für den *Smart Appliance Enabler* aus:
```
<Appliances ...>
    <Appliance ...>
        <HttpSwitch onUrl="http://192.168.1.1/cm?cmnd=Power%20On" offUrl="http://192.168.1.1/cm?cmnd=Power%20Off"/>
    </Appliance>
</Appliances>
```
In der Log-Datei ```/var/log/smartapplianceenabler.log``` sollten sich dann für jede Schaltvorgang folgende Zeilen finden:
```
2017-06-03 18:39:52,143 DEBUG [http-nio-8080-exec-1] d.a.s.s.w.SempController [SempController.java:192] F-00000001-000000000001-00: Received control request
2017-06-03 18:39:52,145 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:101] F-00000001-000000000001-00: Sending HTTP request
2017-06-03 18:39:52,145 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:102] F-00000001-000000000001-00: url=http://192.168.1.1/cm?cmnd=Power%20On
2017-06-03 18:39:52,145 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:103] F-00000001-000000000001-00: data=null
2017-06-03 18:39:52,145 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:104] F-00000001-000000000001-00: contentType=null
2017-06-03 18:39:52,145 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: username=null
2017-06-03 18:39:52,145 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:106] F-00000001-000000000001-00: password=null
2017-06-03 18:39:52,163 DEBUG [http-nio-8080-exec-1] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:118] F-00000001-000000000001-00: Response code is 200
2017-06-03 18:39:52,163 DEBUG [http-nio-8080-exec-1] d.a.s.a.Appliance [Appliance.java:318] F-00000001-000000000001-00: Control state has changed to on: runningTimeMonitor=not null
2017-06-03 18:39:52,165 DEBUG [http-nio-8080-exec-1] d.a.s.s.w.SempController [SempController.java:214] F-00000001-000000000001-00: Setting appliance state to ON
```
