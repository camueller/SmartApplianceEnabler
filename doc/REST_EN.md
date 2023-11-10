# REST interface
REST services exist for managing the configuration using the web front end. Like the SEMP interface itself, these can also be used independently of the web front end.

## Switching an appliance
The following command can be used to switch on a device, whereby the URL and the device ID (identical to the appliance ID) must be adjusted:
```console
curl -X POST -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000002-00</DeviceId><On>true</On></DeviceControl></EM2Device>' --header 'Content-Type: application/xml' http://127.0.0.1:8080/semp
```
To turn it off, just set `<On>false</On>` instead of `<On>true</On>`.

A device that is switched on manually only stays in this state if it is in an active time frame. Otherwise the *Smart Appliance Enabler* turns it off again.
A second command is required to generate an additional time frame for a specific runtime before switching on. Both commands sent directly one after the other (first timeframe, then switching on) have the same effect as the traffic light function provides (click for immediate start + selection of the corresponding runtime).
The following command works to activate an additional timeframe for 10 minutes from now:
```console
curl -s -X PUT -F id=F-00000001-000000000002-00 -F runtime=600 http://127.0.0.1:8080/sae/runtime
```

## Setting schedules
Usually the schedules are read from the `Appliance.xml` file. However, it is possible to transfer the schedules to the *Smart Appliance Enabler* via REST. To do this, the schedule(s) must be combined in a root element `Schedules`, which is passed to *Smart Appliance Enabler* with specification of the appliance ID. The `Schedules` element contains one or more `Schedule` elements whose structure is derived from the [XML schema](https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-2.0.xsd ).

```bash
$ curl \
    -s \
    -X POST \
    -d '<Schedules xmlns="http://github.com/camueller/SmartApplianceEnabler/v2.0"><Schedule><RuntimeRequest min="1800" max="3600" /><DayTimeframe><Start hour="0" minute="0" second="0" /><End hour="18" minute="59" second="59" /></DayTimeframe></Schedule></Schedules>' \
    --header 'Content-Type: application/xml' \
    http://localhost:8080/sae/schedules?id=F-00000001-000000000001-00
```

The `xmlns` attribute (especially the version of the *Smart Appliance Enabler* at the end) must match the `xmlns` attribute in the `Appliances.xml` file.

The following should then be found in the *Smart Appliance Enabler* log:
```bash
2023-11-10 14:00:35,643 DEBUG [http-nio-8080-exec-1] d.a.s.w.SaeController [SaeController.java:434] F-00000001-000000000001-00: Received request to activate 1 schedule(s)
[...]
2023-11-10 14:00:35,644 DEBUG [http-nio-8080-exec-1] d.a.s.s.TimeframeIntervalHandler [TimeframeIntervalHandler.java:190] F-00000001-000000000001-00: Cleaing queue
2023-11-10 14:00:35,644 DEBUG [http-nio-8080-exec-1] d.a.s.s.TimeframeIntervalHandler [TimeframeIntervalHandler.java:195] F-00000001-000000000001-00: Starting to fill queue
2023-11-10 14:00:35,655 DEBUG [http-nio-8080-exec-1] d.a.s.s.TimeframeIntervalHandler [TimeframeIntervalHandler.java:364] F-00000001-000000000001-00: Adding timeframeInterval to queue: CREATED/2023-11-10T00:00:00/2023-11-10T18:59:59::ENABLED/1800s/3600s
```
