# Keba KeContact P30 c-series / x-series

[Keba](https://www.keba.com/de/emobility/elektromobilitaet) hat seine Wallboxen *KeContact P30 c-series* (Firmware-Version >3.10.16) und *KeContact P30 x-series* (Software-Version >1.11) mit der Unterstützung des Modbus-Protokolls aufgewertet, nachdem zuvor nur ein proprietäres UDP-Protokoll unterstützt wurde.

Bei Verwendung dieser Wallbox ist kein separater Stromzähler erforderlich, weil der Zählerwert von der Wallbox selbst mit hoher Genauigkeit bereitgestellt wird und der *Smart Appliance Enabler* daraus die aktuelle Leistungsaufnahme berechnet.

## Geräte-Konfiguration
Die Firmware-Version muss mindestens den o.g. Stand haben.

## Konfiguration im Smart Appliance Enabler

Für die Konfiguration sollte die Vorlage `Keba P30 c-series >3.10.16 / x-series >1.11` verwendet werden - dadurch werden alle Felder korrekt ausgefüllt. Lediglich die Modbus-Instanz muss noch gewählt werden.

![Konfiguration des go-eCharger als Schalter](../pics/fe/EVChargerKeba.png)

### Zähler

Wie oben geschrieben stellt die Wallbox selbst die Zählerwerte bereit, d.h. für die Zählerkonfiguration muss die gleiche Modbus-Instanz verwendet werden, wie für die Wallbox selbst.

Die `Slave-Adresse` muss auf 255 gesetzt werden wie bei der Wallbox auch.

Der Zählerstand wird in `Register 1036` bereitgestellt.

Der `Register-Typ` ist `Holding (FC=3)`.

Der Wert-Typ muss auf `Float als Integer` gesetzt werden.

Bei `Datenwörter` muss nichts eingegeben werden.

Der `Umrechnungsfaktor` muss auf `0.0001` gesetzt werden.

Als Zustand ist `Zählerwert` zu wählen.

![Konfiguration des go-eCharger als Zähler](../pics/fe/EVChargerKebaMeter.png)
