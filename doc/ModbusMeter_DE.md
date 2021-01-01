# Modbus-Stromzähler

Stromzähler mit [Modbus](https://de.wikipedia.org/wiki/Modbus)-Protokoll erlauben die Abfrage diverser Werte, wobei jeder Wert aus einem bestimmten Register gelesen werden muss. Für den *Smart Appliance Enabler* ist lediglich der Wert *aktuelle Leistung* bzw. *active power* interessant.

Für Modbus-Schalter gelten die allgemeinen Hinweise zur Verwendung von [Modbus im SmartApplianceEnabler](Modbus_DE.md).

Bevor ein Modbus-basierter Zähler konfiguriert werden kann, muss ein [Modbus/TCP](Settings_DE.md#Modbus) konfiguriert werden, der dann in der Konfiguration des Modbus-Stromzählers ausgewählt wird. Auch Slave-Adresse muss angegeben werden.
Ausserdem kann ein ```Messinterval``` angegeben werden für die Durchschnittsberechnung der Leistungsaufnahme.

Der *Smart Appliance Enabler* verwendet den Modbus-Zähler sowohl zur Bestimmung der aktuellen Leistungsaufnahme als auch zum Messen von Energiemengen. Dafür muß jeweils die Register-Adresse und der Register-Typ eingegeben werden. Die restlichen Felder sind optional.

![Modbus-basierter Zähler](../pics/fe/ModbusMeter.png)

## Log
Wird ein Modbus-Zähler für das Gerät `F-00000001-000000000005-00` verwendet, kann man die ermittelte Leistungsaufnahme im [Log](Logging_DE.md) mit folgendem Befehl anzeigen:

```console
sae@raspi2:~ $ grep 'Modbus\|Register' /tmp/rolling-2020-12-30.log | grep F-00000001-000000000019-00
2020-12-30 14:33:51,483 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusSlave [ModbusSlave.java:76] F-00000001-000000000019-00: Connecting to modbus modbus@127.0.0.1:502
2020-12-30 14:33:51,546 DEBUG [http-nio-8080-exec-7] d.a.s.m.e.ReadFloatInputRegisterExecutorImpl [ReadInputRegisterExecutor.java:57] F-00000001-000000000019-00: Input register=342 value=[17668, 65470, 0, 0]
2020-12-30 14:33:51,550 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:219] F-00000001-000000000019-00: Float value=2127.984
2020-12-30 14:33:51,551 DEBUG [http-nio-8080-exec-7] d.a.s.m.ModbusElectricityMeter [ModbusElectricityMeter.java:88] F-00000001-000000000019-00: average power = 6895W
```

## Schaltbeispiel 1: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Messen des Stromverbrauchs eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen, wobei diese Schaltung natürlich um einen [Schalter](https://github.com/camueller/SmartApplianceEnabler/blob/master/README.md#schalter) erweitert werden kann, wenn neben dem Messen auch geschaltet werden soll.

![Schaltbeispiel](../pics/SchaltungModbusZaehler.jpg)
