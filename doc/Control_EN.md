# Controls
In order to configure a control, its type must be selected in the `Type` selection box.

The *Smart Appliance Enabler* currently supports the following controls:

* [Meter reporting switch](MeterReportingSwitch_EN.md) is automatically active if no other control is configured
* [GPIO](GPIOSwitch_EN.md)
* [Modbus](ModbusSwitch_EN.md) only appears if at least one Modbus has been configured in the [Settings](Settings_EN.md#modbus)
* [HTTP](HttpSwitch_EN.md)
* [Level switch](LevelSwitch_EN.md)
* [PWM switch](PwmSwitch_EN.md)
* [Always-on switch](AlwaysOnSwitch_EN.md)

According to this selection, the fields that can be configured for the selected control type are displayed.

If the selected control type can be combined with [Starting current detection](StartingCurrentDetection_EN.md) and this has been activated by clicking the checkbox, further fields with configuration parameters of the starting current detection are displayed.

To use the SG-Ready function of heat pumps, some switch types can be marked as [switch-on option](SwitchOnOption_EN.md).

If `EV charger` is specified as the appliance type, the [wallbox configuration and vehicle management](EVCharger_EN.md) can be carried out on this page.

See also: [General notes on configuration](Configuration_EN.md)

## <a name="control-request"></a> Control requests from the Sunny Home Manager

If a control request is received from the *Sunny Home Manager* for an appliance, this leads to a corresponding log entry that can be displayed with the following command:

```console
sae@raspi:~ $ grep "control request" /tmp/rolling-2020-12-30.log
2020-12-30 14:30:09,977 DEBUG [http-nio-8080-exec-9] d.a.s.s.w.SempController [SempController.java:235] F-00000001-000000000019-00: Received control request: on=true, recommendedPowerConsumption=22000W
```

*Webmin*: In [View Logfile](#webmin-logs) enter `control request` after `Only show lines with text` and press refresh.
