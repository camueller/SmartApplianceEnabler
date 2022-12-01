# Sonoff Pow
The [Sonoff Pow from ITead](https://www.itead.cc/sonoff-pow.html) is an inexpensive switch that is connected to the WLAN and can also measure the current power consumption of the switched device.

I've already "lost" many Gosund SP111 devices with high starting currents (e.g. motors/pumps), while a Sonoff Pow is not overwhelmed even with such devices.

Use with the *Smart Appliance Enabler* is only possible if the adapter is flashed with the [Tasmota-Firmware](Tasmota_DE.md).

Observe the [device-specific instructions for flashing with Tasmota](https://tasmota.github.io/docs/devices/Sonoff-Pow/).

After flashing, the device must be set in the settings of the Tasmota web interface as:
- Sonoff Pow (6)

or

- Sonoff Pow R2 (43)
