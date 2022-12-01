# Raspberry Pi
The *Smart Appliance Enabler* requires a **[Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) 2 Model B (or newer)** as hardware. This extremely inexpensive miniature computer (approx. 40 euros) is perfect for controlling and measuring, as it already contains [digital input/output interfaces](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) that for switching and for measuring the power consumption. In principle, you can also run the *Smart Appliance Enabler* on a Raspberry Pi Zero, but this platform is not officially supported and you should not expect any support if you have problems.

Various switches and/or electricity meters can be connected to the GPIO pins of the Raspberry, i.e. a single Raspberry Pi can manage a large number of devices. However, the **current** at the 5V pin must not exceed the value of 300 mA (model B) or 500mA (model A) and at the 3.3V pin the value of 50mA ([Source](http:/ /elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29))!

To access the GPIO pins, the *Smart Appliance Enabler* internally uses the [pigpioj](https://github.com/mattjlewis/pigpioj) Java library. The socket mode of the library is used so that the *Smart Appliance Enabler* can access the GPIO pins without root privileges.

[pigpioj](https://github.com/mattjlewis/pigpioj) in turn uses [pigpio](https://abyz.me.uk/rpi/pigpio/). From this library there is also a daemon `pigpiod`, which [pigpioj](https://github.com/mattjlewis/pigpioj) accesses in socket mode. The prerequisite for this is that [pigpiod has been installed](ManualInstallation_EN.md)!

For the [numbering of the connections, the pin number on the Raspberry Pi is not used, but the GPIO number of the Broadcom processor](https://raspberrypi.stackexchange.com/questions/12966/what-is-the- difference-between-board-and-bcm-for-gpio-pin-numbering).

Consequently, this number must be entered as *GPIO port* in the *Smart Appliance Enabler*.

![Raspberry Pi Pinout](../pics/raspberry-pi-15b.jpg)

## Switch off / reboot
Like any computer with a writeable storage medium, the Raspberry Pi should not be simply unplugged from the power source to turn it off or restart it. This can damage the file system or the SD card.

Shutting down followed by powering off is done with the command:
```console
sudo shutdown now
```

Reboot is done with the command:
```console
sudo shutdown -r now
```

*Webmin*: If you select `System` in the side menu and click on the sub-item `Bootup and Shutdown`, you will find buttons for `Reboot System` and `Shutdown System` at the bottom of the page.
