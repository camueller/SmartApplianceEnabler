# Edimax SP-2101W

Der [Edimax SP-2101W](http://www.edimax-de.eu/edimax/merchandise/merchandise_detail/data/edimax/de/home_automation_smart_plug/sp-2101w) ist ein Steckdosenadapter, der mit dem WLAN verbunden ist und das angeschlossene Gerät schalten sowie dessen den aktuellen Stromverbrauch messen kann.

Die aktuelle Firmware für den SP-2101W ist die Version 2.08. Diese unterstützt nur noch _HTTP Digest Authtentication_, die vom *Smart Appliance Enabler* aktuell noch nicht unterstützt wird. Die Firmware-Version 2.03 hingegen unterstützt noch _HTTP Basic Authtentication_. Zur Verwendung der SP-2101W mit dem *Smart Appliance Enabler* ist deshalb ein Firmware-Downgrade erforderlich, der wie folgt vorgenommen werden kann:

1. Die [Firmware-Version 2.03](https://www.dropbox.com/s/zgqvl3ipryss34f/SP2101W_EDIMAX_2.03_20151012_upg.bin?dl=0) herunterladen
1. Den Reset-Taster länger als 10 Sekunden gedrückt halten, um den SP-2101W in die Werkseinstellung zurückzusetzen.
1. Auf dem Computer mit dem unverschlüsselten WLAN des SP-2101W verbinden
1. Im Web-Browser folgende URL öffnen: http://192.168.20.3:10000/tnupgrade.html , wobei die aktuell installierte Firmware-Version angezeigt wird und eine Datei mit der zu installierenden Firmware angegeben werden kann
1. Die heruntergeladene Datei SP2101W_EDIMAX_2.03_20151012_upg.bin auswählen
1. Nach Beendigung des Firmware-Updates den SP-2101W erneute in die Werkseinstellung zurückzusetzen und neu konfigurieren

## SP-2101W als Stromzähler
Die aktuelle Leistungsaufnahme des SP-2101W kann wie folgt abgefragt werden (Passwort hier 12345678):
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent></Device.System.Power.NowCurrent><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>' http://admin:12345678@192.168.69.74:10000/smartplug.cgi
<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent>0.2871</Device.System.Power.NowCurrent><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>
```
Bei der Angabe der für die Abfrage notwendigen XML-Struktur im *Smart Appliance Enabler* ist zu beachten, dass diese _encoded_ ist. Dazu kann z.B. http://coderstoolbox.net/string/#!encoding=xml&action=encode&charset=us_ascii genutzt werden.

Damit der *Smart Appliance Enabler* in dieser XML-Antwort den eigentlichen Wert für die Leistungsaufnahme findet (hier: 52.49W), muss als Regulärer Ausdruck ```.*NowPower>(\d*.{0,1}\d+).*``` angegeben werden.

Unter Berücksichtigung der genannten Punkte könnte die Konfiguration des SP-2101W als Stromzähler wie folgt aussehen:
```
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <HttpElectricityMeter url="http://192.168.1.1:10000/smartplug.cgi" data="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;get&quot;&gt;&lt;NOW_POWER&gt;&lt;Device.System.Power.NowCurrent&gt;&lt;/Device.System.Power.NowCurrent&gt;&lt;Device.System.Power.NowPower&gt;&lt;/Device.System.Power.NowPower&gt;&lt;/NOW_POWER&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;" powerValueExtractionRegex=".*NowPower>(\d*.{0,1}\d+).*" contentType="application/xml" username="admin" password="12345678" />
    </Appliance>
</Appliances>
```
In der Log-Datei /var/log/smartapplianceenabler.log sollten sich dann für jede Abfrage folgende Zeilen finden:
```
2017-06-03 18:53:37,627 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:101] F-00000001-000000000001-00: Sending HTTP request
2017-06-03 18:53:37,627 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:102] F-00000001-000000000001-00: url=http://192.168.69.74:10000/smartplug.cgi
2017-06-03 18:53:37,627 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:103] F-00000001-000000000001-00: data=<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent></Device.System.Power.NowCurrent><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>
2017-06-03 18:53:37,628 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:104] F-00000001-000000000001-00: contentType=application/xml
2017-06-03 18:53:37,628 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: username=admin
2017-06-03 18:53:37,628 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:106] F-00000001-000000000001-00: password=12345678
2017-06-03 18:53:37,743 DEBUG [Timer-0] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:118] F-00000001-000000000001-00: Response code is 200
2017-06-03 18:53:37,744 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:119] F-00000001-000000000001-00: HTTP response: <?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent>0.2871</Device.System.Power.NowCurrent><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>
2017-06-03 18:53:37,744 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:120] F-00000001-000000000001-00: Power value extraction regex: .*NowPower>(\d*.{0,1}\d+).*
2017-06-03 18:53:37,744 DEBUG [Timer-0] d.a.s.a.HttpElectricityMeter [HttpElectricityMeter.java:122] F-00000001-000000000001-00: Power value extracted from HTTP response: 52.49
2017-06-03 18:53:37,745 DEBUG [Timer-0] d.a.s.a.PollElectricityMeter [PollElectricityMeter.java:63] F-00000001-000000000001-00: timestamps added/removed/total: 1/1/7
```

## SP-2101W als Schalter
Der Schaltzustand des SP-2101W kann wie folgt geändert werden (Passwort hier 12345678):

_Einschalten_
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>'  http://admin:12345678@192.168.1.1:10000/smartplug.cgi
```

_Ausschalten_
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>OFF</Device.System.Power.State></CMD></SMARTPLUG>'  http://admin:12345678@192.168.1.1:10000/smartplug.cgi
```

Entsprechend sieht die Konfiguration für den *Smart Appliance Enabler* aus:
```
<Appliances ...>
    <Appliance ...>
        <HttpSwitch onUrl="http://192.168.1.1:10000/smartplug.cgi" onData="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;ON&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;" offUrl="http://192.168.1.1:10000/smartplug.cgi" offData="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;OFF&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;"username="admin" password="12345678" />
    </Appliance>
</Appliances>
```
In der Log-Datei /var/log/smartapplianceenabler.log sollten sich dann für jede Schaltvorgang folgende Zeilen finden:
```
2017-06-03 18:54:03,193 DEBUG [http-nio-8080-exec-5] d.a.s.s.w.SempController [SempController.java:192] F-00000001-000000000001-00: Received control request
2017-06-03 18:54:03,197 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:101] F-00000001-000000000001-00: Sending HTTP request
2017-06-03 18:54:03,197 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:102] F-00000001-000000000001-00: url=http://192.168.69.74:10000/smartplug.cgi
2017-06-03 18:54:03,198 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:103] F-00000001-000000000001-00: data=<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>
2017-06-03 18:54:03,198 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:104] F-00000001-000000000001-00: contentType=application/xml
2017-06-03 18:54:03,199 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: username=admin
2017-06-03 18:54:03,199 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:106] F-00000001-000000000001-00: password=12345678
2017-06-03 18:54:04,363 DEBUG [http-nio-8080-exec-5] d.a.s.a.HttpTransactionExecutor [HttpTransactionExecutor.java:118] F-00000001-000000000001-00: Response code is 200
2017-06-03 18:54:04,364 DEBUG [http-nio-8080-exec-5] d.a.s.a.Appliance [Appliance.java:318] F-00000001-000000000001-00: Control state has changed to on: runningTimeMonitor=not null
2017-06-03 18:54:04,370 DEBUG [http-nio-8080-exec-5] d.a.s.s.w.SempController [SempController.java:214] F-00000001-000000000001-00: Setting appliance state to ON

```
