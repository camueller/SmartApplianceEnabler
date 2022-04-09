# Schalter

Um einen Schalter zu konfigurieren muss in der `Typ`-Auswahlbox der Typ des Schalters ausgewählt werden.

Derzeit unterstützt der *Smart Appliance Enabler* folgende Schalter:

* [Zählerbasierter Zustandsmelder](MeterReportingSwitch_DE.md) ist automatisch aktiv, wenn kein anderer Schalter konfiguriert wird
* [GPIO](GPIOSwitch_DE.md)
* [Modbus](ModbusSwitch_DE.md) erscheint nur, wenn in den [Einstellungen](Settings_DE.md#user-content-modbus) mindesten ein Modbus konfiguriert wurde
* [HTTP](HttpSwitch_DE.md)
* [Stufenschalter](LevelSwitch_DE.md)
* [PWM-Schalter](PwmSwitch_DE.md)
* [Immer eingeschaltet](AlwaysOnSwitch_DE.md)

Entsprechend dieser Auswahl werden die für den gewählten Schalter-Typ konfigurierbaren Felder eingeblendet.

Falls der gewählte Schalter-Typ mit [Anlaufstromerkennung](Anlaufstromerkennung_DE.md) kombiniert werden kann und diese durch Anklicken der Checkbox aktiviert wurde, werden weitere Felder mit Konfigurationsparametern der Anlaufstromerkennung eingeblendet.

Wenn als Gerätetyp `Elektroauto-Ladegerät` angegeben ist, kann auf dieser Seite die [Konfiguration der Wallbox sowie die Verwaltung der Fahrzeuge](EVCharger_DE.md) vorgenommen werden.

Siehe auch: [Allgemeine Hinweise zur Konfiguration](Configuration_DE.md)