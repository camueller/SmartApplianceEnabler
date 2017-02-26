# Modbus-Stromzähler

Für Modbus-Stromzähler gelten die allgemeinen Hinweise zur [Modbus-Unterstützung in *Smart Appliance Enabler*](Modbus_DE.md).

Stromzähler mit [Modbus](https://de.wikipedia.org/wiki/Modbus)-Protokoll erlauben die Abfrage diverser Werte, wobei jeder Wert aus einem bestimmten Register gelesen werden muss. Für den *Smart Appliance Enabler* ist lediglich der Wert *aktuelle Leistung* bzw. *active power* interessant. Wird ein Modbus-Zähler verwendet, finden sich in der Log-Datei ```/var/log/smartapplianceenabler.log``` für jede Abfrage folgende Zeilen:
```
2016-09-19 19:39:25,010 DEBUG [Timer-0] d.a.s.m.ModbusSlave [ModbusSlave.java:77] F-00000001-000000000001-00: Connecting to modbus modbus//127.0.0.1:502
2016-09-19 19:39:25,051 DEBUG [Timer-0] d.a.s.m.ReadInputRegisterExecutor [ReadInputRegisterExecutor.java:63] F-00000001-000000000001-00: Input register 0C: value=61.766785
2016-09-19 19:39:25,052 DEBUG [Timer-0] d.a.s.a.PollElectricityMeter [PollElectricityMeter.java:41] F-00000001-000000000001-00: timestamps added/removed/total: 1/0/2
```

## Schaltbeispiel 1: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Messen des Stromverbrauchs eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen, wobei diese Schaltung natürlich um einen [Schalter](https://github.com/camueller/SmartApplianceEnabler/blob/master/README.md#schalter) erweitert werden kann, wenn neben dem Messen auch geschaltet werden soll.

![Schaltbeispiel](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SchaltungModbusZaehler.jpg)

Die Konfiguration für dieses Schaltbeispiel würde wie folgt aussehen, wobei die Registeradresse vom verwendeten Zähler abhängt:
```
<?xml version="1.0" encoding="UTF-8"?>
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <ModbusElectricityMeter idref="modbus" slaveAddress="1" registerAddress="0C" pollInterval="5" measurementInterval="60" />
    </Appliance>
    <Connectivity>
        <ModbusTCP id="modbus" host="127.0.0.1" />
    </Connectivity>
</Appliances>
```

Allgemeine Hinweise zu diesem Thema finden sich im Kapitel [Konfiguration](Configuration_DE.md).
