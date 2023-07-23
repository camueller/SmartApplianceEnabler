# MQTT-Topics und Nachrichten
Bei der MQTT-Kommunikation unterscheidet der *Smart Appliance Enabler* zwischen:
- Nachrichten
- Ereignissen

## Nachrichten
Der *Smart Appliance Enabler* sendet die folgenden MQTT-Nachrichten entweder einmalig (mit dem Flag `Retained`) oder periodisch.  


### ApplianceInfoMessage
#### Kontext
Eine `ApplianceInfoMessage` wird einmalig während der Initialisierung gesendet.

#### Topic
`/sae/<appliance_id>/ApplianceInfoMessage`

_Beispiel_

`/sae/F-12345678-000000000001-00/ApplianceInfoMessage`

#### Inhalt
| Feld          | Typ           | Beipiel-Wert               | Beschreibung                |
|---------------|---------------|----------------------------|-----------------------------|
| applianceInfo | ApplianceInfo | siehe unten                | Informationen zum Appliance |
| time          | String        | 2022-12-27T19:11:25.952152 | Erzeugungszeitpunkt         |
| type          | String        | MeterMessage               | Art der Nachricht           |

Die Struktur von `ApplianceInfo` sieht wie folgt aus:

| Feld                 | Typ     | Beipiel-Wert               | Beschreibung                                                                   |
|----------------------|---------|----------------------------|--------------------------------------------------------------------------------|
| currentPowerMethod   | String  | Measurement                | Art der Leistungsermittlung                                                    |
| id                   | String  | F-12345678-000000000001-00 | [Appliance-Konfiguration: ID](Appliance_DE.md)                                 |
| interruptionsAllowed | Boolean | false                      | [Appliance-Konfiguration: Unterbrechnung erlaubt](Appliance_DE.md)             |
| maxOffTime           | Integer | 0                          | [Appliance-Konfiguration: Max. Ausschaltdauer](Appliance_DE.md)                |
| maxOnTime            | Integer | 0                          | [Appliance-Konfiguration: Max. Einschaltdauer](Appliance_DE.md)                |
| maxPowerConsumption  | Integer | 4000                       | [Appliance-Konfiguration: Max. Leistungsaufnahme](Appliance_DE.md)             |
| minOffTime           | Integer | 0                          | [Appliance-Konfiguration: Min. Ausschaltdauer](Appliance_DE.md)                |
| minOnTime            | Integer | 0                          | [Appliance-Konfiguration: Min. Einschaltdauer](Appliance_DE.md)                |
| minPowerConsumption  | Integer | null                       | [Appliance-Konfiguration: Min. Leistungsaufnahme](Appliance_DE.md)             |
| name                 | String  | Vitocal 300                | [Appliance-Konfiguration: Bezeichnung](Appliance_DE.md)                        |
| notificationSenderId | String  | null                       | [Appliance-Konfiguration: Absender-ID für Benachrichtigungen](Appliance_DE.md) |
| serial               | String  | SE12345                    | [Appliance-Konfiguration: Seriennummer](Appliance_DE.md)                       |
| type                 | String  | DishWasher                 | [Appliance-Konfiguration: Typ](Appliance_DE.md)                                |
| vendor               | String  | Viessmann                  | [Appliance-Konfiguration: Hersteller](Appliance_DE.md)                         |


### ControlMessage
#### Kontext
Eine `ControlMessage` wird jedes Mal gesendet, wenn der Status des Schalters ermittelt wurde.

Auch zum Schalten wird sie verwendet.

#### Topic
##### Nach Ermittlung des Status des Schalters
`/sae/<appliance_id>/Control`

_Beispiel_

`/sae/F-12345678-000000000001-00/Control`

##### Zum Schalten
`/sae/<appliance_id>/Control/set`

_Beispiel_

`/sae/F-12345678-000000000001-00/Control/set`

#### Inhalt
| Feld   | Typ     | Beipiel-Wert               | Beschreibung                                                        |
|--------|---------|----------------------------|---------------------------------------------------------------------|
| on     | Boolean | true                       | Eingeschaltet/Einschalten = true; Ausgeschaltet/Ausschalten = false |
| time   | String  | 2022-12-27T19:11:25.952152 | Erzeugungszeitpunkt                                                 |
| type   | String  | MeterMessage               | Art der Nachricht                                                   |


