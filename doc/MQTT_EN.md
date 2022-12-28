# MQTT topics and messages
In MQTT communication, the *Smart Appliance Enabler* distinguishes between:
- Messages
- Events

## Messages
The *Smart Appliance Enabler* sends the following MQTT messages either once (with the flag `Retained`) or periodically.


### ApplianceInfoMessage
#### Context
An `ApplianceInfoMessage` is sent once during initialization.

#### Topic
`/sae/<appliance_id>/ApplianceInfoMessage`

_Example_

`/sae/F-12345678-000000000001-00/ApplianceInfoMessage`

#### Payload

| field         | type           | example value               | description                      |
|---------------|----------------|-----------------------------|----------------------------------|
| applianceInfo | ApplianceInfo  | see below                   | Information about the appliance  |
| times         | String         | 2022-12-27T19:11:25.952152  | Creation timestamp               |
| type          | String         | MeterMessage                | Type of message                  |

The structure of `ApplianceInfo` looks like this:

| field         | type           | example value               | description                                                            |
|----------------------|---------|----------------------------|-------------------------------------------------------------------------|
| currentPowerMethod   | String  | Measurement                | Type of performance assessment                                          |
| id                   | String  | F-12345678-000000000001-00 | [Appliance configuration: ID](Appliance_EN.md)                          |
| interruptionsAllowed | Boolean | false                      | [Appliance configuration: Interruption allowed](Appliance_EN.md)        |
| maxOffTime           | Integer | 0                          | [Appliance configuration: Max. switch-off time](Appliance_EN.md)        |
| maxOnTime            | Integer | 0                          | [Appliance configuration: Max. switch-on time](Appliance_EN.md)         |
| maxPowerConsumption  | Integer | 4000                       | [Appliance configuration: Max. power consumption](Appliance_EN.md)      |
| minOffTime           | Integer | 0                          | [Appliance configuration: Min. switch-off time](Appliance_EN.md)        |
| minOnTime            | Integer | 0                          | [Appliance configuration: Min. switch-on time](Appliance_EN.md)         |
| minPowerConsumption  | Integer | null                       | [Appliance configuration: Min. power consumption](Appliance_EN.md)      |
| name                 | String  | Vitocal 300                | [Appliance configuration: Bezeichnung](Appliance_EN.md)                 |
| notificationSenderId | String  | null                       | [Appliance configuration: Sender ID for notifications](Appliance_EN.md) |
| serial               | String  | SE12345                    | [Appliance configuration: Serial number](Appliance_EN.md)               |
| type                 | String  | DishWasher                 | [Appliance configuration: Type](Appliance_EN.md)                        |
| vendor               | String  | Viessmann                  | [Appliance configuration: Manufacturer](Appliance_EN.md)                |


### ControlMessage
#### Context
A `ControlMessage` is sent each time the status of the switch is determined.

It is also used for switching.

#### Topic
##### After determining the status of the switch
`/sae/<appliance_id>/Control`

_Example_

`/sae/F-12345678-000000000001-00/Control`

##### To switch
`/sae/<appliance_id>/Control/set`

_Example_

`/sae/F-12345678-000000000001-00/Control/set`

#### Payload
| field  | type    | example value              | description                                  |
|--------|---------|----------------------------|----------------------------------------------|
| on     | Boolean | true                       | switch(ed) on = true; switch(ed) off = false |
| time   | String  | 2022-12-27T19:11:25.952152 | Creation timestamp                           |
| type   | String  | MeterMessage               | Type of message                              |


### MeterMessage
#### Context
A `MeterMessage` is sent whenever new values for 'Power' or 'Energy' are available.

#### Topic
`/sae/<appliance_id>/Meter`

_Example_

`/sae/F-12345678-000000000001-00/Meter`

#### Payload
| field  | type    | example value              | description        |
|--------|---------|----------------------------|--------------------|
| energy | Float   | 2.56783                    | Energy in kWh      |
| power  | Integer | 131                        | Power in W         |
| time   | String  | 2022-12-27T19:11:25.952152 | Creation timestamp |
| type   | String  | MeterMessage               | Type of message    |


### StartingCurrentSwitchMessage
#### Context
A `StartingCurrentSwitchMessage` is an extended `ControlMessage` and is sent whenever the status of a switch with starting current detection is determined.

#### Topic
`/sae/<appliance_id>/Control`

_Example_

`/sae/F-12345678-000000000001-00/Control`

