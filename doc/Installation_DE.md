# Installation
Die nachfolgenden Kapitel sollten in der angegebenen Reihenfolge umgesetzt werden.

## Betriebssystem
### Allgemeine Hinweise
Das Betriebssystem für den Raspberry ist Linux. Die Interaktion mit Linux erfolgt dabei über die sogenannte Shell (vergleichbar der DOS-Box bzw. cmd.exe unter Windows). Eine deutschsprachige Einführung dazu findet sich [hier](https://wiki.ubuntuusers.de/Shell/Einf%C3%BChrung/).
In der Dokumentation zum *Smart Appliance Enabler* finden sich immer wieder Shell-Befehle und die dazu korrespondieren Ausgaben. Zur besseren Lesbarkeit bietet Github ein Farbschema an, das verschiedene Farben für die einzelnen Elemente verwendet:
* violett: Eingabeaufforderung bzw. Prompt (endet mit dem $-Zeichen)
* schwarz: einzugebender Befehl (Dollar-Zeichen und Leerzeichen zu Beginn gehören nicht dazu!)
* blau: Ausgabe bzw. Antwort auf den eingegebenen Befehl

Die genannten Elemente finden sich alle in dem nachfolgenden Beispiel:
```console
pi@raspi:~ $ uname -a
Linux raspi3 4.19.75-v7+ #1270 SMP Tue Sep 24 18:45:11 BST 2019 armv7l GNU/Linux
```

### Raspbian
Für den Raspberry Pi existieren verschiedene, darauf zugeschnittene Linux-Distributionen, wobei [Raspbian](https://www.raspberrypi.org/downloads/raspbian) vermutlich die geläufigste ist und hier verwendet werden soll.

Beim Raspbian-Image ist die **Lite-Version** ausreichend, sodass man eine *4GB-SD-Karte* verwenden kann.

_**Für Smart Appliancer Enabler bis einschliesslich Version 1.4 gilt:**_ Es muss Rasbian Stretch verwendet werden (Raspbian Buster oder neuer ist nicht geeignet!!!). Download: https://downloads.raspberrypi.org/raspbian_lite/images/raspbian_lite-2019-04-09/

_**Für Smart Appliancer Enabler ab Version 1.5 gilt:**_ Es ist mindestens Raspbian Buster erforderlich. 

Mit dem nachfolgenden Befehl kann man unter Linux ein Image auf eine SD-Karte schreiben:
```console
axel@p51:/tmp$ sudo dd bs=4M if=2019-09-26-raspbian-buster-lite.img of=/dev/mmcblk0 status=progress oflag=sync
[sudo] password for axel: 
2248146944 bytes (2.2 GB, 2.1 GiB) copied, 280 s, 8.0 MB/s 
536+0 records in
536+0 records out
2248146944 bytes (2.2 GB, 2.1 GiB) copied, 280.242 s, 8.0 MB/s
```
Sollte der Raspberry mit der SD-Karte nicht starten, kann es durchaus an der SD-Karte selbst liegen. In diesem Fall einfach einen anderen SD-Karten-Typ verwenden (gute Erfahrungen habe ich mit SanDisk gemacht). Einen erfolgreichen Start erkennt man leicht daran, dass die grüne LED flackert/leuchtet (= Zugriff auf die SD-Karte).

### SSH-Client
Die Interaktion mit dem Raspberry Pi erfolt über SSH (Secure Shell), d.h. über ein Fenster vergleichbar der Windows-Eingabeaufforderung. Während bei Linux ein SSH-Client zur Standardausrüstung gehört muss dieser unter Windows separat installiert werden. Eine Anleitung dafür findet sich im Artikel [SSH using Windows](https://www.raspberrypi.org/documentation/remote-access/ssh/windows.md).

### SSH-Zugriff
Auf neueren Images ist SSH aus Sicherheitsgründen standardmäßig deaktiviert. Zum Aktivieren gibt es verschiedene Möglichkeiten (siehe https://linuxundich.de/raspberry-pi/ssh-auf-dem-raspberry-pi-aktivieren-jetzt-unter-raspian-noetig oder 
https://kofler.info/geaenderte-ssh-server-konfiguration-von-raspbian), wobei ich den nachfolgend beschriebenen Weg bevorzuge (geht mit diesen Befehlen so nur unter Linux):

1. Mounten der Boot-Partition der SD-Karte
```console
axel@tpw520:~$ sudo mount /dev/mmcblk0p1 /mnt
```
2. Erzeugen einer leeren Datei mit dem Namen ```ssh```:
```console
axel@tpw520:~$ sudo touch /mnt/ssh
```
3. Unmounten der gemounteten Partition der SD-Karte
```console
axel@tpw520:~$ sudo umount /mnt
```
Nachdem der Raspberry Pi mit der so modifizierten SD-Karte gebootet wurde, sollte der Zugriff mit SSH möglich sein.
Dabei nicht vergessen, den Raspberry Pi über ein Ethernet-Kabel mit dem Router zu verbinden!
Für den zugriff benötigt man natürllich die IP-Adresse oder den Hostnamen, der dem Raspberry Pi vom Router zugewiesen wurde (in meinem Beispiel ist das ```raspi```)
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
### Pakete aktualisieren
Nach der Installation von Raspbian empfiehlt es sich, die Paket-Informationen zu aktualisieren:
```console
pi@raspi ~ $ sudo apt update
Hit:1 http://raspbian.raspberrypi.org/raspbian buster InRelease
Hit:2 http://archive.raspberrypi.org/debian buster InRelease
Reading package lists... Done
Building dependency tree       
Reading state information... Done
64 packages can be upgraded. Run 'apt list --upgradable' to see them.
```

Danach sollte man die installierten Pakete aktualisieren:
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

### WLAN einrichten (nicht verfügbar bei Raspberry Pi 2)
Soll der Raspberry Pi über WLAN statt über Ethernet angebunden werden, müssen SSID und Passwort in die Datei ```/etc/wpa_supplicant/wpa_supplicant.conf``` eingetragen werden. Eine genaue Beschreibung findet sich in [Setting WiFi up via the command line](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md).

### Hostnamen ändern
Unabhängig von dem Hostnamen, über den der Raspberry im lokalen Netzwerk erreicht werden kann, ist sein Hostname standardmäßig ```raspberry``` (auch sichtbar am Prompt: ```pi@raspberrypi:~ $```). Vor allem, wenn man mehrere Raspberries im Netz hat, will man auch am Prompt sehen, auf welchem Raspberry man gerade die Befehle eingibt. Zum Ändern des Hostnames kann nachfolgender Befehl auf dem Raspberry verwendet werden:
```console
pi@raspberrypi ~ $ sudo hostname -b raspi3
```

### Zeitzone einstellen
Damit Zeitangaben zum Schalten der Geräte richtig interpretiert werden, sollte die Zeitzone des Raspberry auf die lokale Zeit gesetzt sein (nicht UTC!). Das kann mit folgendende Befehlen erreicht werden:
```console
pi@raspberrypi ~ $ sudo /bin/bash -c "echo 'Europe/Berlin' > /etc/timezone"
pi@raspberrypi ~ $ sudo cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
```

### Java installieren
#### Smart Appliancer Enabler bis einschliesslich Version 1.4
Die Installation des vom *Smart Appliance Enabler* benötigten Java 8 erfolgt ganz einfach mit
```console
pi@raspberrypi ~ $ sudo apt install oracle-java8-jdk
```
Die erfolgreiche Installation läßt sich mit folgendem Befehl überprüfen:
```console
pi@raspberrypi:~ $ java -version
java version "1.8.0_65"
Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
Java HotSpot(TM) Client VM (build 25.65-b01, mixed mode)
```
#### Smart Appliancer Enabler ab Version 1.5

```console
pi@raspberrypi:~ $ sudo apt install openjdk-11-jre-headless
```
Die erfolgreiche Installation läßt sich mit folgendem Befehl überprüfen:
```console
pi@raspberrypi:~ $ java -version
openjdk version "11.0.5" 2019-10-15
OpenJDK Runtime Environment (build 11.0.5+10-post-Raspbian-1deb10u1)
OpenJDK Server VM (build 11.0.5+10-post-Raspbian-1deb10u1, mixed mode)
```

## Wiring-Pi installieren
Falls der *Smart Appliance Enabler* auf die GPIO-Anschlüsse des Raspberry Pi zugreifen soll, muss die Biliothek [Wiring Pi](http://wiringpi.com/) installiert sein. Das lässt sich mit folgendem Befehl erreichen:
```console
pi@raspberrypi ~ $ sudo apt install wiringpi
```

## Smart Appliance Enabler
### Erstinstallation

#### Start-Script und Konfigurationsdateien
Zunächst werden User und Gruppe angelegt, die beim Starten des *Smart Appliance Enabler* verwendet werden und denen bestimmte Dateien/Verzeichnisse gehören.
Danach werden Start-Script und zugehörige Konfigurationsdateien heruntergeladen und gleich die Berechtigungen für diese Dateien gesetzt.
```console
pi@raspberrypi ~ $ sudo mkdir /opt/sae
pi@raspberrypi ~ $ sudo groupadd sae
pi@raspberrypi ~ $ sudo useradd -d /opt/sae -c "SmartApplianceEnabler" -g sae -M sae -s /bin/bash
pi@raspberrypi ~ $ sudo usermod -a -G gpio,sudo sae
pi@raspberrypi ~ $ sudo passwd sae
pi@raspberrypi ~ $ sudo cp /home/pi/.profile /opt/sae
pi@raspberrypi ~ $ sudo cp /home/pi/.bashrc /opt/sae

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

Der *Smart Appliance Enabler* wird normalerweise als Service des [Systemd](https://de.wikipedia.org/wiki/Systemd) verwaltet. Dazu dient die Datei ```/lib/systemd/system/smartapplianceenabler.service```.

Die Datei ```/opt/sae/smartapplianceenabler``` ist das eigentliche Start-Script für den *Smart Appliance Enabler*. Man kann es zwar direkt aufrufen, aber eigentlich sollte es nur vom _Systemd_ verwendet werden.

In der Datei ```/etc/default/smartapplianceenabler``` finden sich die Konfigurationseinstellungen für den *Smart Appliance Enabler*. Die darin befindlichen Parameter (z.B. Netzwerk-Adresse, Port, Java-Einstellungen, ...) sind in der Datei selbst dokumentiert. Normalerweise sollte man die Datei unverändert lassen können.

Die Konfiguration des Loggings erfolgt in der Datei ```/opt/sae/logback-spring.xml```.

Damit der *Smart Appliance Enabler* beim Systemstart ebenfalls gestartet wird (via Systemd), muss folgender Befehl ausgeführt werden:
```console
pi@raspberrypi ~ $ sudo systemctl enable smartapplianceenabler.service
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler.service → /lib/systemd/system/smartapplianceenabler.service.
```
Nach diesen Änderungen muss der Systemd dazu gebracht werden, die Service-Konfigurationen neu zu lesen:
```console
pi@raspberrypi ~ $ sudo systemctl daemon-reload
```
Die erfolgreiche Registrierung des Dienstes *smartapplianceenabler* kann wie folgt überprüft werden:
```console
pi@raspberrypi ~ $ systemctl list-units|grep smartapplianceenabler
smartapplianceenabler.service                                                                loaded failed failed    Smart Appliance Enabler
```
Falls die zweite Zeile nicht angezeigt wird, sollte der Raspberry neu gestartet werden.

#### Programm-Download
Der eigentliche Programmcode befindet sich in der Datei ```SmartApplianceEnabler-X.Y.Z.war```, die ebenfalls heruntergeladen werden muss. *X.Y.Z* steht dabei für die aktuelle Versionsnummer (z.B. 1.3.50), die [hinter dem Download-Button](https://github.com/camueller/SmartApplianceEnabler#smart-appliance-enabler) angezeigt wird. Entsprechend dieser Hinweise muss die Version im nachfolgenden Befehl angepasst werden an 2 Stellen (*v1.3.501* und *SmartApplianceEnabler-1.3.50.war*):
```console
pi@raspberrypi ~ $ wget https://github.com/camueller/SmartApplianceEnabler/releases/download/v1.3.50/SmartApplianceEnabler-1.3.50.war -P /opt/sae
pi@raspberrypi ~ $ sudo chown -R sae:sae /opt/sae
```

Nach dem Download sollte geprüft werden, dass die heruntergeladene Programm-Datei mindestens 20 MB gross ist - andernfalls wurde möglicherweise eine inkorrekte URL verwendet:
```console
pi@raspberrypi ~ $ ls -al /opt/sae/*.war
-rw-r--r-- 1 sae sae 23040544 Oct 20 08:49 /opt/sae/SmartApplianceEnabler-1.4.15.war
```

#### Start
Jetzt sollte man den *Smart Appliance Enabler* starten können:
```console
pi@raspberrypi:~ $ sudo systemctl start smartapplianceenabler.service
```
Je nach Raspberry Pi-Model dauert der Start bis zu 60 Sekunden. 
Weil bei diesem Befehl keine Ausgaben auf der Konsole erfolgen, sollte [überprüft werden, ob der *Smart Appliance Enabler* tatsächlich läuft](Support_DE.md#l%C3%A4uft-der-smart-appliance-enabler). Auf dieser Seite finden sich auch Hinweise, falls es Probleme gibt.

Wenn der *Smart Appliance Enabler* läuft, muss als Nächstes die [Konfiguration](Configuration_DE.md) vorgenommen werden.

#### Stop
Das Stoppen des  *Smart Appliance Enabler* erfolgt so:
```console
pi@raspberrypi:~ $ sudo systemctl stop smartapplianceenabler.service
```

### Update
Das Update einer vorhandenen Version besteht darin, zunächst das alte Programm zu löschen:
```console
pi@raspberrypi ~ $ rm /opt/sae/*.war
```
Jetzt kann die gewünschte Version des Programms heruntergeladen werden, wie im Erstinstallations-Kapitel [Programm-Download](#programm-download) beschrieben.
Falls das Format der Konfigurationsdatei ```Appliances.xml``` in der neuen Programmversion nicht mehr kompatibel zur alten Version ist oder man sich diesbezüglich unsicher ist, müssen die alten Konfigurationsdateien gelöscht werden:
```console
pi@raspberrypi ~ $ rm /opt/sae/Appliances.xml
pi@raspberrypi ~ $ rm /opt/sae/Device2EM.xml
```
Nach dem Löschen dieser beiden Datei muss die Konfiguration neu erstellt werden, wie im Kapitel [Konfiguration](Configuration_DE.md) beschrieben.
