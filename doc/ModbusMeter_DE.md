# Modbus-Stromzähler

Für Modbus-Schalter gelten die allgemeinen Hinweise zur Verwendung von [Modbus im SmartApplianceEnabler](Modbus_DE.md).

Nach Möglichkeit sollte als Parameter `Zählerstand` eingstellt werden, weil der *Smart Appliance Enabler* dann diesen Wert **nur einmal pro Minute abfragen** muss und aus der Differenz zur vorangegangen Anfrage die Leistung sehr genau berechnet kann.

Wird als Parameter `Leistung` eingestellt, erfolgt die Abfrage dieses Wertes mehrmals pro Minute, um aus diesen Werten den Durschnitt zu berechnen. Der zeitliche Abstand zwischen diesen Abfragen kann mit dem `Abfrage-Intervall` festgelegt werden - der Standardwert sind 20 Sekunden.

Für den Parameter `Zählerstand` wird der Wert in kWh und für den Parameter `Leistung` in W benötigt. Falls die Werte in anderen Einheiten geliefert werden, muss ein muss ein `Umrechnungsfaktor` angegeben werden, der mit dem gelieferten Wert multipliziert wird, um ihn in die benötigte Einheit umzurechnen. Wird beispielsweise der Parameter `Leistung` in mW geliefert, muss als `Umrechnungsfaktor` der Wert `1000` angegeben werden.

![Modbus-basierter Zähler](../pics/fe/ModbusMeter.png)

## Log
Wird ein Modbus-Zähler für das Gerät `F-00000001-000000000005-00` verwendet, kann man die ermittelte Leistungsaufnahme im [Log](Logging_DE.md) mit folgendem Befehl anzeigen:

```console
sae@raspi:~ $ grep 'Modbus\|Register' /tmp/rolling-2020-12-30.log | grep F-00000001-000000000019-00
2020-12-30 14:33:51,483 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusSlave [ModbusSlave.java:76] F-00000001-000000000019-00: Connecting to modbus modbus@127.0.0.1:502
2020-12-30 14:33:51,546 DEBUG [http-nio-8080-exec-7] d.a.s.m.e.ReadFloatInputRegisterExecutorImpl [ReadInputRegisterExecutor.java:57] F-00000001-000000000019-00: Input register=342 value=[17668, 65470, 0, 0]
2020-12-30 14:33:51,550 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:219] F-00000001-000000000019-00: Float value=2127.984
2020-12-30 14:33:51,551 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:88] F-00000001-000000000019-00: average power = 6895W
```

*Webmin*: In [View Logfile](Logging_DE.md#user-content-webmin-logs) gibt man hinter `Only show lines with text` ein `Modbus` und drückt Refresh.

## Schaltbeispiel: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Messen des Stromverbrauchs eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen, wobei diese Schaltung natürlich um einen Schalter erweitert werden kann, wenn neben dem Messen auch geschaltet werden soll.

![Schaltbeispiel](../pics/SchaltungModbusZaehler.jpg)
