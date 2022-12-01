# PWM switch
A PWM switch enables consumers with **variable power consumption** to be controlled if they can be controlled via [PWM (pulse width modulation)](https://de.wikipedia.org/wiki/Pulsdurationmodulation). The **GPIO ports** of the Raspberry Pi can not only be switched on and off, but also send a PWM signal.

In the [Device Settings](Appliance_EN.md) a value for the *Min. Power consumption* can be entered. In addition, an [Excess Energy Schedule](Schedules_EN.md) must be configured.

The figure below shows the PWM settings for a model making servo:

![PWM Switch](../pics/fe/PwmSwitch.png)

When configuring the GPIO connection, it is essential to observe the [Notes on the Raspberry Pi and the numbering of the GPIO connections](Raspberry_EN.md)!

For the configuration of the PWM signal at least the `PWM frequency` and the `Max. duty cycle` can be specified. The latter is the ratio of the pulse duration to the period duration (in percent) and expresses the maximum power consumption.

Optionally, if required, the `Min. Duty cycle` can be specified, which expresses the ratio of pulse duration to period duration (in percent) at minimum power consumption.

## Log
If a device (here `F-00000001-000000000001-00`) is controlled with a configured PWM switch, the control commands can be displayed in [Log](Logging_EN.md) with the following command:

```console
sae@raspi2:~ $ grep "c.PwmSwitch" /tmp/rolling-2022-03-27.log | grep F-00000001-000000000001-00
2022-03-27 08:00:49,798 INFO [MQTT Call: F-00000001-000000000001-00-PwmSwitch] d.a.s.c.PwmSwitch [PwmSwitch.java:144] F-00000001-000000000001-00: Setting power to 2000
2022-03-27 08:00:49,799 INFO [MQTT Call: F-00000001-000000000001-00-PwmSwitch] d.a.s.c.PwmSwitch [PwmSwitch.java:158] F-00000001-000000000001-00: Setting GPIO 17 duty cycle to 310
```

*Webmin*: In [View Logfile](Logging_EN.md#user-content-webmin-logs) enter `c.PwmSwitch` after `Only show lines with text` and press refresh.