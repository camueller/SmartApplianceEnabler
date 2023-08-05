# SEMP
## Protocol
The *Sunny Home Manager* detects the *Smart Appliance Enabler* via the [UPnP protocol](https://de.wikipedia.org/wiki/Universal_Plug_and_Play), which can also be used to easily detect multimedia devices. The *Smart Appliance Enabler* uses this protocol to communicate the actual **SEMP-URL** to the *Sunny Home Manager*.

The use of the UPnP protocol means that *Sunny Home Manager* and *Smart Appliance Enabler* **are in the same network** and must be able to communicate with each other via multicast!

Normally, the *Smart Appliance Enabler* can correctly determine this URL itself. However, if the host has multiple network interfaces or the *Smart Appliance Enabler* runs in a virtual machine or container, it may be necessary to tell the *Smart Appliance Enabler* which URL it should communicate to the *Sunny Home Manager*. This is done via the configuration parameter `semp.gateway.address` in the file `/etc/default/smartapplianceenabler`.

## <a name="url"></a> SEMP-URL

After the *Sunny Home Manager* has detected the *Smart Appliance Enabler*, further communication consists **exclusively** of the *Sunny Home Manager* accessing the following **SEMP-URL** of the *Smart Appliance Enabler* (host name / IP address must be adjusted accordingly) **every 60 seconds** :
```
http://raspi:8080/semp
```
The URL of the SEMP host communicated by the *Smart Appliance Enabler* to the *Sunny Home Manager* is written to the log immediately after the start:

```console
sae@raspi:~ $ grep "SEMP UPnP" /tmp/rolling-2020-12-31.log
2020-12-31 14:36:22,744 INFO [main] d.a.s.s.d.SempDiscovery [SempDiscovery.java:57] SEMP UPnP will redirect to http://192.168.1.1:8080
```

*Webmin*: In [View Logfile](#webmin-logs) enter `SEMP UPnP` after `Only show lines with text` and press refresh.

Entering this URL, supplemented by the path `/semp` (according to the above example, this would be `http://192.168.1.1:8080/semp`), in a web browser must lead to the display of the SEMP XML described below . If this does not work because this URL is incorrect, the *Sunny Home Manager* cannot communicate with the *Smart Appliance Enabler*, i.e. no devices can be added in the *Sunny Portal* and devices are not measured and switched!

## <a name="xml"></a> SEMP-XML

By entering the [SEMP-URL](#url) in a normal web browser the data can be displayed reported by the *Smart Appliance Enabler* to the *Sunny Home Manager*.

The SEMP URL returns an [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language) document according to the SEMP specification, which contains a `DeviceInfo` and a `DeviceStatus` for each device. Optionally, `PlanningRequest` can also be included.

`DeviceInfo`, `DeviceStatus` and `PlanningRequest` each contain an element `DeviceId` which is used to express which device it applies to. The `DeviceId` is identical to the `ID` that was assigned when the appliance was created in the *Smart Appliance Enabler*.

When troubleshooting, you will look closely at the *DeviceStatus*, which contains the *DeviceId* of the problematic device.