### MeterMessage
#### Kontext
Eine `MeterMessage` wird jedes Mal gesendet, wenn neue Werte für „Leistung“ oder „Energie“ verfügbar sind.

#### Topic
`/sae/<appliance_id>/Meter`

_Beispiel_

`/sae/F-12345678-000000000001-00/Meter`

#### Inhalt
| Feld   | Typ     | Beipiel-Wert               | Beschreibung        |
|--------|---------|----------------------------|---------------------|
| energy | Float   | 2.56783                    | Energie in kWh      |
| power  | Integer | 131                        | Leistung in W       |
| time   | String  | 2022-12-27T19:11:25.952152 | Erzeugungszeitpunkt |
| type   | String  | MeterMessage               | Art der Nachricht   |


### StartingCurrentSwitchMessage
#### Kontext
Eine `StartingCurrentSwitchMessage` ist eine erweiterte `ControlMessage` und wird jedes Mal gesendet, wenn der Status eines Schalters mit Anlaufstromerkennung ermittelt wurde.

#### Topic
`/sae/<appliance_id>/Control`

_Beispiel_

`/sae/F-12345678-000000000001-00/Control`

#### Inhalt
Zusätzlich zu den Feldern der `ControlMessage` sind folgende Felder enthalten: 

| Feld                              | Typ     | Beipiel-Wert | Beschreibung                                                                   |
|-----------------------------------|---------|--------------|--------------------------------------------------------------------------------|
| powerThreshold                    | Integer | 15           | [Leistungsschaltgrenze in W](StartingCurrentDetection_DE.md)                   |
| startingCurrentDetectionDuration  | Integer | 30           | [Überschreitungsdauer beim Einschalten in s](StartingCurrentDetection_DE.md)   |
| finishedCurrentDetectionDuration  | Integer | 300          | [Unterschreitungsdauer beim Ausschalten in s](StartingCurrentDetection_DE.md)  |


### SwitchOptionMessage
#### Kontext
Eine `SwitchOptionMessage` ist eine erweiterte `ControlMessage` und wird jedes Mal gesendet, wenn der Status eines Schalters mit Einschaltoption ermittelt wurde.

#### Topic
`/sae/<appliance_id>/Control`

_Beispiel_

`/sae/F-12345678-000000000001-00/Control`

#### Inhalt
Zusätzlich zu den Feldern der `ControlMessage` sind folgende Felder enthalten:

| Feld                        | Typ     | Beipiel-Wert | Beschreibung                                               |
|-----------------------------|---------|--------------|------------------------------------------------------------|
| powerThreshold              | Integer | 15           | [Leistungsschaltgrenze in W](SwitchOnOption_DE.md)         |
| switchOnDetectionDuration   | Integer | 30           | [Dauer der Einschalterkennung in s](SwitchOnOption_DE.md)  |
| switchOffDetectionDuration  | Integer | 300          | [Dauer der Ausschalterkennung in s](SwitchOnOption_DE.md)  |


### TimeframeIntervalQueueMessage
#### Kontext
Eine `TimeframeIntervalQueueMessage` wird nur dann gesendet, wenn die Queue gefüllt oder geändert wird (mit dem Flag `Retained`).

#### Topic
`/sae/<appliance_id>/TimeframeIntervalQueueMessage`

_Beispiel_

`/sae/F-12345678-000000000001-00/TimeframeIntervalQueueMessage`

#### Inhalt
| Feld     | Typ                           | Beipiel-Wert               | Beschreibung                       |
|----------|-------------------------------|----------------------------|------------------------------------|
| entries  | TimeframeIntervalQueueEntry[] | siehe unten                | Liste der Zeitfenster in der Queue |
| time     | String                        | 2022-12-27T19:11:25.952152 | Erzeugungszeitpunkt                |
| type     | String                        | MeterMessage               | Art der Nachricht                  |

Die Struktur eines `TimeframeIntervalQueueEntry` sieht dabei wie folgt aus:

