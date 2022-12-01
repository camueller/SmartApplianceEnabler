# HTTP meter

For [HTTP-based devices, these general instructions](Http_DE.md) must be observed!

The `parameter` designates the measured value which the meter supplies to the *Smart Appliance Enabler* for performance determination.

If possible, `Meter reading` should be set as the parameter, because the *Smart Appliance Enabler* then only has to query this value **once per minute** and can calculate the power very precisely from the difference to the previous query. However, the meter reading in kWh must be supplied with at least **4 decimal places** (better 5). With some adapters, this accuracy must first be set (see [Tasmota](Tasmota_DE.md)).

If `Power` is set as the parameter, this value is queried several times per minute in order to calculate the average from these values. The interval between these queries can be specified with the `Query Interval` - the default value is 20 seconds.

If the HTTP response is delivered in **JSON format**, this should be set as `Format` because then by specifying the `Extraction path` the numerical value can be extracted from the HTTP response very easily.

Alternatively (or also downstream of the JSON interpretation), a [regular expression for extraction](ValueExtraction_DE.md) can be specified if the numerical value has to be extracted from a text (XML, ...). This also applies if the HTTP response appears to only contain the number, but it also contains a line break (CR/LF).

The configuration of the parameters described above must ensure that the *Smart Appliance Enabler* can extract the correct number from the HTTP response.

The value in kWh is required for the `Meter reading` parameter and in W for the `Power` parameter. If the values are supplied in other units, a `factor to value` must be specified, which is multiplied by the supplied value to convert it into the required unit. For example, if the parameter `Power` is supplied in mW, the value `1000` must be specified as the `factor to value`.

![HTTP-basierter Zähler](../pics/fe/HttpMeter.png)

## Log
If an HTTP counter is used for the device `F-00000001-000000000005-00`, the determined power consumption can be displayed in [Log](Logging_DE.md) with the following command:

```console
sae@raspi:~ $ grep 'Http' /tmp/rolling-2021-01-01.log | grep F-00000001-000000000005-00
2021-01-01 09:42:50,472 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:107] F-00000001-000000000005-00: Sending GET request url=http://espressomaschine/cm?cmnd=Status%208
2021-01-01 09:42:50,516 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:168] F-00000001-000000000005-00: Response code is 200
2021-01-01 09:42:50,531 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:86] F-00000001-000000000005-00: url=http://espressomaschine/cm?cmnd=Status%208 httpMethod=GET data=null path=null
2021-01-01 09:42:50,532 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:89] F-00000001-000000000005-00: Response: {"StatusSNS":{"Time":"2021-01-01T09:42:50","ENERGY":{"TotalStartTime":"2019-08-18T10:55:03","Total":164.950,"Yesterday":0.482,"Today":0.124,"Power":1279,"ApparentPower":1481,"ReactivePower":747,"Factor":0.86,"Voltage":233,"Current":6.370}}}
2021-01-01 09:42:50,533 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:58] F-00000001-000000000005-00: value=1279.0 protocolHandlerValue={"StatusSNS":{"Time":"2021-01-01T09:42:50","ENERGY":{"TotalStartTime":"2019-08-18T10:55:03","Total":164.950,"Yesterday":0.482,"Today":0.124,"Power":1279,"ApparentPower":1481,"ReactivePower":747,"Factor":0.86,"Voltage":233,"Current":6.370}}} valueExtractionRegex=,.Power.:(\d+) extractedValue=1279
2021-01-01 09:42:55,632 DEBUG [http-nio-8080-exec-9] d.a.s.m.HttpElectricityMeter [HttpElectricityMeter.java:154] F-00000001-000000000005-00: average power = 1280W
2021-01-01 09:42:55,636 DEBUG [http-nio-8080-exec-9] d.a.s.m.HttpElectricityMeter [HttpElectricityMeter.java:154] F-00000001-000000000005-00: average power = 1280W
```

*Webmin*: In [View Logfile](Logging_DE.md#user-content-webmin-logs) enter `F-00000001-000000000005-00` after `Only show lines with text` and press refresh.
