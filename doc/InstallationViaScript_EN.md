# Installation via script
The manual installation described here requires SSH access to the Raspberry Pi and interaction with the shell. If possible, the [Standard Installation](Installation_DE.md) should be selected instead, which runs automatically and does not require any Linux knowledge.

After logging in via SSH as user "pi", a root shell must be started:
```console
pi@raspberrypi:~ $ sudo bash
```

In this root shell, the actual installation is started as follows:
```console
root@raspberrypi:/home/pi# curl -sSL https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/setup.sh | sh
```

The progress of the installation can be tracked in the console.

When the installation is complete, the **red LED will turn off for one hour**.

The *Smart Appliance Enabler* is now running and you can continue with the [Configuration](Configuration_DE.md).

The software for administration via web browser (*webmin*) should now run - see [Notes on using webmin for *Smart Appliance Enabler*](Webmin_DE.md).
