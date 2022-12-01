# Configuration of wallboxes feat. Modbus protocol without template

The *Smart Appliance Enabler* comes with templates for various wallboxes that contain the associated configuration. If no configuration is available for the wallbox used, but it can be controlled via Modbus/TCP, you can also create a configuration yourself.

## General Modbus configuration

Before the Wallbox-specific Modbus configuration can be created, these [General requirements for using Modbus in the Smart Appliance Enabler](Modbus_DE.md) must be met.

## Wallbox specific Modbus configuration

The Wallbox-specific Modbus configuration of the *Smart Appliance Enabler* essentially consists of 3 parts:

### Wallbox status

Reading out the wallbox status is a read access.

To determine the wallbox status, 4 parameters must be determined:
- Vehicle not connected
- Vehicle connected
- Charging
- Charger reports error

Each parameter must be mapped to a register along with a [Regular Expression/Regex](ValueExtraction_EN.md), i.e. if the value in that register matches the regular expression, the wallbox state will match that parameter.

Multiple parameters can be mapped to the same register, each parameter should have a different regular expression.

If you assign a parameter to several registers, this corresponds to an AND operation, i.e. the wallbox status only corresponds to this parameter if all assigned registers have a value that matches the respective regular expression.

In the Modbus documentation of the wallbox, look for one or more registers in the input registers (read) that can map the 4 parameters of the status mentioned above.

_Example:_

In the case of wall boxes with a Phoenix Contact EM-CP-PP-ETH controller, the status is contained in register 100 in the form of a letter:

| Parameter              | Extraction regex | Explanation                                                                   |
|------------------------|------------------|-------------------------------------------------------------------------------|
| Vehicle not connected  | (A)              | If the register shows A, no vehicle is connected                              |
| Vehicle connected      | (B)              | If B is in the register, a vehicle is connected but is not currently charging |
| Charging               | (C&#124;D)       | If the register says C or D, the vehicle is charging                          |
| Charger reports error  | (E&#124;F)       | If the register shows E or F, the charger is in the error state               |

### Start/stop charging

Starting and ending the charging process are two actions that represent a write access.

In the Modbus documentation of the wallbox, look for a register in the holding registers (Write) that allows the charging status to be set. After the charging process has ended, it should be possible to restart the charging process without disconnecting the vehicle from the wallbox.

_Example:_

In the case of wall boxes with a Phoenix Contact EM-CP-PP-ETH controller, the charging status is set via register 400:

| Parameter      | Value | Explanation                                                                      |
|----------------|-------|----------------------------------------------------------------------------------|
| Start charging | 1     | To start the loading process, the value 1 must be written into this register.    |
| Stop charging  | 0     | If the value 0 is written into the register, the loading process is terminated.  |

### Set charging current

Setting the charging current also represents a write access.

In the Modbus documentation of the wall box, look for a register in the holding registers (Write) that allows the charging current to be set.

_Example:_

In the case of wall boxes with a Phoenix Contact EM-CP-PP-ETH controller, the charging current is set via register 300:

| Parameter             | Value | Explanation                                                                            |
|-----------------------| ---- |-----------------------------------------------------------------------------------------|
| Set charging current  | 0    | The value is a required field. 0 can be entered because the concrete value is variable. |
