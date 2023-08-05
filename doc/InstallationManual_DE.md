# Manuelle Installation
Die hier beschriebene manuelle Installation benötigt einen SSH-Zugriff auf den Raspberry Pi und die Interaktion mit der Shell. Falls möglich, sollte stattdessen die [Standard-Installation](Installation_DE.md) gewählt werden, die automatisch abläuft und keine Linux-Kenntnisse erfordert.

Die nachfolgenden Kapitel sollten in der angegebenen Reihenfolge umgesetzt werden.

## Betriebssystem
### Allgemeine Hinweise
Das Betriebssystem für den Raspberry ist Linux. Die Interaktion mit Linux erfolgt dabei über die sogenannte Shell (vergleichbar der DOS-Box bzw. cmd.exe unter Windows). Eine deutschsprachige Einführung dazu findet sich [hier](https://wiki.ubuntuusers.de/Shell/Einf%C3%BChrung/).
In der Dokumentation zum *Smart Appliance Enabler* finden sich überall Shell-Befehle und die dazu korrespondieren Ausgaben. Zur besseren Lesbarkeit bietet Github ein Farbschema an, das verschiedene Farben für die einzelnen Elemente verwendet:
* violett: Eingabeaufforderung bzw. Prompt (endet mit dem $-Zeichen)
* schwarz: einzugebender Befehl (Dollar-Zeichen und Leerzeichen zu Beginn gehören nicht dazu!)
* blau: Ausgabe bzw. Antwort auf den eingegebenen Befehl

Die genannten Elemente finden sich alle in dem nachfolgenden Beispiel:
```bash
pi@raspi:~ $ uname -a
Linux raspi3 4.19.75-v7+ #1270 SMP Tue Sep 24 18:45:11 BST 2019 armv7l GNU/Linux
```

Befehle mit mehreren Optionen und Parametern werden zum besseren Verständnis mehrzeilig dargestellt, wobei der Backslash `\ ` das letzte Zeichen jeder Zeile ist und damit die jeweils nächste Zeile "verbindet". Ein solcher Befehl kann so wie er ist kopiert und ausgeführt werden. Ein Beispiel:

```bash
$ docker run \
    -it \
    --rm \
    -p 1880:1880 \
    -v node_red_data:/data \
    --name nodered \
    nodered/node-red
```

### Raspbian
Von den [Raspberry Pi OS](https://www.raspberrypi.org/software) Images ist die **Lite-Version** ausreichend, sodass man eine *4GB-SD-Karte* verwenden kann.

_**Für Smart Appliancer Enabler bis einschliesslich Version 1.4 gilt:**_ Es muss Rasbian Stretch verwendet werden (Raspbian Buster oder neuer ist nicht geeignet!!!). Download: https://downloads.raspberrypi.org/raspbian_lite/images/raspbian_lite-2019-04-09/

_**Für Smart Appliancer Enabler ab Version 1.5 gilt:**_ Es ist mindestens Raspbian Buster erforderlich.

Zum Schreiben des Images auf eine SD-Karte eignet sich der [Raspberry Pi Imager](https://www.raspberrypi.org/software). Alternativ kann man mit dem nachfolgenden Befehl unter Linux das Image auf eine SD-Karte schreiben:
```bash
$ sudo dd bs=4M if=2019-09-26-raspbian-buster-lite.img of=/dev/mmcblk0 status=progress oflag=sync
[sudo] password for axel: 
2248146944 bytes (2.2 GB, 2.1 GiB) copied, 280 s, 8.0 MB/s 
536+0 records in
536+0 records out
2248146944 bytes (2.2 GB, 2.1 GiB) copied, 280.242 s, 8.0 MB/s
```
Das [Vergrößerung des Root-Filesystems](#root-filesystem-vergroesern) kann später noch erfolgen.

Sollte der Raspberry mit der SD-Karte nicht starten, kann es durchaus an der SD-Karte selbst liegen. In diesem Fall einfach einen anderen SD-Karten-Typ verwenden (gute Erfahrungen habe ich mit SanDisk gemacht). Einen erfolgreichen Start erkennt man leicht daran, dass die grüne LED flackert/leuchtet (= Zugriff auf die SD-Karte).

### SSH-Client
Die Interaktion mit dem Raspberry Pi erfolt über SSH (Secure Shell), das ist ein Fenster vergleichbar der Windows-Eingabeaufforderung. Während bei Linux ein SSH-Client zur Standardausrüstung gehört muss dieser unter Windows separat installiert werden. Eine Anleitung dafür findet sich im Artikel [SSH using Windows](https://www.raspberrypi.org/documentation/remote-access/ssh/windows.md).

### SSH-Zugriff
Auf neueren Images ist SSH aus Sicherheitsgründen standardmäßig deaktiviert. Zum Aktivieren gibt es verschiedene Möglichkeiten (siehe https://linuxundich.de/raspberry-pi/ssh-auf-dem-raspberry-pi-aktivieren-jetzt-unter-raspian-noetig oder
https://kofler.info/geaenderte-ssh-server-konfiguration-von-raspbian), wobei ich den nachfolgend beschriebenen Weg bevorzuge (geht mit diesen Befehlen so nur unter Linux):

1. Mounten der Boot-Partition der SD-Karte
   ```bash
   $ sudo mount /dev/mmcblk0p1 /mnt
   ```
2. Erzeugen einer leeren Datei mit dem Namen `ssh`:
   ```bash
   $ sudo touch /mnt/ssh
   ```
3. Unmounten der gemounteten Partition der SD-Karte
   ```bash
   $ sudo umount /mnt
   ```
Nachdem der Raspberry Pi mit der so modifizierten SD-Karte gebootet wurde, sollte der Zugriff mit SSH möglich sein. Dabei nicht vergessen, den Raspberry Pi über ein Ethernet-Kabel mit dem Router zu verbinden!
Für den zugriff benötigt man natürllich die IP-Adresse oder den Hostnamen, der dem Raspberry Pi vom Router zugewiesen wurde (in meinem Beispiel ist das `raspi`)

```bash
$ ssh pi@raspi
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

### <a name="root-filesystem-vergroesern"></a> Root-Filesystem vergrößern
Die Raspbian-Images werden in der Regel für SD-Karten mit einer Größe von 2 GB erstellt. Wenn die verwendete SD-Karte größer ist, bleibt der darüber hinausgehene Speicherplatz unbenutzt. Raspbian enthält jedoch das Utility `raspi-config`, mit dem man ganz einfach das Root-Filesystem so vergrößern kann, dass die gesamte SD-Karte genutzt wird (hier wurde eine 16 GB SD-Karte verwendet):

```bash
$ sudo raspi-config --expand-rootfs

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

### Pakete aktualisieren
Nach der Installation von Raspbian empfiehlt es sich, die Paket-Informationen zu aktualisieren:
```bash
$ sudo apt update
Hit:1 http://raspbian.raspberrypi.org/raspbian buster InRelease
Hit:2 http://archive.raspberrypi.org/debian buster InRelease
Reading package lists... Done
Building dependency tree       
Reading state information... Done
64 packages can be upgraded. Run 'apt list --upgradable' to see them.
```

Danach sollte man die installierten Pakete aktualisieren:
```bash
$ sudo apt upgrade
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

### WLAN einrichten (nicht verfügbar bei Raspberry Pi 2)
Soll der Raspberry Pi über WLAN statt über Ethernet angebunden werden, müssen SSID und Passwort in die Datei `/etc/wpa_supplicant/wpa_supplicant.conf` eingetragen werden. Eine genaue Beschreibung findet sich in [Setting WiFi up via the command line](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md).

### Hostnamen ändern
Unabhängig von dem Hostnamen, über den der Raspberry Pi im lokalen Netzwerk erreicht werden kann, ist sein Hostname standardmäßig `raspberry` (auch sichtbar am Prompt: `pi@raspberrypi:~ $`). Vor allem, wenn man mehrere Raspberry Pis im Netz hat, will man auch am Prompt sehen, auf welchem Raspberry man gerade die Befehle eingibt.

Zum Ändern des Hostnames kann das Tool `raspi-config` verwendet werden, indem der Menüpunkt _System Options_ und dann der Menüpunkt _Hostname_ gewählt wird:

```bash
$ sudo raspi-config
```

Wenn der Hostname so geändert wird, propagiert der Raspberry Pi seinen Namen auch, was zu Problemen führen kann, wenn bereits ein Raspberry Pi mit gleichem Namen läuft. In diesem Fall sollte der Hostname erst dann geändert werden, wenn der neue Raspberry Pi den bisherigen ersetzen soll.

### Zeitzone einstellen
Damit Zeitangaben zum Schalten der Geräte richtig interpretiert werden, sollte die Zeitzone des Raspberry auf die lokale Zeit gesetzt sein (nicht UTC!). Das kann mit folgendende Befehlen erreicht werden:

```bash
$ sudo /bin/bash -c "echo 'Europe/Berlin' > /etc/timezone"
$ sudo cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
```

## Java installieren
Zur Installation von Java ist folgender Befehl erforderlich:

```bash
$ sudo apt install openjdk-11-jre-headless
```

Die erfolgreiche Installation läßt sich mit folgendem Befehl überprüfen:

```bash
$ java -version
openjdk version "11.0.5" 2019-10-15
OpenJDK Runtime Environment (build 11.0.5+10-post-Raspbian-1deb10u1)
OpenJDK Server VM (build 11.0.5+10-post-Raspbian-1deb10u1, mixed mode)
```

#### Hinweis zum Raspberry Pi Zero
**Die nachfolgenden Hinweise stammen von Usern, die den *Smart Appliance Enabler* auf einem Raspberry Pi Zero betreiben. Diese Platform ist offiziell nicht unterstützt, also bitte keinen Support bei Problemen erwarten.**

OpenJDK 11 ist mit ARMv6 und ARMv7 leider nicht kompatibel. Dafür besteht die Möglichkeit über "Zulu Build for OpenJDK" von Azul eine Alternative zu installieren.
Dafür zuerst ein Verzeichnis erstellen und anschließend die Architektur (Soft Float (armsf) oder Hard Float (armhf)) des OS bestimmen:

```bash
$ sudo mkdir /opt/jdk
$ cd /opt/jdk
$ dpkg --print-architecture
```

Auf der [Downloadseite](https://www.azul.com/downloads/zulu-community/?version=java-11-lts&package=jdk) von Azul den Downloadlink der aktuellen Version für die passende Architektur (armsf oder armhf) kopieren. Danach downloaden, entpacken, aufräumen und verlinken:

```bash
$ sudo wget https://cdn.azul.com/zulu-embedded/bin/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz
$ sudo tar -xzvf zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz
$ sudo rm *.tar.gz
$ sudo update-alternatives --install /usr/bin/java java /opt/jdk/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf/bin/java 1
$ sudo update-alternatives --install /usr/bin/javac javac /opt/jdk/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf/bin/javac 1
```
Da die Raspberry Pi 1-Modelle bzw. die Pi-Zero weniger Rechenleistung bereitstellen, dauert der Start des SAE im Allgmeinen etwas länger. Aus diesem Grund müssen die Timeout-Zeiten noch angepasst werden müssen, um einen Abbruch beim Programmstart zu verhindern. Dafür mit einem Editor in `/opt/sae/smartapplianceenabler` den `sleep 1` durch `sleep 3` ersetzen und in /lib/systemd/system/smartapplianceenabler.service `TimeoutStartSec=90s` auf `TimeoutStartSec=180s` ändern.

## pigpiod installieren
Falls der *Smart Appliance Enabler* auf die GPIO-Anschlüsse des Raspberry Pi zugreifen soll, muss die Bibliothek [pigpiod](http://abyz.me.uk/rpi/pigpio/) installiert sein. Das lässt sich mit folgendem Befehlen erreichen:

```bash
$ sudo apt install pigpiod
```

Der `pigpiod` ist standardmässig so installiert, dass er nur lokale Zugriffe akzeptiert. Obwohl der *Smart Appliance Enabler* tatsächlich via `localhost` zugreift, ist der Zugriff auf den `pigpiod` nicht möglich, wenn er mit dieser Einschränkung gestartet wird. Zur Deaktivierung dieser Einschränkung muss in in der Datei `/lib/systemd/system/pigpiod.service` in der Zeile mit `ExecStart` der Parameter `-l` entfernt werden, sdoass sie wie folgt aussieht:

```bash
ExecStart=/usr/bin/pigpiod
```

Um den Deamon beim Systemstart automatisch via `systemd` zu starten, muss folgender Befehl ausgeführt werden:

```bash
$ sudo systemctl enable pigpiod
Created symlink /etc/systemd/system/multi-user.target.wants/pigpiod.service → /lib/systemd/system/pigpiod.service.
```

## MQTT-Broker
Der *Smart Appliance Enabler* benötigt einen MQTT-Broker, wobei ein bereits vorhandener MQTT-Broker genutzt werden kann. Falls noch kein MQTT-Broker vorhanden ist, empfiehlt sich die Verwendung von [Eclipse Mosquitto](https://mosquitto.org/).

### Installation und Konfiguration
[Eclipse Mosquitto](https://mosquitto.org/) lässt sich direkt aus Raspbian-Repository installieren:

```bash
$ sudo apt install mosquitto
```

In der Konfigurationsdatei `/etc/mosquitto/mosquitto.conf` sollte die Speicherung der MQTT-Nachrichten auf der SD-Karte deaktiviert werden. Dazu muss die entsprechende Zeile wie folgt aussehen:

```
persistence false
```

Um nicht-authentifizierten Zugriff auf den MQTT-Broker zuzulassen, muss die Datei `/etc/mosquitto/conf.d/smartapplianceenabler.conf` mit folgendem Inhalt erstellt werden:

```
listener 1883
allow_anonymous true
```

Zum Starten eignet sich folgender Befehl:

```bash
$ sudo systemctl start mosquitto
```

Um den MQTT-Broker beim Systemstart automatisch zu starten (via Systemd), muss folgender Befehl ausgeführt werden:

```bash
$ sudo systemctl enable mosquitto
Synchronizing state of mosquitto.service with SysV service script with /lib/systemd/systemd-sysv-install.
Executing: /lib/systemd/systemd-sysv-install enable mosquitto
```

### Installation und Konfiguration mit Docker
Für [Eclipse Mosquitto](https://mosquitto.org/) existiert ein Docker-Image:

```bash
$ docker pull eclipse-mosquitto
```

Zum Starten ohne Authentifizierung eignet sich folgender Befehl:

```bash
$ docker run \
    -it \
    --rm \
    -p 1883:1883 \
    --name mosquitto \
    eclipse-mosquitto \
    mosquitto -c /mosquitto-no-auth.conf
```

## Node-RED
Die optionale [Installaton von Node-RED](NodeRED_DE.md) ermöglicht die Nutzung eines detaillierten Dashboards auf Basis der MQTT-Nachrichten.

## Smart Appliance Enabler
### Erstinstallation

#### Start-Script und Konfigurationsdateien
Zunächst werden User und Gruppe angelegt, die beim Starten des *Smart Appliance Enabler* verwendet werden und denen bestimmte Dateien/Verzeichnisse gehören.
Danach werden Start-Script und zugehörige Konfigurationsdateien heruntergeladen und gleich die Berechtigungen für diese Dateien gesetzt.

```bash
$ sudo useradd -d /opt/sae -m -s /bin/bash sae
$ sudo usermod -a -G sudo sae
$ sudo passwd sae

$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/smartapplianceenabler -P /opt/sae
$ sudo chmod 755 /opt/sae/smartapplianceenabler

$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/smartapplianceenabler.service -P /lib/systemd/system
$ sudo chown root.root /lib/systemd/system/smartapplianceenabler.service
$ sudo chmod 755 /lib/systemd/system/smartapplianceenabler.service

$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/default/smartapplianceenabler -P /etc/default
$ sudo chown root.root /etc/default/smartapplianceenabler
$ sudo chmod 644 /etc/default/smartapplianceenabler

$ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/logback-spring.xml -P /opt/sae
$ sudo chmod 644 /opt/sae/logback-spring.xml

$ sudo chown -R sae:sae /opt/sae
```

Der *Smart Appliance Enabler* wird normalerweise als Service des [Systemd](https://de.wikipedia.org/wiki/Systemd) verwaltet. Dazu dient die Datei `/lib/systemd/system/smartapplianceenabler.service`.

Die Datei `/opt/sae/smartapplianceenabler` ist das eigentliche Start-Script für den *Smart Appliance Enabler*. Man kann es zwar direkt aufrufen, aber eigentlich sollte es nur vom _Systemd_ verwendet werden.

Siehe auch:
- [Konfigurationseinstellungen](ConfigurationFiles_DE.md#user-content-etc-default-smartapplianceenabler)
- [Konfiguration des Loggings](ConfigurationFiles_DE.md#user-content-log-konfiguration)

Damit der *Smart Appliance Enabler* beim Systemstart ebenfalls gestartet wird (via Systemd), muss folgender Befehl ausgeführt werden:

```bash
$ sudo systemctl enable smartapplianceenabler
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler.service → /lib/systemd/system/smartapplianceenabler.service.
```

Nach diesen Änderungen muss der Systemd die Service-Konfigurationen neu einlesen:

```bash
$ sudo systemctl daemon-reload
```

Die erfolgreiche Registrierung des Dienstes *smartapplianceenabler* kann wie folgt überprüft werden:

```bash
$ systemctl list-units | grep smartapplianceenabler
smartapplianceenabler.service                                                                loaded failed failed    Smart Appliance Enabler
```

Falls die zweite Zeile nicht angezeigt wird, sollte der Raspberry Pi neu gestartet werden.

#### Programm-Download
Der eigentliche Programmcode befindet sich in der Datei `SmartApplianceEnabler-X.Y.Z.war`, die ebenfalls heruntergeladen werden muss. *X.Y.Z* steht dabei für die aktuelle Versionsnummer (z.B. 2.1.0), die [hinter dem Download-Button](https://github.com/camueller/SmartApplianceEnabler#smart-appliance-enabler) angezeigt wird. 

```bash
$ VERSION=2.1.0
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/releases/download/${VERSION}/SmartApplianceEnabler-${VERSION}.war -P /opt/sae
$ sudo chown -R sae:sae /opt/sae
```

Nach dem Download sollte geprüft werden, dass die heruntergeladene Programm-Datei mindestens 20 MB gross ist - andernfalls wurde möglicherweise eine inkorrekte URL verwendet:

```bash
$ ls -al /opt/sae/*.war
-rw-r--r-- 1 sae sae 23040544 Oct 20 08:49 /opt/sae/SmartApplianceEnabler-1.4.15.war
```

#### Start
Jetzt sollte man den *Smart Appliance Enabler* starten können:

```bash
$ sudo systemctl start smartapplianceenabler
```

Je nach Raspberry Pi-Model dauert der Start bis zu 60 Sekunden.

Wenn der *Smart Appliance Enabler* läuft, muss als Nächstes die [Konfiguration](Configuration_DE.md) vorgenommen werden.

#### Stop
Das Stoppen des  *Smart Appliance Enabler* erfolgt so:

```bash
$ sudo systemctl stop smartapplianceenabler.service
```

#### Status
Mit folgendem Befehl lässt sich üprüfen, ob der *Smart Appliance Enabler* läuft:

```bash
$ sudo systemctl status smartapplianceenabler.service
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
Zum Update einer vorhandenen Version muss zunächst das alte Programm gelöscht werden:

```bash
$ rm /opt/sae/*.war
```

Jetzt kann die gewünschte Version des Programms heruntergeladen und wie im Erstinstallations-Kapitel [Programm-Download](#programm-download) beschrieben installiert werden.

In den allermeisten Fällen kann die neue Version des *Smart Appliance Enabler* die Konfigurationsdateien der alten Version auf die neue Version migrieren. Falls sich beim Starten der neuen Version des *Smart Appliance Enabler* im Log Fehlermeldungen finden, welche eindeutig auf Probleme mit der Konfigurationsdatei hindeuten, sollten die Dateien so umbenannt werden, dass sie nicht verwendet werden aber ggf. wieder reaktiviert werden können:

```bash
$ mv /opt/sae/Appliances.xml /opt/sae/Appliances.xml.old
$ mv /opt/sae/Device2EM.xml /opt/sae/Device2EM.xml.old
```

Danach muss die Konfiguration wie im Kapitel [Konfiguration](Configuration_DE.md) beschrieben neu erstellt werden.

### <a name="notifications"></a> Benachrichtigungen
Für den optionalen Versand von Benachrichtigungen via Instant-Messanger wie [Telegram](http://www.telegram.org), muss das entsprechende Shell-Script heruntergeladen und ausführbar gemacht werden:

```bash
$ wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/notifyWithTelegram.sh -P /opt/sae
$ chmod +x /opt/sae/notifyWithTelegram.sh
```
