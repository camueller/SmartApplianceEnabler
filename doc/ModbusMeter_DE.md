# Modbus-Stromzähler

Für Modbus-Stromzähler gelten die allgemeinen Hinweise zur [Modbus-Unterstützung in *Smart Appliance Enabler*](doc/Modbus_DE.md).

Stromzähler mit [Modbus](https://de.wikipedia.org/wiki/Modbus)-Protokoll erlauben die Abfrage diverser Werte, wobei jeder Wert aus einem bestimmten Register gelesen werden muss. Für den *Smart Appliance Enabler* ist lediglich der Wert *aktuelle Leistung* bzw. *active power* interessant. Wird ein Modbus-Zähler verwendet, finden sich in der Log-Datei ```/var/log/smartapplianceenabler.log``` für jede Abfrage folgende Zeilen:
```
2016-09-19 19:39:25,010 DEBUG [Timer-0] d.a.s.m.ModbusSlave [ModbusSlave.java:77] F-00000001-000000000001-00: Connecting to modbus modbus//127.0.0.1:502
2016-09-19 19:39:25,051 DEBUG [Timer-0] d.a.s.m.ReadInputRegisterExecutor [ReadInputRegisterExecutor.java:63] F-00000001-000000000001-00: Input register 0C: value=61.766785
2016-09-19 19:39:25,052 DEBUG [Timer-0] d.a.s.a.PollElectricityMeter [PollElectricityMeter.java:41] F-00000001-000000000001-00: timestamps added/removed/total: 1/0/2
```
