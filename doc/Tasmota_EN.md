# Alternate firmware Tasmota
Adapters with which any device can be switched on and off via WIFI and some of which also have an integrated electricity meter are often based on the microcontroller [ESP8266](https://de.wikipedia.org/wiki/ESP8266).

Most of the time, these devices can only be used with the adapter manufacturer's cloud services. Fortunately, the alternative firmware [Tasmota](https://github.com/arendst/Sonoff-Tasmota) exists, which was originally developed for various Sonoff adapters, but can now be used for a large number of adapters.

To do this, however, the Tasmota firmware must be written ("flashed") to the flash memory of the microcontroller. To connect the microcontroller to a PC or Raspberry Pi for flashing, an FT232RL adapter (costs between 2 and 5 euros) is required.

## Flash
For the actual flashing you need a program like [ESPEasy](https://www.heise.de/ct/artikel/ESPEasy-installieren-4076214.html) or the [Tasmotizer](https://github.com/) that is based on it. tasmota/tasmotizer).

Before flashing, first delete the old firmware:
```console
pi@raspberrypi:~ $ esptool.py --port /dev/ttyUSB0 erase_flash
esptool.py v2.7
Serial port /dev/ttyUSB0
Connecting....
Detecting chip type... ESP8266
Chip is ESP8266EX
Features: WiFi
Crystal is 26MHz
MAC: bc:dd:c2:23:23:84
Uploading stub...
Running stub...
Stub running...
Erasing flash (this may take a while)...
Chip erase completed successfully in 4.0s
Hard resetting via RTS pin...
```

After that you can flash the Tastmota firmware:
```console
pi@raspberrypi:~ $ esptool.py --port /dev/ttyUSB0 write_flash -fs 1MB -fm dout 0x00000 sonoff-DE.bin
esptool.py v2.7
Serial port /dev/ttyUSB0
Connecting....
Detecting chip type... ESP8266
Chip is ESP8266EX
Features: WiFi
Crystal is 26MHz
MAC: bc:dd:c2:23:23:84
Uploading stub...
Running stub...
Stub running...
Configuring flash size...
Compressed 517008 bytes to 356736...
Wrote 517008 bytes (356736 compressed) at 0x00000000 in 33.1 seconds (effective 124.9 kbit/s)...
Hash of data verified.

Leaving...
Hard resetting via RTS pin...
```

## Upgrading the firmware
If Tasmota firmware is already on the adapter, you can update it via the menu item `Firmware update`. If an error occurs (`Upload buffer comparison differs`), you should first upload the `tasmota-minimal.bin` and only then the desired full version. [Source](https://www.schimmer-media.de/forum/index.php?thread/223-sonoff-basic-update-nicht-m%C3%B6glich/)

## Devices running Tasmota firmware as electricity meters
The following command can be used to query the status of Tasmota adapters, which also includes the meter reading and power:
```console
pi@raspberrypi:~ $ curl http://192.168.1.1/cm?cmnd=Status%208
```

Tasmota returns the response in JSON format, formatted as follows:
```
{
  "StatusSNS": {
    "Time": "2021-02-03T15:12:52",
    "Switch1": "ON",
    "ENERGY": {
      "TotalStartTime": "2020-01-05T12:41:22",
      "Total": 13.48712,
      "Yesterday": 0.000,
      "Today": 0.000,
      "Power": 0,
      "ApparentPower": 0,
      "ReactivePower": 0,
      "Factor": 0.00,
      "Voltage": 0,
      "Current": 0.000
    }
  }
}
```

The above example results in the following field contents in the *Smart Appliance Enabler*:

| Field                                             | Value                                 |
|---------------------------------------------------|---------------------------------------|
| Format                                            | JSON                                  |
| URL                                               | http://192.168.1.1/cm?cmnd=Status%208 |
| Extraction path (using parameter `meter reading`) | $.StatusSNS.ENERGY.Total              |
| Extraction path (using parameter `power`)         | $.StatusSNS.ENERGY.Power              |

By default, Tasmota only returns the `meter reading` with 3 decimal places. In order for the *Smart Appliance Enabler* to be able to calculate the power from meter reading differences as precisely as possible, the Tasmota adapter must be **configured to 5 decimal places**.
To do this, go to the Tasmota web console of the adapter and enter the command `EnergyRes 5` and complete the input with `Enter`:
```
17:14:25 RSL: RESULT = {"EnergyRes":5}
```

The [Log](Logging_EN.md) contains the following lines for each meter query:
```
2021-02-08 00:09:54,324 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000014-00: Executing timer task name=PollEnergyMeter id=13049675
2021-02-08 00:09:54,324 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:107] F-00000001-000000000014-00: Sending GET request url=http://kuehltruhe/cm?cmnd=Status%208
2021-02-08 00:09:54,459 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:168] F-00000001-000000000014-00: Response code is 200
2021-02-08 00:09:54,462 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:86] F-00000001-000000000014-00: url=http://kuehltruhe/cm?cmnd=Status%208 httpMethod=GET data=null path=$.StatusSNS.ENERGY.Total
2021-02-08 00:09:54,463 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:89] F-00000001-000000000014-00: Response: {"StatusSNS":{"Time":"2021-02-08T00:09:54","ENERGY":{"TotalStartTime":"2020-01-05T17:01:57","Total":56.00865,"Yesterday":0.53820,"Today":0.00005,"Power":0,"ApparentPower":5,"ReactivePower":5,"Factor":0.06,"Voltage":237,"Current":0.021}}}
2021-02-08 00:09:54,464 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:58] F-00000001-000000000014-00: value=56.00865 protocolHandlerValue=56.00865 valueExtractionRegex=null extractedValue=56.00865
2021-02-08 00:09:54,465 DEBUG [Timer-0] d.a.s.m.PollEnergyMeter [PollEnergyMeter.java:120] F-00000001-000000000014-00: Adding value: timestamp=2021-02-08T00:09:54.324795 value=56.00865
```

*Webmin*: In [View Logfile](Logging_EN.md#user-content-webmin-logs) enter `Http` after `Only show lines with text` and press refresh.

## Devices with Tasmota firmware as switches
The switching state can be changed as follows:

_Switch on_
```console
axel@p51:~$ curl http://192.168.1.1/cm?cmnd=Power%20On
```

_Switch off_
```console
axel@p51:~$ curl http://192.168.1.1/cm?cmnd=Power%20Off
```

_Query switch state_
```console
$ curl http://192.168.1.1/cm?cmnd=Power
{"POWER":"OFF"}
```

The above example results in the following field contents in the *Smart Appliance Enabler*:

| Field                   | URL                                    | Extraction regex |
|-------------------------| ----                                   |------------------|
| Action "Switch-on"      | http://192.168.1.1/cm?cmnd=Power%20On  |                  |
| Action "Switch off"     | http://192.168.1.1/cm?cmnd=Power%20Off |                  |
| Parameter "Switched on" | http://192.168.1.1/cm?cmnd=Power       | :.ON             |

The [Log](Logging_EN.md) contains the following lines for each switching process:
```
2020-01-06 14:51:22,817 INFO [http-nio-8080-exec-4] d.a.s.c.HttpSwitch [HttpSwitch.java:128] F-00000001-000000000001-00: Switching on
2020-01-06 14:51:22,817 DEBUG [http-nio-8080-exec-4] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:105] F-00000001-000000000001-00: Sending GET request url=http://192.168.1.1/cm?cmnd=Power%20On
2020-01-06 14:51:22,984 DEBUG [http-nio-8080-exec-4] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:160] F-00000001-000000000001-00: Response code is 200
```

*Webmin*: In [View Logfile](Logging_EN.md#webmin-logs) enter `Http` after `Only show lines with text` and press refresh.

## Create runtime request on keypress
Tasmota allows you to create rules that are triggered on specific events.
Accordingly, you can define a rule that when you press a button on the Tasmota adapter, a runtime request is sent to the *Smart Appliance Enabler*. As a result, the use of the starting current detection can (and must!) be omitted. Without these, the device would always be switched on according to configured schedules, which is why these should not be used either. As a consequence, when you press the buttons on the Tasmota adapter, all information that is otherwise provided via schedules must be transmitted.

To create a rule, enter the following on the Tasmota console:
```
Rule1 ON Button1#State=3 DO WebSend [192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600 ENDON
```
... whereby
- `Button1` is the name of the button to which the event is linked
- `State=3` defines that the event is triggered on a long keypress (Hold).
- `192.168.0.1` is the hostname or IP address where the *Smart Appliance Enabler* can be reached
- `F-xxxxxxxx-xxxxxxxxxxxx-xx` is the appliance ID
- `3600` is the desired run time in seconds
- `21600` is the latest time in seconds from now that the runtime must finish

This rule must now be activated:
```
Rule1 1
```

With a long press (> 4 seconds) of the button, the desired runtime request is now made:
```
11:01:28 APP: Knopf1 Mehrfachdruck 1
11:01:29 WIF: Prüfe Verbindung...
11:01:29 WIF: verbunden
11:01:29 RUL: BUTTON1#STATE=3 performs "WebSend [192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600"
11:01:29 SRC: Rule
11:01:29 CMD: Gruppe 0, Index 1, Befehl "WEBSEND", Daten "[192.168.0.1:8080] ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME 3600 21600"
11:01:29 RSL: RESULT = {"WebSend":"Done"}
```

If necessary, the rule can be deactivated again at any time:
```
Rule1 0
```

Further options and a detailed description of the parameters can be found in the Tasmota documentation at https://tasmota.github.io/docs/Buttons-and-Switches/#button