#### Payload
In addition to the fields of the `ControlMessage`, the following fields are included:

| field                            | type    | example value   | description                                                                                                          |
|----------------------------------|---------|-----------------|----------------------------------------------------------------------------------------------------------------------|
| powerThreshold                   | Integer | 15              | [power threshold in W](StartingCurrentDetection_EN.md)                                                               |
| startingCurrentDetectionDuration | Integer | 30              | [Duration of transgresion during switch on](StartingCurrentDetection_EN.md)                                          |
| finishedCurrentDetectionDuration | Integer | 300             | [Duration of undershoot durng switch off in s](StartingCurrentDetection_EN.md) |


### SwitchOptionMessage
#### Context
A `SwitchOptionMessage` is an extended `ControlMessage` and is sent whenever the status of a switch with a switch-on option is determined.

#### Topic
`/sae/<appliance_id>/Control`

_Example_

`/sae/F-12345678-000000000001-00/Control`

#### Payload
In addition to the fields of the `ControlMessage`, the following fields are included:

| field                      | type    | example value  | description                                                |
|----------------------------|---------|----------------|------------------------------------------------------------|
| powerThreshold             | Integer | 15             | [power threshold in W](SwitchOnOption_EN.md)               |
| switchOnDetectionDuration  | Integer | 30             | [Switch-on detection duration in s](SwitchOnOption_EN.md)  |
| switchOffDetectionDuration | Integer | 300            | [Switch-off detection duration in s](SwitchOnOption_EN.md) |


### TimeframeIntervalQueueMessage
#### Context
A `TimeframeIntervalQueueMessage` is only sent when the queue is filled or changed (with the flag `Retained`).

#### Topic
`/sae/<appliance_id>/TimeframeIntervalQueueMessage`

_Example_

`/sae/F-12345678-000000000001-00/TimeframeIntervalQueueMessage`

#### Payload
| field    | type                          | example value              | description                     |
|----------|-------------------------------|----------------------------|---------------------------------|
| entries  | TimeframeIntervalQueueEntry[] | see below                  | List of timeframes in the queue |
| time     | String                        | 2022-12-27T19:11:25.952152 | Creation timestamp              |
| type     | String                        | MeterMessage               | Type of message                 |

The structure of a `TimeframeIntervalQueueEntry` looks like this:

| field    | type    | example value       | description                                      |
|----------|---------|---------------------|--------------------------------------------------|
| state    | String  | ACTIVE              | Status of timeframe                              |
| start    | String  | 2022-12-27T10:00:00 | Begin of timeframe                               |
| end      | String  | 2022-12-27T18:00:00 | End of timeframe                                 |
| type     | String  | RuntimeRequest      | Type of request                                  |
| min      | Integer | null                | min. Runtime/Energy                              |
| max      | Integer | 10800               | max. Runtime/Energy                              |
| enabled  | Boolean | true                | Request enabled = true; Request disabled = false |


### VariablePowerConsumerMessage
#### Context
A `VariablePowerConsumerMessage` is an extended `ControlMessage` and is sent whenever the status of a switch for a device with variable power consumption is determined.

It is also used for switching.


#### Topic
##### After determining the status of the switch
`/sae/<appliance_id>/Control`

_Example_

`/sae/F-12345678-000000000001-00/Control`

##### To switch
`/sae/<appliance_id>/Control/set`

_Example_

`/sae/F-12345678-000000000001-00/Control/set`

#### Payload
In addition to the fields of the `ControlMessage`, the following fields are included:

| field             | type    | example value  | description                                                     |
|-------------------|---------|----------------|-----------------------------------------------------------------|
| power             | Integer | 1200           | current/requested power consumption                             |
| useOptionalEnergy | Boolean | false          | Excess energy is used = true; Excess energy is not used = false |



## Events
Events are also MQTT messages, but contain the grouping level `Event` after the appliance ID in the topic:

`/sae/<appliance_id>/Event/...`

The *Smart Appliance Enabler* only sends events when they occur.

By default, all events have the following `payload`:

| field  | type    | example value              | description        |
|--------|---------|----------------------------|--------------------|
| time   | String  | 2022-12-27T19:11:25.952152 | Creation timestamp |
| type   | String  | MqttMesage                 | Type of message    |


### EnableRuntimeRequest
#### Context
When `EnableRuntimeRequest` is received, the request of the first timeframe is set to `enabled`.

However, requirements are enabled by default unless they are marked "external activation".

