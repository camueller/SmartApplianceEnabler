# Read state of charge (SOC) automatically
Unfortunately, most wallboxes do not yet support ISO 15118 (extensive communication via Powerline Communication (PLC) to the vehicle), which is why the wallbox does not know the SOC of the vehicle.

There are two options for automated reading of the SOC by the *Smart Appliance Enabler*:
- SOC script
- ODB2 adapter

## SOC script
For some vehicles there are so-called *SOC scripts*, which usually emulate access to the manufacturer's homepage in order to get the SOC. The scripts are usually **not permanently stable** because every change to the manufacturer's homepage requires an adjustment to the SOC scripts or the libraries used. Some manufacturers also offer access to vehicle data via **paid services**.

The use of these vehicle-specific scripts is described in the following chapters, although this is user content and I was unable to verify the procedures described. The pages are available in German only!

* [Kia](kia_DE.md)
* [Nissan](NissanLeaf_EN.md)
* [Skoda](Skoda_DE.md)
* [Volkswagen](VW_DE.md)

## ODB2 adapter
The problems with the SOC scripts can be avoided by reading the SOC in the vehicle via the **ODB2 interface** available in every vehicle.

* [WiCAN ODB2 adapter](WiCAN_EN.md)