# Konfiguration von Wallboxen mit Modbus-Protokoll ohne Vorlage

Der *Smart Appliance Enabler* bringt für diverse Wallboxen Vorlagen mit, welche die zugehörige Konfiguration beinhalten. Wenn für die verwendete Wallbox keine Konfiguration vorhanden ist, diese aber über Modbus/TCP gesteuert werden kann, kann man auch selbst eine Konfiguration erstellen.

## Allgemeine Modbus-Konfiguration

Bevor die Wallbox-spezifische Modbus-Konfiguration erstellt werden kann, müssen diese [allgemeinen Voraussetzungen zur Nutzung von Modbus im Smart Appliance Enabler](Modbus_DE.md) erfüllt sein.   

## Wallbox-spezifische Modbus-Konfiguration

Die Wallbox-spezifische Modbus-Konfiguration des *Smart Appliance Enabler* besteht im Wesentlichen aus drei Teilen:

### Wallbox-Status

Das Auslesen des Wallbox-Status ist ein Lesezugriff.

Zur Bestimmung des Wallbox-Status müssen vier Parameter bestimmt werden:
- Ladestecker nicht angeschlossen
- Ladestecker angeschlossen
- Ladevorgang läuft
- Ladegerät meldet Fehler

Jeder Parameter muss zusammen mit einem [regulären Ausdruck/Regex](ValueExtraction_DE.md) einem Register zugeordnet werden, d.h. wenn der Wert in diesem Register auf den regulären Ausdruck passt, entspricht der Wallbox-Status diesem Parameter.

Es können mehrere Parameter demselben Register zugeordnet werden, wobei jeder Parameter einen anderen regulären Ausdruck haben sollte.

Wenn man einen Parameter mehreren Registern zuordnet, entspricht das einer UND-Verknüfung, d.h. der Wallbox-Status entspricht nur dann diesem Parameter, wenn alle zugeordneten Register einen Wert haben, der auf den jeweiligen regulären Ausdruck passt.

In der Modbus-Dokumentation der Wallbox sucht man bei den Input-Registern (Read) nach einem oder mehreren Registern, welche die o.g. vier Parameter des Status abbilden können.

_Beispiel:_

Bei Wallboxen mit Phoenix Contact EM-CP-PP-ETH-Controller ist der Status im Register 100 in Form eines Buchstabens enthalten:

| Parameter                       | Regulärer Ausdruck | Erklärung                                                                         |
| ------------------------------- | ------------------ | --------------------------------------------------------------------------------- |
| Ladestecker nicht angeschlossen | (A)                | Wenn im Register A steht, ist kein Fahrzeug angeschlossen                         |
| Ladestecker angeschlossen       | (B)                | Wenn im Register B steht, ist ein Fahrzeug angeschlossen, aber lädt aktuell nicht |
| Ladevorgang läuft               | (C&#124;D)         | Wenn im Register C oder D steht, lädt das Fahrzeug gerade                         |
| Ladegerät meldet Fehler         | (E&#124;F)         | Wenn im Register E oder F steht, ist das Ladegerät im Fehlerzustand               |

### Ladevorgang beginnen/beenden

Das Beginnen und Beenden des Ladevorganges sind zwei Aktionen, die einen Schreibzugriff darstellen.

In der Modbus-Dokumentation der Wallbox sucht man bei den Holding-Registern (Write) nach einem Register, welches das Setzen des Ladestatus erlaubt. Nach dem Beenden des Ladevorganges sollte der Ladevorgang erneut gestartet werden können, ohne das Fahrzeug von der Wallbox zu trennen.

_Beispiel:_

Bei Wallboxen mit Phoenix Contact EM-CP-PP-ETH-Controller erfolgt das Setzen des Ladestatus über das Register 400:   

| Parameter            | Wert | Erklärung                                                                              |
| -------------------- | ---- | -------------------------------------------------------------------------------------- |
| Ladevorgang beginnen | 1    | Um den Ladevorgang zu beginnen, muss der Wert 1 in dieses Register geschrieben werden. |
| Ladevorgang beenden  | 0    | Wird der Wert 0 in das Register geschrieben, wird der Ladevorgang beendet.             |

### Ladestromstärke setzen

Das Setzen der Ladestromstärke stellt ebenfalls einen Schreibzugriff dar.

In der Modbus-Dokumentation der Wallbox sucht man bei den Holding-Registern (Write) nach einem Register, welches das Setzen der Ladestromstärke erlaubt.

_Beispiel:_

Bei Wallboxen mit Phoenix Contact EM-CP-PP-ETH-Controller erfolgt das Setzen der Ladestromstärke über das Register 300:

| Parameter              | Wert | Erklärung                                                                                        |
| ---------------------- | ---- | ------------------------------------------------------------------------------------------------ |
| Ladestromstärke setzen | 0    | Der Wert ist ein Pflichtfeld. Es kann 0 eingetragen werden, weil der konkrete Wert variabel ist. |