#### Topic
`/sae/<appliance_id>/Event/EnableRuntimeRequest`

_Example_

`/sae/F-12345678-000000000001-00/Event/EnableRuntimeRequest`


### DisableRuntimeRequest
#### Context
If `DisableRuntimeRequest` is received, the request of the first timeframe will be set to `disabled`.

#### Topic
`/sae/<appliance_id>/Event/DisableRuntimeRequest`

_Example_

`/sae/F-12345678-000000000001-00/Event/DisableRuntimeRequest`


### EVChargerSocChangedEvent
#### Context
If the SOC calculated or supplied by the SOC script changes, this event is sent.

#### Topic
`/sae/<appliance_id>/Event/EVChargerSocChangedEvent`

_Example_

`/sae/F-12345678-000000000001-00/Event/EVChargerSocChangedEvent`

#### Payload
In addition to the common fields of each event, the following fields are included:

| field            | type      | example value              | description                                 |
|------------------|-----------|----------------------------|---------------------------------------------|
| chargeLoss       | Float     | 11.123                     | current charge loss in percent              |
| socValues        | SocValues | see below                  | SOC at different times                      |
| socInitialTime   | String    | 2022-12-27T19:11:25.952152 | Timestamp of the initial SOC                |
| socRetrievedTime | String    | 2022-12-27T19:11:25.952152 | Timestamp of the SOC returned by SOC script |

The structure of `SocValues` looks like this:

| field           | type     | example value | description                                           |
|-----------------|----------|---------------|-------------------------------------------------------|
| batteryCapacity | Integer  | 36000         | [EV configuration: battery capacity](EVCharger_EN.md) |
| current         | Integer  | 52            | current, calculated SOC                               |
| initial         | Integer  | 19            | SOC when the EV was connected with charger            |
| retrieved       | Integer  | 40            | SOC returned during last execution of SOC script      |


### EVChargerStateChangedEvent
#### Context
If the status of the wallbox changes, this event is sent.

#### Topic
`/sae/<appliance_id>/Event/EVChargerStateChangedEvent`

_Example_

`/sae/F-12345678-000000000001-00/Event/EVChargerStateChangedEvent`

#### Payload
In addition to the common fields of each event, the following fields are included:

| field         | type    | example value              | description                                                                                                                                                                  |
|---------------|---------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| evId          | Integer | 1                          | ID of the EV connected                                                                                                                                                       |
| newState      | String  | VEHICLE_CONNECTED          | new [Status](https://github.com/camueller/SmartApplianceEnabler/blob/EnableRuntimeRequest/src/main/java/de/avanux/smartapplianceenabler/control/ev/EVChargerState.java)      |
| previousState | String  | VEHICLE_NOT_CONNECTED      | previous [Status](https://github.com/camueller/SmartApplianceEnabler/blob/EnableRuntimeRequest/src/main/java/de/avanux/smartapplianceenabler/control/ev/EVChargerState.java) |


### SempDevice2EM
#### Context
This event is sent when the *Sunny Home Manager* fetches all data from the *Smart Appliance Enabler*.

#### Topic
`/sae/<appliance_id>/Event/SempDevice2EM`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempDevice2EM`


### SempEM2Device
#### Context
This event is sent when the *Sunny Home Manager* sends a switching command to the *Smart Appliance Enabler*.

#### Topic
`/sae/<appliance_id>/Event/SempEM2Device`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempEM2Device`


### SempGetDeviceInfo
#### Context
This event is sent when the *Sunny Home Manager* only fetches the device information from the *Smart Appliance Enabler*.

#### Topic
`/sae/<appliance_id>/Event/SempGetDeviceInfo`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempGetDeviceInfo`


### SempGetDeviceStatus
#### Context
This event is sent when the *Sunny Home Manager* only fetches the device status from the *Smart Appliance Enabler*.

#### Topic
`/sae/<appliance_id>/Event/SempGetDeviceStatus`

_Example_

`/sae/F-12345678-000000000001-00/Event/SempGetDeviceStatus`


### WrappedControlSwitchOffDetected
#### Context
This event is sent when the real switching status of a device with starting current detection or switch-on option changes.

#### Topic
`/sae/<appliance_id>/Event/WrappedControlSwitchOffDetected`

_Example_

`/sae/F-12345678-000000000001-00/Event/WrappedControlSwitchOffDetected`



## Using REST API to publish MQTT messages

TBD: https://www.home-assistant.io/integrations/mqtt/#rest-api