| Feld     | Typ     | Beipiel-Wert        | Beschreibung                                                  |
|----------|---------|---------------------|---------------------------------------------------------------|
| state    | String  | ACTIVE              | Status des Zeitfensters                                       |
| start    | String  | 2022-12-27T10:00:00 | Beginn des Zeitfensters                                       |
| end      | String  | 2022-12-27T18:00:00 | Ende des Zeitfensters                                         |
| type     | String  | RuntimeRequest      | Art des Requests                                              |
| min      | Integer | null                | min. Laufzeit/Energie                                         |
| max      | Integer | 10800               | max. Laufzeit/Energie                                         |
| enabled  | Boolean | true                | Anforderung aktiviert = true; Anforderung deaktiviert = false |


### VariablePowerConsumerMessage
#### Kontext
Eine `VariablePowerConsumerMessage` ist eine erweiterte `ControlMessage` und wird jedes Mal gesendet, wenn der Status eines Schalters für ein Gerät mit variabler Leistungsaufnahme ermittelt wurde.

Auch zum Schalten wird sie verwendet.


#### Topic
##### Nach Ermittlung des Status des Schalters
`/sae/<appliance_id>/Control`

_Beispiel_

`/sae/F-12345678-000000000001-00/Control`

##### Zum Schalten
`/sae/<appliance_id>/Control/set`

_Beispiel_

`/sae/F-12345678-000000000001-00/Control/set`

#### Inhalt
Zusätzlich zu den Feldern der `ControlMessage` sind folgende Felder enthalten:

| Feld               | Typ     | Beipiel-Wert | Beschreibung                                                                            |
|--------------------|---------|--------------|-----------------------------------------------------------------------------------------|
| power              | Integer | 1200         | aktuelle/geforderte Leistungsaufnahme                                                   |
| useOptionalEnergy  | Boolean | false        | Überschussenergie wird verwendet = true; keine Überschussenergie wird verwendet = false |



## Ereignisse
Ereignisse sind ebenfalls MQTT-Nachrichten, enthalten nach der Appliance-ID im Topic jedoch auch noch die Gruppierungsebende `Event`:

`/sae/<appliance_id>/Event/...`

Der *Smart Appliance Enabler* sendet Ereignisse nur, wenn sie auftreten.

Standardmäßig haben alle Ereignisse folgenden Inhalt: 

| Feld   | Typ     | Beipiel-Wert               | Beschreibung         |
|--------|---------|----------------------------|----------------------|
| time   | String  | 2022-12-27T19:11:25.952152 | Erzeugungszeitpunkt  |
| type   | String  | MqttMesage                 | Art der Nachricht    |


### EnableRuntimeRequest
#### Kontext
Wenn `EnableRuntimeRequest` empfangen wird, wird die Anforderung des ersten Zeitfensters auf `enabled` gesetzt.

Anforderungen sind jedoch standardmäßig aktiviert, außer sie sind mit „externe Aktivierung“ gekennzeichnet.

#### Topic
`/sae/<appliance_id>/Event/EnableRuntimeRequest`

_Beispiel_

`/sae/F-12345678-000000000001-00/Event/EnableRuntimeRequest`


### DisableRuntimeRequest
#### Kontext
Wenn `DisableRuntimeRequest` empfangen wird, wird die Anforderung des ersten Zeitfensters auf `disabled` gesetzt.

#### Topic
`/sae/<appliance_id>/Event/DisableRuntimeRequest`

_Example_

`/sae/F-12345678-000000000001-00/Event/DisableRuntimeRequest`


### EVChargerSocChangedEvent
#### Kontext
Wenn sich der berechnete oder vom SOC-Script gelieferte SOC ändert, wird dieses Event gesendet.

#### Topic
`/sae/<appliance_id>/Event/EVChargerSocChangedEvent`

_Example_

`/sae/F-12345678-000000000001-00/Event/EVChargerSocChangedEvent`

#### Inhalt
Zusätzlich zu den gemeinsamen Feldern jedes Ereignisses sind die folgenden Felder enthalten:

