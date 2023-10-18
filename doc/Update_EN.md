# Update
## General information
The web application, which is also updated as part of the update, is part of the *Smart Appliance Enabler*. Depending on the settings in the web browser used, the web interface may not be displayed correctly after the update and behave "strangely".

![Web App Corrupt](../pics/fe/WebAppCorrupt.png)

In this case, the web browser's cache for the *Smart Appliance Enabler* URL must be cleared, or possibly completely. After that, the web application should work as usual.

## Update with the same main version (1.x -> 1.x, 2.x -> 2.x)
Updating the installed version of the *Smart Appliance Enabler* consists of replacing the `SmartApplianceEnabler-*.war` file in the `/opt/sae` directory. **It should be noted that there is always only one file with the extension `.war` in the directory!** In order to be able to switch back to the old version, you can rename it to, for example,`SmartApplianceEnabler-1.6.19.war.old` instead of deleting them.

Before an update, copies of the [configuration files](ConfigurationFiles_EN.md) should be created because they are automatically updated when the new version is started and it is unlikely that it will be possible to switch back to the previously used version.

### Download the new version
The versions of the *Smart Appliance Enabler* available for download can be found on the [project page in the Releases section](https://github.com/camueller/SmartApplianceEnabler/releases).

### Performing the update
Copy the new `SmartApplianceEnabler-*.war` file to the `/opt/sae` directory on the Raspberry Pi using `scp`. The login should be done with the user `sae`, so that the file has `sae` as owner and group.

*Webmin*: With the [webmin file manager](Webmin_EN.md) you can also copy the file to the Raspberry Pi and also rename or delete an existing file. Copies of files are created using the "Copy" and "Paste" menu items.

### Launching the updated version
After the `SmartApplianceEnabler-*.war` file to be used has been placed, the *Smart Appliance Enabler* process needs to be restarted. This happens with the following command:
```console
sudo systemctl restart smartapplianceenabler.service
```

*Webmin*: The *Smart Appliance Enabler* can also be restarted with the [webmin service management](Webmin_EN.md).

## Update from version 1.6 to version 2.x
### Automated update
Requirements for the automated update are:
- Raspberry Pi
- SSH shell or `webmin`
- non-virtualized standard installation of the *Smart Appliance Enabler*

**Caution: Under no circumstances must the update process be interrupted once it has started!**

To perform the update, you must be logged in as user "pi" either via SSH or `webmin`. When using `webmin`, the [menu item `Command Shell`](Webmin_EN.md) must be selected to enter the following commands.

First the configuration file has to be downloaded:
```console
sudo curl -sSL https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/install.config --output /usr/local/etc/install.config
```

After that, the update is started as follows:
```console
curl -sSL https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/upgrade.sh | sudo sh
```

The actual update script [upgrade2.sh](https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/upgrade2.sh) is downloaded and executed at system startup (in `/etc/rc.local `) registered. A restart is then triggered, meaning that after the Raspberry Pi has finished booting, you have to connect again via SSH or load `webmin` again. You should be patient with `webmin` because network traffic and/or system load can temporarily restrict usability.

If you are logged into the Raspberry Pi via SSH, you can follow the progress of the update in the console with `tail -f /tmp/install.log`. You can also follow the progress of the update in `webmin` - see [Show log files](Webmin_EN.md), whereby `/tmp/install.log` must be entered as the log file.

At the beginning of the update, the **Raspberry Pi OS is updated to "Bullseye"** if an older version is still installed. Even with a fast internet connection, this OS update can take an hour or more even on a Raspberry Pi 4! After the OS update, the Raspberry Pi will restart!

If the Raspberry Pi OS has the "Bullseye" version, the [*Smart Appliance Enabler* configuration files](ConfigurationFiles_EN.md) and also the `SmartApplianceEnabler-*.war` file (with file extension `.bak`) are backed up first. The actual update of the *Smart Appliance Enabler* is then carried out, including the installation of the required packages. The MQTT broker `mosquitto` is also installed and configured automatically.

When the update is finished, the **red LED will turn off for one hour**.

### Manual update
The manual update should only be performed if:
- no Raspberry Pi is used
- and a non-virtualized *Smart Appliance Enabler* installation is used

#### pigpiod
If GPIO is to be used (only on Raspberry Pi) [pigpiod must be installed](InstallationManual_EN.md#pigpiod-installieren).

The following lines must be added to the `/etc/default/smartapplianceenabler` file:
```
# Configure pigpioj to use pigpiod daemon in order to avoid forcing the Smart Appliance Enabler to run as root
JAVA_OPTS="${JAVA_OPTS} -DPIGPIOD_HOST=localhost"
```

#### MQTT broker
**SAE 2.0 cannot run without an MQTT broker**. In theory, any existing MQTT broker should work, but in practice this doesn't seem to be the case. If in doubt, the MQTT broker [Mosquitto should be installed](InstallationManual_EN.md#mqtt-broker).

If the MQTT broker cannot be reached via `localhost:1883` or username/password are required, these parameters must be configured in the *Smart Appliance Enabler* settings. As long as these are not configured, the web interface of the SAE can be used, but **no data is transmitted to the SHM or control requests are executed by it**.

#### Configuration files
It is strongly recommended to back up the `Appliances.xml` configuration file before the update, in case you want to switch back to version 1.6 after all. As soon as a change is saved in *Smart Appliance Enabler* 2.0, the `Appliances.xml` file is overwritten, making it no longer readable for *Smart Appliance Enabler* 1.6.

#### Update installed version of *Smart Appliance Enabler*
The *Smart Appliance Enabler* is updated by [exchanging the war file as described above on this page](#performing-the-update).

### Docker
The file https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/run/etc/docker/compose/docker-compose.yml has been adjusted and starts a container with `pigpiod` and before the *Smart Appliance Enabler* `mosquito`.

Otherwise, you only have to use the Docker image in the desired version.
