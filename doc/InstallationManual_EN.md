# Manual installation
The manual installation described here requires SSH access to the Raspberry Pi and interaction with the shell. If possible, the [Standard Installation](Installation_EN.md) should be selected instead, which runs automatically and does not require any Linux knowledge.

The following chapters should be implemented in the order given.

## Operating system
### General information
The operating system for the Raspberry is Linux. The interaction with Linux takes place via the so-called shell (comparable to the DOS box or cmd.exe under Windows). A German-language introduction to this can be found [here](https://wiki.ubuntuusers.de/Shell/Einf%C3%BChrung/).
Shell commands and the corresponding outputs can be found everywhere in the documentation for the *Smart Appliance Enabler*. For better readability, Github provides a color scheme that uses different colors for each element:
* violet: input request or prompt (ends with the $ sign)
* black: command to be entered (dollar signs and spaces at the beginning are not included!)
* blue: Output or response to the entered command

The elements mentioned can all be found in the following example:
```console
pi@raspi:~ $ uname -a
Linux raspi3 4.19.75-v7+ #1270 SMP Tue Sep 24 18:45:11 BST 2019 armv7l GNU/Linux
```

### Raspbian
From the [Raspberry Pi OS](https://www.raspberrypi.org/software) images, the **Lite version** is sufficient, so you can use a *4GB SD card*.

_**For Smart Appliancer Enabler up to and including version 1.4:**_ Raspbian Stretch must be used (Raspbian Buster or newer is not suitable!!!). Download: https://downloads.raspberrypi.org/raspbian_lite/images/raspbian_lite-2019-04-09/

_**For Smart Appliancer Enabler version 1.5 or higher:**_ At least Raspbian Buster is required.

The [Raspberry Pi Imager](https://www.raspberrypi.org/software) is suitable for writing the image to an SD card. Alternatively, you can use the following command to write the image to an SD card under Linux:
```console
axel@p51:/tmp$ sudo dd bs=4M if=2019-09-26-raspbian-buster-lite.img of=/dev/mmcblk0 status=progress oflag=sync
[sudo] password for axel: 
2248146944 bytes (2.2 GB, 2.1 GiB) copied, 280 s, 8.0 MB/s 
536+0 records in
536+0 records out
2248146944 bytes (2.2 GB, 2.1 GiB) copied, 280.242 s, 8.0 MB/s
```
The [enlargement of the root file system](#root-filesystem-enlarge) can still be done later.

If the Raspberry does not boot from the SD card, it may well be casued by the SD card itself. In this case, just use a different type of SD card (I've had good experiences with SanDisk). A successful start is easily recognized by the fact that the green LED flickers/lights up (= access to the SD card).

### SSH client
The interaction with the Raspberry Pi takes place via SSH (Secure Shell), which is a window comparable to the Windows command prompt. While an SSH client is part of the standard equipment on Linux, it has to be installed separately on Windows. Instructions for this can be found in the article [SSH using Windows](https://www.raspberrypi.org/documentation/remote-access/ssh/windows.md).

### SSH access
On newer images, SSH is disabled by default for security reasons. There are various ways of activating (see https://linuxundich.de/raspberry-pi/ssh-auf-dem-raspberry-pi-aktivieren-jetzt-unter-raspian-needed or
https://kofler.info/geaenderte-ssh-server-konferenz-von-raspbian), although I prefer the method described below (this command only works under Linux):

1. Mount SD card boot partition
```console
axel@tpw520:~$ sudo mount /dev/mmcblk0p1 /mnt
```
2. Create an empty file named `ssh`:
```console
axel@tpw520:~$ sudo touch /mnt/ssh
```
3. Unmount the mounted SD card partition
```console
axel@tpw520:~$ sudo umount /mnt
```
After the Raspberry Pi has been booted with the SD card modified in this way, access with SSH should be possible. Don't forget to connect the Raspberry Pi to the router with an Ethernet cable!
Of course, for access you need the IP address or the host name that was assigned to the Raspberry Pi by the router (in my example this is `raspi`)
```console
axel@p51:~$ ssh pi@raspi
pi@raspi's password: 
Linux raspberrypi 4.19.75-v7+ #1270 SMP Tue Sep 24 18:45:11 BST 2019 armv7l

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.

SSH is enabled and the default password for the 'pi' user has not been changed.
This is a security risk - please login as the 'pi' user and type 'passwd' to set a new password.

pi@raspberrypi:~ $
```
### <a name="root-filesystem-enlarge"></a> Increase root file system
The Raspbian images are usually created for SD cards with a size of 2 GB. If the SD card used is larger, the additional storage space remains unused. However, Raspbian includes the `raspi-config` utility that allows you to easily enlarge the root filesystem to use the entire SD card (a 16 GB SD card was used here):

```console
pi@raspberrypi:~ $ sudo raspi-config --expand-rootfs

Welcome to fdisk (util-linux 2.33.1).
Changes will remain in memory only, until you decide to write them.
Be careful before using the write command.


Command (m for help): Disk /dev/mmcblk0: 14.7 GiB, 15719727104 bytes, 30702592 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: dos
Disk identifier: 0x6c586e13

Device         Boot  Start      End  Sectors  Size Id Type
/dev/mmcblk0p1        8192   532479   524288  256M  c W95 FAT32 (LBA)
/dev/mmcblk0p2      532480 30702591 30170112 14.4G 83 Linux

Command (m for help): Partition number (1,2, default 2): 
Partition 2 has been deleted.

Command (m for help): Partition type
   p   primary (1 primary, 0 extended, 3 free)
   e   extended (container for logical partitions)
Select (default p): Partition number (2-4, default 2): First sector (2048-30702591, default 2048): Last sector, +/-sectors or +/-size{K,M,G,T,P} (532480-30702591, default 30702591): 
Created a new partition 2 of type 'Linux' and of size 14.4 GiB.
Partition #2 contains a ext4 signature.

Command (m for help): 
Disk /dev/mmcblk0: 14.7 GiB, 15719727104 bytes, 30702592 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: dos
Disk identifier: 0x6c586e13

Device         Boot  Start      End  Sectors  Size Id Type
/dev/mmcblk0p1        8192   532479   524288  256M  c W95 FAT32 (LBA)
/dev/mmcblk0p2      532480 30702591 30170112 14.4G 83 Linux

Command (m for help): The partition table has been altered.
Syncing disks.

Please reboot
```

### Update Packages
After installing Raspbian, it's a good idea to update the package information:
```console
pi@raspi ~ $ sudo apt update
Hit:1 http://raspbian.raspberrypi.org/raspbian buster InRelease
Hit:2 http://archive.raspberrypi.org/debian buster InRelease
Reading package lists... Done
Building dependency tree       
Reading state information... Done
64 packages can be upgraded. Run 'apt list --upgradable' to see them.
```

After that you should update the installed packages:
```console
pi@raspi:~ $ sudo apt upgrade
Reading package lists... Done
Building dependency tree       
Reading state information... Done
Calculating upgrade... Done
The following NEW packages will be installed:
  busybox initramfs-tools initramfs-tools-core klibc-utils libklibc linux-base pigz
The following packages will be upgraded:
  base-files cron dhcpcd5 distro-info-data e2fsprogs file firmware-atheros firmware-brcm80211 firmware-libertas firmware-misc-nonfree firmware-realtek freetype2-doc libcom-err2 libext2fs2 libfreetype6
  libfreetype6-dev libfribidi0 libglib2.0-0 libglib2.0-data libmagic-mgc libmagic1 libncurses6 libncursesw5 libncursesw6 libpam-systemd libpython2.7-minimal libpython2.7-stdlib libraspberrypi-bin
  libraspberrypi-dev libraspberrypi-doc libraspberrypi0 libsasl2-2 libsasl2-modules-db libss2 libssl1.1 libsystemd0 libtinfo5 libtinfo6 libudev1 libxml2 libxmuu1 ncurses-base ncurses-bin ncurses-term
  openssh-client openssh-server openssh-sftp-server openssl pi-bluetooth python2.7 python2.7-minimal raspberrypi-bootloader raspberrypi-kernel raspberrypi-sys-mods raspi-config rpcbind rpi-eeprom
  rpi-eeprom-images ssh sudo systemd systemd-sysv udev wpasupplicant
64 upgraded, 7 newly installed, 0 to remove and 0 not upgraded.
Need to get 150 MB of archives.
After this operation, 4214 kB of additional disk space will be used.
Do you want to continue? [Y/n] 
Get:1 http://archive.raspberrypi.org/debian buster/main armhf dhcpcd5 armhf 1:8.1.2-1+rpt1 [146 kB]
Get:3 http://archive.raspberrypi.org/debian buster/main armhf firmware-atheros all 1:20190114-1+rpt4 [3887 kB]
...
```

### Set up WIFI (not available with Raspberry Pi 2)
If the Raspberry Pi is to be connected via WIFI instead of Ethernet, the SSID and password must be entered in the file `/etc/wpa_supplicant/wpa_supplicant.conf`. A detailed description can be found in [Setting WiFi up via the command line](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md).

### Change hostname
Regardless of the hostname via which the Raspberry Pi can be reached in the local network, its default hostname is `raspberry` (also visible in the prompt: `pi@raspberrypi:~ $`). Especially if you have several Raspberry Pis in the network, you want to see at the prompt which Raspberry you are currently entering the commands on.

The `raspi-config` tool can be used to change the hostname by selecting the _System Options_ menu item and then the _Hostname_ menu item:
```console
pi@raspberrypi:~ $ sudo raspi-config
```

If the hostname is changed like this, the Raspberry Pi will also propagate its name, which can cause problems if a Raspberry Pi with the same name is already running. In this case, the hostname should only be changed if the new Raspberry Pi is to replace the previous one.

### Set time zone
The time zone of the Raspberry should be set to the local time (not UTC!) so that time information for switching the devices is correctly interpreted. This can be achieved with the following commands:
```console
pi@raspberrypi ~ $ sudo /bin/bash -c "echo 'Europe/Berlin' > /etc/timezone"
pi@raspberrypi ~ $ sudo cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
```

## Install Java
The following command is required to install Java:
```console
pi@raspberrypi:~ $ sudo apt install openjdk-11-jre-headless
```

The successful installation can be checked with the following command:
```console
pi@raspberrypi:~ $ java -version
openjdk version "11.0.5" 2019-10-15
OpenJDK Runtime Environment (build 11.0.5+10-post-Raspbian-1deb10u1)
OpenJDK Server VM (build 11.0.5+10-post-Raspbian-1deb10u1, mixed mode)
```
#### Notes on Raspberry Pi Zero
**The following information comes from users running the *Smart Appliance Enabler* on a Raspberry Pi Zero. This platform is not officially supported, so please do not expect support if you have any problems.**

Unfortunately, OpenJDK 11 is not compatible with ARMv6 and ARMv7. There is the possibility to install an alternative via "Zulu Build for OpenJDK" from Azul.
To do this, first create a directory and then determine the architecture (soft float (armsf) or hard float (armhf)) of the OS:
```console
pi@raspberrypi:~ $ sudo mkdir /opt/jdk
pi@raspberrypi:~ $ cd /opt/jdk
pi@raspberrypi:~ $ dpkg --print-architecture
```
On the Azul [download page](https://www.azul.com/downloads/zulu-community/?version=java-11-lts&package=jdk) copy the download link of the current version for the appropriate architecture (armsf or armhf). Then download, unpack, clean up and link:
```console
pi@raspberrypi:~ $ sudo wget https://cdn.azul.com/zulu-embedded/bin/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz
pi@raspberrypi:~ $ sudo tar -xzvf zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz
pi@raspberrypi:~ $ sudo rm *.tar.gz
pi@raspberrypi:~ $ sudo update-alternatives --install /usr/bin/java java /opt/jdk/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf/bin/java 1
pi@raspberrypi:~ $ sudo update-alternatives --install /usr/bin/javac javac /opt/jdk/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf/bin/javac 1
```
Since the Raspberry Pi 1 models or the Pi-Zero provide less computing power, the start of the SAE generally takes a little longer, so that the timeout times have to be adjusted to prevent the program from aborting. Use an editor to replace `sleep 1` with `sleep 3` in `/opt/sae/smartapplianceenabler` and change `TimeoutStartSec=90s` to `TimeoutStartSec=180s` in /lib/systemd/system/smartapplianceenabler.service.

## install pigpiod
If you want the *Smart Appliance Enabler* to access the Raspberry Pi's GPIO ports, the [pigpiod](http://abyz.me.uk/rpi/pigpio/) library must be installed. This can be achieved with the following commands:
```console
pi@raspberrypi:~ $ sudo apt install pigpiod
```
The `pigpiod` is installed by default to only accept local access. Although the *Smart Appliance Enabler* actually accesses via `localhost`, the `pigpiod` cannot be accessed when started with this restriction. To disable this restriction, in the file `/lib/systemd/system/pigpiod.service` in the line with `ExecStart` the parameter `-l` has to be removed so that it looks like this:
```console
ExecStart=/usr/bin/pigpiod
```

To start the deamon automatically at system startup (via systemd), the following command must be executed:
```console
pi@raspberrypi ~ $ sudo systemctl enable pigpiod
Created symlink /etc/systemd/system/multi-user.target.wants/pigpiod.service → /lib/systemd/system/pigpiod.service.
```

## MQTT broker
The *Smart Appliance Enabler* requires an MQTT broker, whereby an existing MQTT broker can be used.
If no MQTT broker is available yet, we recommend using [Eclipse Mosquitto](https://mosquitto.org/).

### Installation and configuration
[Eclipse Mosquitto](https://mosquitto.org/) can be installed directly from the Raspbian repository:
```console
pi@raspberrypi:~ $ sudo apt install mosquitto
```
The storage of MQTT messages on the SD card should be deactivated in the configuration file `/etc/mosquitto/mosquitto.conf`. To do this, the corresponding line must look like this:
```
persistence false
```
To allow unauthenticated access to the MQTT broker, the file `/etc/mosquitto/conf.d/smartapplianceenabler.conf` must be created with the following content:
```
listener 1883
allow_anonymous true
```
The following command is suitable for starting:
```console
pi@raspberrypi:~ $ sudo systemctl start mosquitto
```
To start the MQTT broker automatically at system startup (via systemd), the following command must be executed:
```console
pi@raspberrypi ~ $ sudo systemctl enable mosquitto
Synchronizing state of mosquitto.service with SysV service script with /lib/systemd/systemd-sysv-install.
Executing: /lib/systemd/systemd-sysv-install enable mosquitto
```

### Installation and configuration using Docker
A Docker image exists for [Eclipse Mosquitto](https://mosquitto.org/):
```console
docker pull eclipse-mosquitto
```
The following command is suitable for starting without authentication:
```console
$ docker run -it --rm -p 1883:1883 --name mosquitto eclipse-mosquitto mosquitto -c /mosquitto-no-auth.conf
```

## Node-RED
The optional [Installation of Node-RED](NodeRED_EN.md) enables the use of a detailed dashboard based on the MQTT messages.

## Smart Appliance Enabler
### Initial installation
#### Init script and configuration files
First, users and groups are created that are used when starting the *Smart Appliance Enabler* and that own certain files/directories.
Then the init script and associated configuration files are downloaded and the permissions for these files are set immediately.
```console
pi@raspberrypi ~ $ sudo useradd -d /opt/sae -m -s /bin/bash sae
pi@raspberrypi ~ $ sudo usermod -a -G sudo sae
pi@raspberrypi ~ $ sudo passwd sae

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/smartapplianceenabler -P /opt/sae
pi@raspberrypi ~ $ sudo chmod 755 /opt/sae/smartapplianceenabler

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/smartapplianceenabler.service -P /lib/systemd/system
pi@raspberrypi ~ $ sudo chown root.root /lib/systemd/system/smartapplianceenabler.service
pi@raspberrypi ~ $ sudo chmod 755 /lib/systemd/system/smartapplianceenabler.service

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/default/smartapplianceenabler -P /etc/default
pi@raspberrypi ~ $ sudo chown root.root /etc/default/smartapplianceenabler
pi@raspberrypi ~ $ sudo chmod 644 /etc/default/smartapplianceenabler

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/logback-spring.xml -P /opt/sae
pi@raspberrypi ~ $ sudo chmod 644 /opt/sae/logback-spring.xml

pi@raspberrypi ~ $ sudo chown -R sae:sae /opt/sae
```

The *Smart Appliance Enabler* is normally managed as a [Systemd](https://de.wikipedia.org/wiki/Systemd) service. The file `/lib/systemd/system/smartapplianceenabler.service` is used for this.

The file `/opt/sae/smartapplianceenabler` is the actual start script for the *Smart Appliance Enabler*. While it can be called directly, it should only be used by _Systemd_.

See also:
- [Konfigurationseinstellungen](ConfigurationFiles_EN.md#user-content-etc-default-smartapplianceenabler)
- [Konfiguration des Loggings](ConfigurationFiles_EN.md#user-content-log-konfiguration)

The following command must be executed so that the *Smart Appliance Enabler* is also started when the system starts (via systemd):
```console
pi@raspberrypi ~ $ sudo systemctl enable smartapplianceenabler
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler.service → /lib/systemd/system/smartapplianceenabler.service.
```
After making these changes, the systemd has to be triggered to reread the service configurations:
```console
pi@raspberrypi ~ $ sudo systemctl daemon-reload
```
Successful registration of the *smartapplianceenabler* service can be verified as follows:
```console
pi@raspberrypi ~ $ systemctl list-units|grep smartapplianceenabler
smartapplianceenabler.service                                                                loaded failed failed    Smart Appliance Enabler
```
If the second line is not displayed, the Raspberry Pi should be restarted.

#### Application download
The actual program code is contained in the `SmartApplianceEnabler-X.Y.Z.war` file, which must also be downloaded. *X.Y.Z* stands for the current version number (e.g. 2.1.0), which is displayed [behind the download button](https://github.com/camueller/SmartApplianceEnabler#smart-appliance-enabler). According to these notes, the version in the following command must be adjusted in 2 places (*2.1.0* and *SmartApplianceEnabler-2.1.0.war*):
```console
pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/releases/download/2.1.0/SmartApplianceEnabler-2.1.0.war -P /opt/sae
pi@raspberrypi ~ $ sudo chown -R sae:sae /opt/sae
```

After the download, it should be checked that the downloaded program file is at least 20 MB in size - otherwise an incorrect URL may have been used:
```console
pi@raspberrypi ~ $ ls -al /opt/sae/*.war
-rw-r--r-- 1 sae sae 23040544 Oct 20 08:49 /opt/sae/SmartApplianceEnabler-1.4.15.war
```

#### Start
Now you should be able to start the *Smart Appliance Enabler*:
```console
pi@raspberrypi:~ $ sudo systemctl start smartapplianceenabler
```
Depending on the Raspberry Pi model, the start takes up to 60 seconds.

If the *Smart Appliance Enabler* is running, the [configuration](Configuration_EN.md) must be made next.

#### Stop
The *Smart Appliance Enabler* is stopped as follows:
```console
pi@raspberrypi:~ $ sudo systemctl stop smartapplianceenabler.service
```

#### Status
The following command can be used to check whether the *Smart Appliance Enabler* is running:
```console
pi@raspberrypi ~ $ sudo systemctl status smartapplianceenabler.service
● smartapplianceenabler.service - Smart Appliance Enabler
   Loaded: loaded (/lib/systemd/system/smartapplianceenabler.service; enabled; vendor preset: enabled)
   Active: active (running) since Thu 2020-12-31 15:35:13 CET; 20h ago
  Process: 24019 ExecStart=/opt/sae/smartapplianceenabler start (code=exited, status=0/SUCCESS)
 Main PID: 24026 (sudo)
    Tasks: 48 (limit: 2063)
   CGroup: /system.slice/smartapplianceenabler.service
           ├─24026 sudo -u sae /usr/bin/java -Djava.awt.headless=true -Xmx256m -Duser.language=de -Duser.country=DE -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid -Dsae.home=
           └─24028 /usr/bin/java -Djava.awt.headless=true -Xmx256m -Duser.language=de -Duser.country=DE -Dlogging.config=/opt/sae/logback-spring.xml -Dsae.pidfile=/var/run/sae/smartapplianceenabler.pid -Dsae.home=/opt/sae -ja
```

### Update
To update an existing version, you must first delete the old program:
```console
pi@raspberrypi ~ $ rm /opt/sae/*.war
```
The desired version of the program can now be downloaded, as described in the initial installation chapter [Program download](#program download).

In most cases the new version of the *Smart Appliance Enabler* can migrate the configuration files from the old version to the new version. If error messages are found in the log when starting the new version of the *Smart Appliance Enabler* that clearly indicate problems with the configuration file, the files should be renamed so that they are not used but can be reactivated if necessary:
```console
pi@raspberrypi ~ $ mv /opt/sae/Appliances.xml /opt/sae/Appliances.xml.old
pi@raspberrypi ~ $ mv /opt/sae/Device2EM.xml /opt/sae/Device2EM.xml.old
```
The configuration must then be created again, as described in the [Configuration](Configuration_EN.md) chapter.

### <a name="notifications"></a> Notifications
For the optional sending of notifications via instant messengers such as [Telegram](http://www.telegram.org), the corresponding shell script must be downloaded and made executable:
```console
pi@raspberrypi ~ $ wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/notifyWithTelegram.sh -P /opt/sae
pi@raspberrypi ~ $ chmod +x /opt/sae/notifyWithTelegram.sh
```
