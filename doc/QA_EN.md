# Questions / problems and answers
## Questions / problems
### Sunny Portal
- Consumers cannot be added in Sunny Portal ---> [SEMP1](#semp1), [SP1](#sp1)
- Load power is not displayed in Sunny Portal ---> [SEMP2](#semp2)
- How can I switch the consumer in Sunny Portal? ---> [SP2](#sp2)
- Entries such as: "EM gateway not found", "EM device not found" often appear in the system logbook. ---> [SP3](#sp3)

### Sunny Home Manager
- Device does not turn on ---> [SEMP3](#semp3), [SEMP4](#semp4), [SEMP5](#semp5), [SAE4](#sae4), [SAE7](#sae7)
- Device is unexpectedly turned on ---> [SEMP4](#semp4)
- Device with excess energy time plan is started right at the beginning of the timeframe even though there is no excess energy [SEMP6](#semp6)

### Smart Appliance Enabler
- Is the *Smart Appliance Enabler* running? ---> [SAE1](#sae1)
- Error starting *Smart Appliance Enabler* ---> [SAE2](#sae2)
- Where can you set a port other than 8080? ---> [SAE5](#sae5)
- How to backup and restore the *Smart Appliance Enabler* configuration, for example for a new installation? ---> [SAE6](#sae6)

## Answers

### SP1
New devices can only be added as long as the [maximum number of devices is not exceeded](SunnyPortal_EN.md#max-devices).

In order to force the *Sunny Home Manager* to search for new devices in the local network again, its power supply can be interrupted briefly. When it has fully started again, [the process for adding new devices must be run through again] (SunnyPortal_EN.md) in *Sunny Portal*.

### SP2
From the point of view of the *Sunny Home Manager*, devices that are managed via the *Smart Appliance Enabler* are **consumers**. Some parameters of these consumers (e.g. percentage of PV energy) can be configured via the *Sunny Portal*, but the device cannot be switched via the *Sunny Portal*. Instead, the device can be switched via [Status-Page](Status_EN.md) of the web interface of the *Smart Appliance Enabler*.

### SP3
The gateway and the devices are not found in the system log for a long time and are then found again. If this process is repeated often, it is usually because one of the devices to be controlled (integrated with WLAN) has poor reception and thus produces a timeout in the entire process. This problem can usually be solved by using a WLAN repeater or switching to wired networking.

### SEMP1
When the *Sunny Home Manager* has found the *Smart Appliance Enabler* in the network, it then queries its status **every 60 seconds**. These queries are logged in the *Smart Appliance Enabler* log file and look like this:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```
If these entries are not available, the communication between *Sunny Home Manager* and *Smart Appliance Enabler* does not work.

Check the following points:
- Does the [SEMP protocol](SEMP_EN.md) work and is the [SEMP-URL](SEMP_EN.md#url) correct?
- Has the *Smart Appliance Enabler* started?
- Can the host running *Smart Appliance Enabler* be pinged?
- Can the *Sunny Home Manager* be pinged?

### SEMP2
First, it must be ensured that the *Smart Appliance Enabler* is found by the *Sunny Home Manager* ---> [SEMP1](#semp1)

If meter values are not displayed in the *Sunny Portal*, the following values must be checked in the [SEMP interface](SEMP_EN.md#xml):
- in the `DeviceStatus` under `PowerInfo`, `AveragePower` must be greater than 0. If not, the power consumption may not be determined. ---> [SAE3](#sae3)
- In the `DeviceStatus`, the `Status` must have the value `On`, otherwise the *Sunny Home Manager* will ignore the performance values

### SEMP3
First, it must be ensured that the *Smart Appliance Enabler* is found by the *Sunny Home Manager* ---> [SEMP1](#semp1)

The *Sunny Home Manager* will only send a switch-on command for a device if a (runtime/energy) requirement has been reported to it. The *Smart Appliance Enabler* does that if
- [schedule was created](Schedules_EN.md) which is **active** and **applicable (day of the week and time)**
- or by [clicking on the green traffic light](Status_EN.md#click-green) an **ad-hoc requirement** arises

You can check in [SEMP-XML](SEMP_EN.md#xml) whether the *Sunny Home Manager* is notified of a demand:
- In the `DeviceStatus`, `EMSignalsAccepted` must be set to `true`
- there must be a `PlanningRequest` with a `Timeframe` at which
    - `EarliestStart` is `0`
    - `minRunningTime` (or `minEnergy` for wallboxes) is greater than `0` if the consumer **must** run. If it **can** run (to use excess energy), `minRunningTime` (or `minEnergy` for wallboxes) must be `0`

If these requirements are met, the *Sunny Home Manager* **may** send a switch-on command at any time.

If the consumer **must** run, it will send a switch-on command **at the latest** when the value of `LatestEnd` in the `Timeframe` of the `PlanningRequest` is only slightly (approx. 60-300) greater than the value of `minRunningTime`.

You can [check in the log](Control_EN.md#control-request) whether a control request is received from the *Sunny Home Manager*. If a corresponding log entry is found and the device is still not switched, it is not the *Sunny Home Manager*. ---> [SAE4](#sae4)

### SEMP4
If switching processes do not meet expectations, you should first check what information the *Smart Appliance Enabler* transmitted to the *Sunny Home Manager* via [SEMP interface](SEMP_DE.md#xml) immediately before the unexpected switching process. To do this, you first have to find the [unexpected switching command](#sae4) in the [log file](Logging_DE.md). From this point, search **backward** for `Reporting device status from control`. This line marks the beginning of a query by the *Sunny Home Manager* and contains
- the *current switching status* (`status=...`)
- the information whether switching commands are currently being accepted (`eMSignalsAccepted=`)
- Runtime or energy requirements (`Timeframe added to PlanningRequest`)

*Example*

The following log shows that the device is not switched on (`status=Off`), but switching commands are accepted (`eMSignalsAccepted=true`). There is also an energy requirement for 0Wh (`0Wh`) up to 9360Wh (`172096s`), which must be met from now (`0s`) to 172096 seconds (`172096s`).
```
2024-03-07 18:27:55,157 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:336] F-00000001-000000000019-00: Reporting device status from control
2024-03-07 18:27:55,157 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:345] F-00000001-000000000019-00: DeviceStatus{eMSignalsAccepted=true, status=Off, errorCode=null}
2024-03-07 18:27:55,157 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:351] F-00000001-000000000019-00: Reporting power info from meter.
2024-03-07 18:27:55,158 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:365] F-00000001-000000000019-00: PowerInfo{averagePower=0, minPower=null, maxPower=null, timestamp=0, averagingInterval=60}
2024-03-07 18:27:55,158 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:435] F-00000001-000000000019-00: Timeframe created: 0s-172096s:0Wh/9360Wh
2024-03-07 18:27:55,159 DEBUG [http-nio-8080-exec-6] d.a.s.s.w.SempController [SempController.java:387] F-00000001-000000000019-00: Timeframe added to PlanningRequest: 0s-172096s:0Wh/9360Wh
```

### SEMP5
From the point of view of SMA, the information that the *Sunny Home Manager* received is relevant for the error analysis. Its [SEMP logs can also be called up](ConnectionAssist_EN.md) and should be used for any service requests to SMA. SMA will not deal with logs from the *Smart Appliance Enabler*.

### SEMP6
In order to plan the device runtimes, the *Sunny Home Manager* must know the costs associated with the running time of these devices. Therefore, the following parameters must have meaningful values (i.e. usually greater than 0):

- Sunny Portal: Configuration -> PV System Properties -> Tab: Parameters ->
  - Electricity Tariff
  - Reimbursement
- SMA Energy App: Settings -> Tariffs
  - Electricity Tariff
  - Reimbursement

**Attention: It looks as if the values in Sunny Portal and the SMA Energy App are not synchronized, i.e. you have to set the values in Sunny Portal and the SMA Energy App.**

### SAE1
The command to check whether the *Smart Appliance Enabler* is running can be found in the [Installation Guide](InstallationManual_EN.md#status) or in the [Docker Guide](Docker_EN.md#container-status).

### SAE2
If the *Smart Appliance Enabler* cannot be started and you cannot find any information in the [Log](Logging_EN.md), it makes sense to start it in the current shell as a test. This allows you to see any errors on the console. The shell must be owned by the user who is otherwise used for the *Smart Appliance Enabler* process - this is usually the user `sae`.

The command for this corresponds exactly to what the start script normally does and looks like this:
```console
sae@raspberrypi:~ $ /usr/bin/java -Djava.awt.headless=true -Xmx256m -Duser.language=de -Duser.country=DE -DPIGPIOD_HOST=localhost -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid -Dsae.home=/opt/sae -jar /opt/sae/SmartApplianceEnabler-2.1.0.war
```  
The version number in the name of the war file must of course be adjusted according to the version used!

### SAE3
The power consumption of the device, which is transmitted to the *Sunny Home Manager*, is determined using the [meter configured](Meter_EN.md) in the *Smart Appliance Enabler*. Depending on its type, you can see the power consumption in the log.

### SAE4
If a control request is received from the *Sunny Home Manager*, this is passed on to the control configured for the device in the *Smart Appliance Enabler*. Depending on its type, you can see the control request in the log:
- [GPIO switch](GPIOSwitch_EN.md#log)
- [HTTP switch](HttpSwitch_EN.md#log)
- [Modbus switch](ModbusSwitch_EN.md#log)
- [PWM switch](PwmSwitch_EN.md#log)
- [Wallbox](EVCharger_EN.md#log)

### SAE5
The default port can be changed in the [Server Configuration](ConfigurationFiles_EN.md#user-content-etc-default-smartapplianceenabler).

### SAE6
The entire configuration of the *Smart Appliance Enabler* is contained in [two XML files](ConfigurationFiles_EN.md). This can be [backed up on another computer] (ConfigurationFiles_EN.md#user-content-scp).

### SAE7
If a [schedule for the use of surplus energy](Schedules_EN.md) is configured for the device, the *Sunny Home Manager* will only send a switch-on command if there is sufficient surplus and it can be expected that this will remain the case according to the configured conditions. For maximum chances of a switch-on command, these conditions should not be restrictive initially. Therefore, you should start with the following values:
- [Appliance Configuration: Min. switch-on time](Appliance_DE.md) leave empty
- [Appliance Configuration: Interruption allowed](Appliance_DE.md) should be enabled
 