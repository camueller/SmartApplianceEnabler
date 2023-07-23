# Schalter
Um einen Schalter zu konfigurieren muss in der `Typ`-Auswahlbox der Typ des Schalters ausgewählt werden.

Derzeit unterstützt der *Smart Appliance Enabler* folgende Schalter:

* [Zählerbasierter Zustandsmelder](MeterReportingSwitch_DE.md) ist automatisch aktiv, wenn kein anderer Schalter konfiguriert wird
* [GPIO](GPIOSwitch_DE.md)
* [Modbus](ModbusSwitch_DE.md) erscheint nur, wenn in den [Einstellungen](Settings_DE.md#modbus) mindesten ein Modbus konfiguriert wurde
* [HTTP](HttpSwitch_DE.md)
* [Stufenschalter](LevelSwitch_DE.md)
* [PWM-Schalter](PwmSwitch_DE.md)
* [Immer eingeschaltet](AlwaysOnSwitch_DE.md)

Entsprechend dieser Auswahl werden die für den gewählten Schalter-Typ konfigurierbaren Felder eingeblendet.

Falls der gewählte Schalter-Typ mit [Anlaufstromerkennung](StartingCurrentDetection_DE.md) kombiniert werden kann und diese durch Anklicken der Checkbox aktiviert wurde, werden weitere Felder mit Konfigurationsparametern der Anlaufstromerkennung eingeblendet.

Zur Nutzung der SG-Ready-Funktion von Wärmepumpen können einige Schalter-Typen als [Einschaltoption](SwitchOnOption_DE.md) markiert werden.

Wenn als Gerätetyp `Elektroauto-Ladegerät` angegeben ist, kann auf dieser Seite die [Konfiguration der Wallbox sowie die Verwaltung der Fahrzeuge](EVCharger_DE.md) vorgenommen werden.

Siehe auch: [Allgemeine Hinweise zur Konfiguration](Configuration_DE.md)

## <a name="control-request"></a> Schaltbefehl vom Sunny Home Manager


Wenn ein Schaltbefehl vom *Sunny Home Manager* für ein Gerät empfangen wird, führt das zu einem entsprechenden Log-Eintrag, der mit folgendem Befehl angezeigt werden kann:

```console
sae@raspi:~ $ grep "control request" /tmp/rolling-2020-12-30.log
2020-12-30 14:30:09,977 DEBUG [http-nio-8080-exec-9] d.a.s.s.w.SempController [SempController.java:235] F-00000001-000000000019-00: Received control request: on=true, recommendedPowerConsumption=22000W
```

*Webmin*: In [View Logfile](#webmin-logs) gibt man hinter `Only show lines with text` ein `control request` und drückt Refresh.
