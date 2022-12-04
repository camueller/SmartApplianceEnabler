# Assembly

*Note: The installation of plugless 200/400V devices should always be carried out by an authorized specialist company!*

One way of mounting the Raspberry Pi is to place it directly in the wiring closet. This is particularly useful if **digital electricity meters** with S0 or Modbus output and/or **solid state relays** are used, which are also installed there.

A **power adapter** must then be installed to supply power to the Raspberry Pi.

An **IDE flat cable** from an old PC is suitable for connecting the digital electricity meters and solid state relays to the Raspberry Pi.

As a result, the assembled devices could look like this:

![Schaltschrank](../pics/Schaltschrank.png)


| Number | Description                                          |
|--------|------------------------------------------------------|
| 1      | solid state relay with heat sink for dishwasher      |
| 2      | electricity meter for dishwasher                     |
| 3      | solid state relay with heat sink for washing machine |
| 4      | meter for washing machine                          |
| 5      | residual current device for Wallbox                  |
| 6      | Raspberry Pi                                         |
| 7      | USB modbus adapter                                   |
| 8      | IDE ribbon cable                                     |
| 9      | power supply for Raspberry Pi                        |
| 10     | Modbus electricity meter for wallbox (3 phases)      |

## DIN rail holder
To mount the Raspberry Pi and solid-state relays in the control cabinet, I use **Bopla TSH 35** DIN rail mounts, which can be ordered from major electronics retailers. First, I screw a PVC plate onto this. The actual component is then attached to this with nylon screws (do not conduct electricity).

![DINSchienenhalter](../pics/DINSchienenhalter.jpg)
![DINSchienenhalterMitPVCPlatte](../pics/DINSchienenhalterMitPVCPlatte.jpg)
