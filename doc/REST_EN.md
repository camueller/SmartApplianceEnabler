# REST interface
Corresponding REST services exist for configuration using a web front end. Like the SEMP interface itself, these can also be used independently of the web front end.

## Switching an appliance
The following command can be used to switch on a device, whereby the URL and the device ID (identical to the appliance ID) must be adjusted:
```console
curl -X POST -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000002-00</DeviceId><On>true</On></DeviceControl></EM2Device>' --header 'Content-Type: application/xml' http://127.0.0.1:8080/semp
```
To turn it off, just set `<On>false</On>` instead of `<On>true</On>`.

A device that is switched on manually only stays in this state if it is in an active time frame. Otherwise the SAE turns it off again.
A second command is required to generate an additional time frame for a specific runtime before switching on. Both commands sent directly one after the other (first timeframe, then switching on) have the same effect as the traffic light function provides (click for immediate start + selection of the corresponding runtime).
The following command works to activate an additional timeframe for 10 minutes from now:
```console
curl -s -X PUT -F id=F-00000001-000000000002-00 -F runtime=600 http://127.0.0.1:8080/sae/runtime
```

## Setting schedules
Normally the schedules are read from the `Appliance.xml` file. However, it is possible to transfer the schedules to the SAE via REST. To do this, the schedule(s) must be combined in a root element `Schedules`, which is passed to SAE with specification of the appliance ID:
```console
curl -s -X POST -d '<Schedules xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.4"><Schedule><RuntimeRequest min="1800" max="3600" /><DayTimeframe><Start hour="0" minute="0" second="0" /><End hour="18" minute="59" second="59" /></DayTimeframe></Schedule></Schedules>' --header 'Content-Type: application/xml' http://localhost:8080/sae/schedules?id=F-00000001-000000000001-00
```
The `xmlns` attribute (especially the version of the *Smart Appliance Enabler* at the end) must match the `xmlns` attribute in the `Appliances.xml` file.

The following should then be found in the SAE log:
```console
2020-01-12 18:31:10,606 DEBUG [http-nio-8080-exec-3] d.a.s.w.SaeController [SaeController.java:413] F-00000001-000000000099-00: Received request to activate 1 schedule(s)
2020-01-12 18:31:10,614 DEBUG [http-nio-8080-exec-3] d.a.s.a.RunningTimeMonitor [RunningTimeMonitor.java:82] F-00000001-000000000099-00: Using enabled time frame 00:00:00.000-18:59:59.000/1800s/3600s
```
