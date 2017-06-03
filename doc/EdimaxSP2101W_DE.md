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
Die aktuelle Leistungsaufnahme des SP-2101W kann wie folgt abgefragt werden:
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent></Device.System.Power.NowCurrent><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>' http://admin:12345678@192.168.69.74:10000/smartplug.cgi
<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent>0.2871</Device.System.Power.NowCurrent><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>
```
Bei der Angabe der für die Abfrage notwendigen XML-Struktur im *Smart Appliance Enabler* ist zu beachten, dass diese _encoded_ ist. Dazu kann z.B. http://coderstoolbox.net/string/#!encoding=xml&action=encode&charset=us_ascii genutzt werden.

Damit der *Smart Appliance Enabler* in dieser XML-Antwort den eigentlichen Wert für die Leistungsaufnahme findet (hier: 52.49W), muss als Regulärer Ausdruck ```.*NowPower.(\d+).*``` angegeben werden.

Unter Berücksichtigung der genannten Punkte könnte die Konfiguration des SP-2101W als Stromzähler wie folgt aussehen:
```
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <HttpElectricityMeter url="http://192.168.1.1/cm?cmnd=Status%208" data="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;get&quot;&gt;&lt;NOW_POWER&gt;&lt;Device.System.Power.NowCurrent&gt;&lt;/Device.System.Power.NowCurrent&gt;&lt;Device.System.Power.NowPower&gt;&lt;/Device.System.Power.NowPower&gt;&lt;/NOW_POWER&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;" extractionRegex=".*NowPower.(\d+).*" />
    </Appliance>
</Appliances>
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
        <HttpSwitch onUrl="http://192.168.1.1/cm?cmnd=Power%20On" onData="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;ON&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;" offUrl="http://192.168.1.1/cm?cmnd=Power%20Off" offData="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;OFF&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;"/>
    </Appliance>
</Appliances>
```
