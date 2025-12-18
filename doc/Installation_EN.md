# Standard installation
This page is about new installations - the procedure for updates is described [here](Update_EN.md).

The standard installation described here runs automatically and requires no Linux knowledge.

If access to the Raspberry Pi is possible via SSH, the installation can alternatively also be carried out by
- [Installation via Script](InstallationViaScript_EN.md), where only 2 shell commands have to be entered
- [manual installation](InstallationManual_EN.md) in which all commands in the documentation must be executed in the shell

## Writing the Raspberry Pi OS image to the SD card
Each Raspberry Pi has a slot for an SD card that serves as a storage medium (similar to the hard drive on normal PCs). The SD card should have a size of at least **4 GB**.

Installing the operating system for the Raspberry Pi consists of **writing an image to an SD card**. In addition to the SD card, you need either a laptop or PC with a built-in **SD card reader** or a card reader as a USB stick. The image cannot simply be copied to the SD card, but must be written to the SD card using a program such as [Raspberry PI Imager](https://www.raspberrypi.org/software/). The following description refers to this program, which must be downloaded and installed accordingly.

For the fully automatic installation of the *Smart Appliance Enabler*, a [modified Raspberry Pi OS image has to be downloaded](https://github.com/camueller/RaspiOSImageAutorunUSBShellScript/releases).

The ZIP file needs to be unpacked anywhere - it just contains a file with the modified Raspberry Pi OS image.

Now the previously installed *Raspberry PI Imager* can be started, which looks like this after the start:

![Imager nach dem Start](../pics/install/imager_initial.png)

After clicking on `Select OS`, `Other Image` must be selected in order to then select the unzipped image file:

![Image wählen](../pics/install/imager_choose_image.png)

Next, the SD card needs to be inserted into the SD card reader.

In the *Raspberry Pi Imager* click on `Select SD card`, whereupon the SD card reader is displayed with the size of the inserted SD card.

![SD-Karte wählen](../pics/install/imager_choose_drive.png)

After clicking on `Write` a deletion warning appears, which you should definitely take seriously:

![Lösch-Warnung](../pics/install/imager_erase_warning.png)

After confirming the delete warning, the image is written to the SD card

![Schreiben](../pics/install/imager_write.png)

After the end of the writing process, it is checked whether an error occurred during writing:

![Prüfen](../pics/install/imager_verify.png)

If no errors were found, the SD card can be removed from the card reader:

![Schreiben erfolgreich](../pics/install/imager_success.png)

## Preparation for installation

To prepare for the installation, the configuration file for the installer [install.config](https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/install.config) must be downloaded and saved locally for subsequent changes.

Notes and examples for the few configuration parameters can be found in the file itself.

[webmin](https://www.webmin.com) is installed by default, which allows the Raspberry Pi to be administered using a web browser. This is very helpful if you are not familiar with Linux.

A USB stick with the following files (and only these!) must be prepared for the installation:
- `install.config` with the above adjustments
- [install.sh](https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/install.sh)
- [install2.sh](https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/install/install2.sh)

**The file names must not be changed (upper and lower case, file extensions). This is particularly important under Windows if it is set in such a way that they are not displayed!**

## Installation
The Raspberry Pi must be **unplugged** while the **SD card** containing the image is inserted into the SD card slot.

The Raspberry Pi must be connected with a **network cable** for installation! The configuration of the wifi is not the subject of this installation and must be done manually at a later date if desired.

Now the Raspberry Pi must be **connected** to the power supply. The **boot process** should be completed after 2 minutes at the latest.

Next, the prepared **USB stick** must be plugged into the Raspberry Pi (regardless of which USB socket). As soon as the Raspberry Pi recognizes the USB stick (thanks to the modified image), it will carry out the **first phase of the installation**. After **10 seconds** the **USB stick must be removed** to trigger a restart so that the **second phase of the installation** can begin. The Raspberry Pi software is updated and the *Smart Appliance Enabler* and other required software are installed. Depending on the Raspberry Pi model, the speed of the SD card and the internet connection, this phase can take some time (on my Raspberry Pi 4 Model B it takes 22 minutes). When the installation is complete, this is signaled by the **flashing of an LED every 1 second**.

The *Smart Appliance Enabler* is now running and you can continue with the [Configuration](Configuration_EN.md).

The software for administration via web browser (*webmin*) should now run - see [Notes on using webmin for *Smart Appliance Enabler*](Webmin_EN.md).

### Errors during installation
In my experience so far, 90% of unsuccessful installations were due to the **instructions not being followed exactly**. In this respect, it can definitely make sense to repeat the installation with more attention.

The installation is logged in the file `/tmp/install.log`. In the event of a faulty installation, it would be very helpful for error analysis if this file was backed up by the Raspberry Pi (with `scp` or `WinSCP`). Before the backup, the Raspberry Pi must not be disconnected from the power supply or rebooted, otherwise the file will no longer be available! The prerequisite for downloading the file is, of course, that the Raspberry Pi can be reached via LAN or WLAN.

The file `/tmp/install.log` [of a successful installation looks like this](../install/install.log). Of particular interest are the lines beginning with `***********`, which mark the beginning of an installation step. And here is the line
```console
********** Starting SAE ...
```
in particular interesting that says *Smart Appliance Enabler* should now be running.
