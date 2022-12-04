# Edimax SP-2101W
The [Edimax SP-2101W](http://www.edimax-de.eu/edimax/merchandise/merchandise_detail/data/edimax/de/home_automation_smart_plug/sp-2101w) is a socket adapter that is connected to the WLAN and that connected device and can measure the current power consumption.

## SP-2101W as meter
The current power consumption of the SP-2101W can be queried as follows (password here 12345678):
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>' http://admin:12345678@192.168.69.74:10000/smartplug.cgi
<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>
```

When specifying the XML structure required for the query in the *Smart Appliance Enabler*, note that this is _encoded_. For this, e.g. http://coderstoolbox.net/string/#!encoding=xml&action=encode&charset=us_ascii can be used.

The above example results in the following field contents in the *Smart Appliance Enabler*:

| Field            | Value                                                                                                                                                                                                                                                         |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| URL              | http://192.168.1.1:10000/smartplug.cgi                                                                                                                                                                                                                        |
| Data             | &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;get&quot;&gt;&lt;Device.System.Power.NowPower&gt;&lt;/Device.System.Power.NowPower&gt;&lt;/NOW_POWER&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt; |
| Username         | admin                                                                                                                                                                                                                                                         |
| Password         | 12345678                                                                                                                                                                                                                                                      |
| Content-Type     | application/xml                                                                                                                                                                                                                                               |
| Extraction regex | .*NowPower>(\d*.{0,1}\d+).*                                                                                                                                                                                                                                   |

## SP-2101W as control
The switching status of the SP-2101W can be changed as follows (password here 12345678):

_Switch on_
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>'  http://admin:12345678@192.168.1.1:10000/smartplug.cgi
```

_Switch off_
```
curl -s -X POST -d '<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="setup"><Device.System.Power.State>OFF</Device.System.Power.State></CMD></SMARTPLUG>'  http://admin:12345678@192.168.1.1:10000/smartplug.cgi
```

The above example results in the following field contents in the *Smart Appliance Enabler*:

| Field            | Value                                                                                                                                                                                                                                      |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| switch-on URL    | http://192.168.1.1:10000/smartplug.cgi                                                                                                                                                                                                     |
| switch-on value  | &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;ON&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt;  |
| switch-off URL   | http://192.168.1.1:10000/smartplug.cgi                                                                                                                                                                                                     |
| switch-off value | &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF8&quot;?&gt;&lt;SMARTPLUG id=&quot;edimax&quot;&gt;&lt;CMD id=&quot;setup&quot;&gt;&lt;Device.System.Power.State&gt;OFF&lt;/Device.System.Power.State&gt;&lt;/CMD&gt;&lt;/SMARTPLUG&gt; |
| Username         | admin                                                                                                                                                                                                                                      |
| Password         | 12345678                                                                                                                                                                                                                                   |
| Content-Type     | application/xml                                                                                                                                                                                                                            |

If an Edimax SP2101W is switched, the following lines can be found in the [Log](Logging_EN.md) for each switching process:
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
