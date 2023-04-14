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

## Calibrating the firmware
[Tasmota firmware should be calibrated](https://tasmota.github.io/docs/Power-Monitoring-Calibration/) before use, since correct measurements are affected by hardware and timing differences.

## Using Tasmota adapters in Smart Appliance Enabler
The *Smart Appliance Enabler* can communicate with Tasmota adapters via MQTT or HTTP. However, MQTT is recommended because it uses fewer resources on the adapter itself and also in the network and is more stable.

## Configure the firmware
### Use of Daylight Saving Time
Daylight Saving Time is not enabled by default in Tasmota. In order to use daylight saving time for time specifications, you have to enter the command `timezone 99` on the Tasmota web console of the adapter and complete the input with `Enter`:
```
10:46:24 CMD: time zone 99
10:46:24 MQT: stat/tasmota/RESULT = {"Timezone":99}
```

### Number of decimal places for the meter reading (only required when using "meter reading" as parameter)
By default, Tasmota only delivers the `meter reading` with 3 decimal places. In order for the *Smart Appliance Enabler* to be able to calculate the power from meter reading differences as precisely as possible, the Tasmota adapter must be **configured to 5 decimal places**. To do this, go to the Tasmota web console of the adapter and enter the command `EnergyRes 5` and complete the input with `Enter`:
```
17:14:25 CMD: EnergyRes 5
17:14:25 RSL: RESULT = {"EnergyRes":5}
```
### MQTT (only when using MQTT as communication protocol)
First, the Tasmota adapter must be [configured to use MQTT](https://tasmota.github.io/docs/MQTT/), which mainly concerns `Host` and `Port` of the MQTT broker and the `Topic`.

#### Frequency of MQTT messages
By default, Tasmota only sends the telemetry data (including the meter reading and the current power consumption) every 5 minutes. In order to send these every 60 seconds, you have to enter the command `TelePeriod 60` on the Tasmota web console of the adapter and complete the input with `Enter`:
```
10:50:37 CMD: TelePeriod 60
10:50:37 MQT: stat/tasmota/RESULT = {"TelePeriod":60}
```

## Devices running Tasmota firmware as electricity meters
### MQTT
Tasmota sends the telemetry data in JSON format, formatted as follows:
```
{
  "Time": "2023-04-13T11:15:37",
  "ENERGY": {
    "TotalStartTime": "2021-03-29T17:27:44",
    "Total": 0.00512,
    "Yesterday": 0.00021,
    "Today": 0.00512,
    "Period": 0,
    "Power": 0,
    "ApparentPower": 0,
    "ReactivePower": 0,
    "Factor": 0,
    "Voltage": 236,
    "Current": 0
  }
}
```
The example above results in the following field contents in the *Smart Appliance Enabler* (whereby the parameter `meter reading` is recommended and "tasmota" must be replaced by the topic name configured for the Tasmota adapter):

| Feld                                                    | Wert                  |
|---------------------------------------------------------|-----------------------|
| Topic                                                   | tele/tasmota/SENSOR   |           
| Format                                                  | JSON                  |
| Value extraction path (using parameter `meter reading`) | $.ENERGY.Total        |
| Value extraction path (using parameter `power`)         | $.ENERGY.Power        |
| Time extraction path                                    | $.Time                |

**Attention:** In order to specify a time extraction, the time must be specified as local time (not UTC!) in the MQTT messages.

### HTTP
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


## Devices with Tasmota firmware as switches
### MQTT
Using MQTT as a switch results in the following field contents in the *Smart Appliance Enabler* (where "tasmota" must be replaced with the topic name configured for the Tasmota adapter):

| Field                      | Value              |
|----------------------------|--------------------|
| Topic                      | cmnd/tasmota/Power |
| Payload for swich-on       | ON                 |
| Payload for swich-off      | OFF                |
| State topic                | stat/tasmota/POWER |
| Regex for state extraktion | ON                 |

### HTTP
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
