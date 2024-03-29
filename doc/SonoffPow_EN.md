# Sonoff Pow
The [Sonoff Pow manufactured by ITead](https://www.itead.cc/sonoff-pow.html) is an inexpensive switch that is connected to the WIFI and can also measure the current power consumption of the switched device.

I've already "lost" many Gosund SP111 devices because of high starting currents (e.g. motors/pumps), while a Sonoff Pow has no problem with this.

Use with the *Smart Appliance Enabler* is only possible if the adapter is flashed with the [Tasmota-Firmware](Tasmota_EN.md).

See the [device-specific instructions for flashing with Tasmota](https://tasmota.github.io/docs/devices/Sonoff-Pow/).

After flashing, the device must be set in the settings of the Tasmota web interface as:
- Sonoff Pow (6)

or

- Sonoff Pow R2 (43)