| Feld              | Typ       | Beipiel-Wert               | Beschreibung                                 |
|-------------------|-----------|----------------------------|----------------------------------------------|
| chargeLoss        | Float     | 11.123                     | aktuelle Ladeverluste in Prozent             |
| socValues         | SocValues | siehe unten                | SOC zu verschiedenen Zeitpunkten             |
| socInitialTime    | String    | 2022-12-27T19:11:25.952152 | Zeitpunkt für den initialen SOC              |
| socRetrievedTime  | String    | 2022-12-27T19:11:25.952152 | Zeitpunkt des vom SOC-Script gelieferten SOC |

Die Struktur von `SocValues` sieht wie folgt aus:

| Feld            | Typ      | Beipiel-Wert | Beschreibung                                                 |
|-----------------|----------|--------------|--------------------------------------------------------------|
| batteryCapacity | Integer  | 36000        | [Fahrzeug-Konfiguration: Batteriekapazität](EVCharger_DE.md) |
| current         | Integer  | 52           | aktueller, berechneter SOC                                   |
| initial         | Integer  | 19           | SOC beim Verbinden mit der Wallbox                           |
| retrieved       | Integer  | 40           | SOC geliefert von der letzten Ausführung des SOC-Scripts     |


### EVChargerStateChangedEvent
#### Kontext
Wenn sich der Status der Wallbox ändert, wird dieses Event gesendet.

#### Topic
`/sae/<appliance_id>/Event/EVChargerStateChangedEvent`

_Example_

`/sae/F-12345678-000000000001-00/Event/EVChargerStateChangedEvent`

#### Inhalt
Zusätzlich zu den gemeinsamen Feldern jedes Ereignisses sind die folgenden Felder enthalten:

| Feld          | Typ     | Beipiel-Wert               | Beschreibung                                                                                                                                                                    |
|---------------|---------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| evId          | Integer | 1                          | ID des verbundenen Fahrzeugs                                                                                                                                                    |
| newState      | String  | VEHICLE_CONNECTED          | neuer [Status](https://github.com/camueller/SmartApplianceEnabler/blob/EnableRuntimeRequest/src/main/java/de/avanux/smartapplianceenabler/control/ev/EVChargerState.java)       |
| previousState | String  | VEHICLE_NOT_CONNECTED      | bisheriger [Status](https://github.com/camueller/SmartApplianceEnabler/blob/EnableRuntimeRequest/src/main/java/de/avanux/smartapplianceenabler/control/ev/EVChargerState.java)  |


### SempDevice2EM
#### Kontext
Dieses Event wird gesendet, wenn der *Sunny Home Manager* alle Daten vom *Smart Appliance Enabler* abholt.

#### Topic
`/sae/<appliance_id>/Event/SempDevice2EM`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempDevice2EM`


### SempEM2Device
#### Kontext
Dieses Event wird gesendet, wenn der *Sunny Home Manager* einen Schaltbefehl an den *Smart Appliance Enabler* sendet.

#### Topic
`/sae/<appliance_id>/Event/SempEM2Device`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempEM2Device`


### SempGetDeviceInfo
#### Kontext
Dieses Event wird gesendet, wenn der *Sunny Home Manager* lediglich die Geräte-Information vom *Smart Appliance Enabler* abholt.

#### Topic
`/sae/<appliance_id>/Event/SempGetDeviceInfo`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempGetDeviceInfo`


### SempGetDeviceStatus
#### Kontext
Dieses Event wird gesendet, wenn der *Sunny Home Manager* lediglich den Geräte-Status vom *Smart Appliance Enabler* abholt.

#### Topic
`/sae/<appliance_id>/Event/SempGetDeviceStatus`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempGetDeviceStatus`


### WrappedControlSwitchOffDetected
#### Kontext
Dieses Event wird gesendet, wenn sich der reale Schaltzustand eines Gerätes mit Anlaufstromerkennung oder Einschaltoption ändert.

#### Topic
`/sae/<appliance_id>/Event/WrappedControlSwitchOffDetected`

_Example_

`/sae/F-12345678-000000000001-00/Event/WrappedControlSwitchOffDetected`



## Using REST API to publish MQTT messages

TBD: https://www.home-assistant.io/integrations/mqtt/#rest-api
