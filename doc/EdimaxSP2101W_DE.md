# Edimax SP-2101W
Der [Edimax SP-2101W](http://www.edimax-de.eu/edimax/merchandise/merchandise_detail/data/edimax/de/home_automation_smart_plug/sp-2101w) ist ein Steckdosenadapter, der mit dem WLAN verbunden ist und das angeschlossene Gerät schalten sowie dessen den aktuellen Stromverbrauch messen kann.

## SP-2101W als Stromzähler
Die aktuelle Leistungsaufnahme des SP-2101W kann wie folgt abgefragt werden (Passwort hier 12345678):
```bash
$ curl \
    -s \
    -X POST \
    -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>' \
    http://admin:12345678@192.168.69.74:10000/smartplug.cgi
```

Das Ergebnis sieht dann (zur besseren Lesbarkeit formatiert) bspw. wie folgt aus:

```xml
<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
    <CMD id="get">
        <NOW_POWER>
            <Device.System.Power.NowPower>52.49</Device.System.Power.NowPower>
        </NOW_POWER>
    </CMD>
</SMARTPLUG>
```

Bei der Angabe der für die Abfrage notwendigen XML-Struktur im *Smart Appliance Enabler* ist zu beachten, dass diese _encoded_ ist. Dazu kann z.B. http://coderstoolbox.net/string/#!encoding=xml&action=encode&charset=us_ascii genutzt werden.

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler*:

| Feld         | Wert |
| ----         | ---- |
| URL          | http://192.168.1.1:10000/smartplug.cgi |
| Daten        | &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;get&quot;&gt;&lt;Device.System.Power.NowPower&gt;&lt;/Device.System.Power.NowPower&gt;&lt;/NOW_POWER&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt; |
| Benutzername | admin |
| Password     | 12345678 |
| Content-Type | application/xml |
| Regulärer Ausdruck zum Extrahieren der Leistung | .*NowPower>(\d*.{0,1}\d+).* |

## SP-2101W als Schalter
Der Schaltzustand des SP-2101W kann wie folgt geändert werden (Passwort hier 12345678):

_Einschalten_
```bash
$ curl \
    -s \
    -X POST \
    -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>' \
    http://admin:12345678@192.168.1.1:10000/smartplug.cgi
```

_Ausschalten_
```bash
$ curl \
    -s \
    -X POST \
    -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>OFF</Device.System.Power.State></CMD></SMARTPLUG>' \
    http://admin:12345678@192.168.1.1:10000/smartplug.cgi
```

Aus obigem Beispiel ergeben sich folgende Feld-Inhalte im *Smart Appliance Enabler*:

| Feld                  | Wert |
| ----                  | ---- |
| URL zum Einschalten   | http://192.168.1.1:10000/smartplug.cgi |
| Daten zum Einschalten | &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;ON&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt; |
| URL zum Ausschalten   | http://192.168.1.1:10000/smartplug.cgi |
| Daten zum Ausschalten | &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;OFF&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt; |
| Benutzername          | admin |
| Password              | 12345678 |
| Content-Type          | application/xml |

Wird ein Edimax SP2101W geschaltet, finden sich im im [Log](Logging_DE.md) für jeden Schaltvorgang folgende Zeilen:
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
