# WiCAN ODB2 Adapter

![meatPi](../../pics/meatPi.png)

The [WiCAN ODB2 adapter from MeatPi](https://www.meatpi.com/products/wican) **connects the vehicle's CAN bus with the WLAN and enables communication via MQTT**.

## Functionality
The adapter remains **permanently plugged into the vehicle**.

When the vehicle approaches the house, the **WiCAN connects to the WiFi** and sends an MQTT message that it is **online**. The [wican-status.sh](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-status.sh) script waits for this and **requests the SOC** using an MQTT message. The [wican-soc.sh](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-soc.sh) script, which extracts the **SOC, is waiting for the answer and published together with a timestamp as an MQTT message (with retained flag)**.

As soon as the **vehicle is connected to the wallbox**, the *Smart Appliance Enabler* will run the [SOC script for WiCAN ODB2 adapter](https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/soc.sh), which receives the MQTT message with SOC and timestamp from the MQTT server.

Approximately 3 minutes after switching off the vehicle **the WiCAN ODB2 adapter also switches off** in order not to put a strain on the vehicle battery.

## Configuration of WiCAN ODB2 adapter

Important settings for configuring the WiCAN ODB2 adapter are:

### AP Config
- Mode: AP+Station

### Station Config
- SSID: SSID of the WiFi from the house
- Password: Password for the WiFi in the house

### CAN
- Protocol: elm327
- MQTT: Enable

### BLE
- BLE Status: Disabled

### Sleep mode
- Sleep: Enable

### MQTT
- MQTT URL: IP address of the MQTT server
- MQTT Port: Port of the MQTT server

## Installation for Smart Appliance Enabler

First, the MQTT clients must be installed so that the shell scripts can interact with the MQTT server:

```bash
sudo apt install mosquitto clients
```
The installation

- the script for monitoring the WiCAN status
- the script for retrieving the SOC
- SOC script for WiCAN ODB2 adapter

is done with the following commands:

```bash
$ mkdir /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-status.sh -P /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/wican-soc.sh -P /opt/sae/soc
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/soc/wican/soc.sh -P /opt/sae/soc
$ chmod +x /opt/sae/soc/*.sh
```

**The scripts `wican-status.sh` and `wican-soc.sh` must be adapted for the respective vehicle**: The CAN message to request the SOC must be entered in `wican-status.sh`. In `wican-soc.sh` the SOC needs to be extracted from the response. Both scripts currently work for the Nissan Leaf ZE1 without any adjustments. The CAN messages for a specific vehicle can usually be found in relevant Internet forums or using **apps such as "Car Scanner"**, which is supported by the WiCAN-OBD2 adapter.

Systemd services are used to start the first two scripts:
```bash
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/wican-status.service -P /lib/systemd/system
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/wican-soc.service -P /lib/systemd/system
$ sudo systemctl daemon reload
```

The following commands are only described for `wican-status`. They apply analogously to `wican-soc`.

To start, all you need is:

```bash
$ sudo service wican status start
```

The status can be displayed as follows:

```bash
$ sudo service wican-status status
● wican-status.service - WiCan status monitor
      Loaded: loaded (/etc/systemd/system/wican-status.service; enabled; vendor preset: enabled)
      Active: active (running) since Sun 2024-02-25 14:36:10 CET; 1h 40min ago
    Main PID: 27260 (wican-status.sh)
       Tasks: 2 (limit: 4915)
         CPU: 431ms
      CGroup: /system.slice/wican-status.service
              ├─27260 /bin/bash /opt/sae/soc/wican-status.sh
              └─27261 mosquitto_sub -h 192.168.1.1 -t wican/5432048f421d/status -C 1

Feb 25 14:36:10 raspi2 systemd[1]: Started WiCan status monitor.
Feb 25 14:36:10 raspi2 wican-status.sh[27260]: Waiting for message ...
```

Stop it with:

```bash
$ sudo service wican-status stop
```

In order for the services to be started even after a reboot, they must be activated accordingly:
```bash
$ sudo systemctl enable wican-soc
Created symlink /etc/systemd/system/multi-user.target.wants/wican-soc.service → /etc/systemd/system/wican-soc.service.
$ sudo systemctl enable wican status
Created symlink /etc/systemd/system/multi-user.target.wants/wican-status.service → /etc/systemd/system/wican-status.service.
```

The console output of the scripts is available through the `journalctl` command:
```bash
sudo journalctl _SYSTEMD_UNIT=wican-soc.service
-- Journal begins at Fri 2023-09-08 09:07:38 CEST, ends at Sun 2024-03-17 08:08:08 CET. --
Feb 25 14:39:03 raspi2 wican-soc.sh[27329]: Waiting for messages ...
Feb 25 17:18:45 raspi2 wican-soc.sh[27329]: Message received:
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6802,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[16,53,97,1,255,255,252,24]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6919,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[33,2,175,255,255,252,79,255]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6929,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[34,255,244,72,6,138,48,212]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6939,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[35,148,76,56,207,3,145,0]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6942,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[36,1,112,0,36,0,0,11]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6958,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[37,179,232,0,15,180,27,128]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6962,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[38,0,5,255,255,252,79,255]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[28304]: {"bus":"0","type":"rx","ts":6978,"frame":[{"id":1979,"dlc":8,"rtr":false,"extd":false,"data":[39,255,252,170,1,174,255,255]}]}
Feb 25 17:18:45 raspi2 wican-soc.sh[27329]: Waiting for messages ...
```

## Smart Appliance Enabler configuration
The configuration must be configured under “Wallbox”:

- Script to read the SOC: `/opt/sae/soc/soc.sh`
- Retry: [] Enabled (not selected)

This ensures that the script is only executed once after connecting to the wallbox, because the SOC script can only provide the correct value at this point in time.