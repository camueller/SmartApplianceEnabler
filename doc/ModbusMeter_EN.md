# Modbus meter

The general notes on using [Modbus in the SmartApplianceEnabler](Modbus_DE.md) apply to Modbus switches.

If possible, `Meter reading` should be set as the parameter, because the *Smart Appliance Enabler* then only has to query this value **once per minute** and can calculate the power very precisely from the difference to the previous query.

If `Power` is set as the parameter, this value is queried several times per minute in order to calculate the average from these values. The interval between these queries can be specified with the `poll Interval` - the default value is 20 seconds.

The value in kWh is required for the `Meter reading` parameter and in W for the `Power` parameter. If the values are supplied in other units, a `factor to value` must be specified, which is multiplied by the supplied value to convert it into the required unit. For example, if the parameter `Power` is supplied in mW, the value `1000` must be specified as the `factor to value`.

![Modbus-basierter Zähler](../pics/fe/ModbusMeter.png)

## Log
If a Modbus meter is used for the device `F-00000001-000000000005-00`, the determined power consumption can be displayed in [Log](Logging_DE.md) with the following command:

```console
sae@raspi:~ $ grep 'Modbus\|Register' /tmp/rolling-2020-12-30.log | grep F-00000001-000000000019-00
2020-12-30 14:33:51,483 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusSlave [ModbusSlave.java:76] F-00000001-000000000019-00: Connecting to modbus modbus@127.0.0.1:502
2020-12-30 14:33:51,546 DEBUG [http-nio-8080-exec-7] d.a.s.m.e.ReadFloatInputRegisterExecutorImpl [ReadInputRegisterExecutor.java:57] F-00000001-000000000019-00: Input register=342 value=[17668, 65470, 0, 0]
2020-12-30 14:33:51,550 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:219] F-00000001-000000000019-00: Float value=2127.984
2020-12-30 14:33:51,551 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:88] F-00000001-000000000019-00: average power = 6895W
```

*Webmin*: In [View Logfile](Logging_DE.md#user-content-webmin-logs) enter `Modbus` after `Only show lines with text` and press Refresh.

## Wiring example: 240V device with power consumption measurement
The structure for measuring the power consumption of a 240V device (e.g. pump) could look like this, whereby this circuit can of course be expanded with a switch if switching is also to be carried out in addition to measuring.

![Schaltbeispiel](../pics/SchaltungModbusZaehler.jpg)
