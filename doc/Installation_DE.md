# Installation
Die nachfolgenden Kapitel sollten in der angegebenen Reihenfolge umgesetzt werden.

## Betriebssystem
Für den Raspberry Pi existieren verschiedene, darauf zugeschnittene, Linux-Distributionen (Images), wobei [Raspbian](https://www.raspberrypi.org/downloads/raspbian) vermutlich das geläufigste ist (auf dieses beziehe ich mich nachfolgend). 
Damit der *Smart Appliance Enabler* lauffähig ist, muss bei der Wahl des Images ist darauf geachtet werden, dass dieses eine Java 8-Runtime enthält oder dass diese nachinstallierbar ist. Beim Raspbian-Image ist die Lite-Version ausreichend, sodass man eine 4GB-SD-Karte verwenden kann.

Mit dem nachfolgenden Befehl kann man unter Linux ein Image auf eine SD-Karte schreiben:
```
axel@tpw520:~/Downloads/raspberry$ sudo dd bs=4M if=2017-04-10-raspbian-jessie-lite.img of=/dev/mmcblk0
[sudo] password for axel:
309+1 records in      
309+1 records out
1297862656 bytes (1,3 GB, 1,2 GiB) copied, 216,029 s, 6,0 MB/s
```
Sollte der Raspberry mit der SD-Karte nicht starten, kann es durchaus an der SD-Karte selbst liegen (diese Situation hatte ich gerade selbst). In diesem Fall einfach einen anderen SD-Karten-Typ verwenden (gute Erfahrungen habe ich mit SanDisk gemacht). Einen erfolgreichen Start erkennt man leicht daran, dass die grüne LED flackert/leuchtet (= Zugriff auf die SD-Karte).

### SSH-Zugriff ermöglichen
Auf neueren Images ist SSH aus Sicherheitsgründen standardmäßig deaktiviert. Zum Aktivieren gibt es verschiedene Möglichkeiten (siehe https://linuxundich.de/raspberry-pi/ssh-auf-dem-raspberry-pi-aktivieren-jetzt-unter-raspian-noetig oder 
https://kofler.info/geaenderte-ssh-server-konfiguration-von-raspbian), wobei ich den nachfolgend beschriebenen Weg bevorzuge (geht mit diesen Befehlen so nur unter Linux):

1. Mounten der Boot-Partition der SD-Karte
```
axel@tpw520:~$ sudo mount /dev/mmcblk0p1 /media/axel/tmp
```
2. Erzeugen einer leeren Datei mit dem Namen ```ssh```:
```
axel@tpw520:~$ touch /media/axel/tmp/ssh
```
3. Unmounten der gemounteten Partition der SD-Karte
```
axel@tpw520:~$ sudo umount /media/axel/tmp
```
Nachdem der Raspberry Pi mit der so modifizierten SD-Karte gebootet wurde, sollte der Zugriff mit SSH möglich sein.

Dabei nicht vergessen, den Raspberry Pi über ein Ethernet-Kabel mit dem Router zu verbinden!

### SSH-Client
Die Interaktion mit dem Raspberry Pi erfolt über SSH (Secure Shell), d.h. über ein Fenster vergleichbar der Windows-Eingabeaufforderung. Während bei Linux ein SSH-Client zur Standardausrüstung gehört muss dieser unter Windows separat installiert werden. Eine Anleitung dafür findet sich im Artikel [SSH using Windows](https://www.raspberrypi.org/documentation/remote-access/ssh/windows.md).

In den nachfolgenden Kapiteln sind diverse Befehle aufgeführt. Am Prompt ```pi@raspberrypi ~ $``` lässt sich leicht erkennen, dass der nachfolgende Befehl in der SSH-Shell auf dem Raspberry Pi einzugeben ist (```pi``` ist dabei der Username, der für die Anmeldung benutzt wurde und ```raspberrypi``` ist der Hostname. ```~``` symbolisiert das Home-Verzeichnis des Users - ansonsten wird das aktuelle Verzeichnis direkt angezeigt).

Der eigentliche, einzugebende Befehl beginnt dabei erst nach dem ```$```-Zeichen!

### WLAN einrichten (nur Raspberry Pi 3)
Soll der Raspberry Pi über WLAN statt über Ethernet angebunden werden, müssen SSID und Passwort in die Datei ```/etc/wpa_supplicant/wpa_supplicant.conf``` eingetragen werden. Eine genaue Beschreibung findet sich im Kapitel [ADDING THE NETWORK DETAILS TO THE RASPBERRY PI](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md).

### Hostnamen ändern
Unabhängig von dem Hostnamen, über den der Raspberry im lokalen Netzwerk erreicht werden kann, ist sein Hostname standardmäßig ```raspberry``` (auch sichtbar am Prompt: ```pi@raspberrypi:~ $```). Vor allem, wenn man mehrere Raspberries im Netz hat, will man auch am Prompt sehen, auf welchem Raspberry man gerade die Befehle eingibt. Zum Ändern des Hostnames kann nachfolgender Befehl auf dem Raspberry verwendet werden:
```
pi@raspberrypi ~ $ sudo hostname -b raspi3
```

### Zeitzone einstellen
Damit Zeitangaben zum Schalten der Geräte richtig interpretiert werden, sollte die Zeitzone des Raspberry auf die lokale Zeit gesetzt sein (nicht UTC!). Das kann mit folgendende Befehlen erreicht werden:
```
pi@raspberrypi ~ $ sudo /bin/bash -c "echo 'Europe/Berlin' > /etc/timezone"
pi@raspberrypi ~ $ sudo cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
```

### Java 8 installieren
Die Installation des vom *Smart Appliance Enabler* benötigten Java 8 erfolgt ganz einfach mit
```
pi@raspberrypi ~ $ sudo apt-get update
pi@raspberrypi ~ $ sudo apt-get install oracle-java8-jdk
```
Die Java-Version läßt sich mit folgendem Befehl überprüfen:
```
pi@raspberrypi:~ $ java -version
java version "1.8.0_65"
Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
Java HotSpot(TM) Client VM (build 25.65-b01, mixed mode)
```

## Smart Appliance Enabler
Die Installation des *Smart Appliance Enabler* besteht darin, folgende Dateien auf den Raspberry zu kopieren:
* das Start-Script und zugehörige Konfigurationsdateien
* die Datei ```SmartApplianceEnabler-*.jar``` mit dem eigentlichen Programmcode (heruntergeladenes Release oder aus Sourcen gebaut)
* die Konfigurationsdatei ```Device2EM.xml```
* die Konfigurationsdatei ```Appliances.xml```

Zunächst werden Start-Script und zugehörige Konfigurationsdateien auf den Raspberry heruntergeladen und gleich die Berechtigungen für dieses Dateien gesetzt:
```
axel@tpw520:/data/git/SmartApplianceEnabler$ ssh pi@raspi
pi@raspi's password: 

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Sun Dec  6 19:17:12 2015

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/init.d/smartapplianceenabler -P /etc/init.d
pi@raspberrypi ~ $ sudo chown root.root /etc/init.d/smartapplianceenabler
pi@raspberrypi ~ $ sudo chmod 755 /etc/init.d/smartapplianceenabler

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/default/smartapplianceenabler -P /etc/default
pi@raspberrypi ~ $ sudo chown root.root /etc/default/smartapplianceenabler
143
pi@raspberrypi ~ $ sudo chmod 644 /etc/default/smartapplianceenabler

pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/logrotate.d/smartapplianceenabler -P /etc/logrotate.d
pi@raspberrypi ~ $ sudo chown root.root /etc/logrotate.d/smartapplianceenabler
pi@raspberrypi ~ $ sudo chmod 644 /etc/logrotate.d/smartapplianceenabler
```
In der Datei ```/etc/default/smartapplianceenabler``` finden sich die Konfigurationseinstellungen für den Dienst *smartapplianceenabler*. Die darin befindlichen Parameter sind in der Datei selbst dokumentiert. Normalerweise sollte man die Datei unverändert lassen können.

Damit der Dienst *smartapplianceenabler* beim Systemstart ebenfalls gestartet wird, muss folgender Befehl ausgeführt werden:
```
pi@raspberrypi ~ $ sudo systemctl enable smartapplianceenabler.service
```
Nach diesen Änderungen muss der [Systemd](https://de.wikipedia.org/wiki/Systemd) dazu gebracht werden, die Service-Konfigurationen neu zu lesen:
```
pi@raspberrypi ~ $ sudo systemctl daemon-reload
```
Die erfolgreiche Reistrierung des Dienstes *smartapplianceenabler* kann wie folgt überprüft werden:
```
pi@raspberrypi ~ $ systemctl list-units|grep smart
smartapplianceenabler.service                                                                           loaded activating start     start LSB: Start Smart Appliance Enabler.
```
Falls die zweite Zeile nicht angezeigt wird, sollte der Raspberry neu gestartet werden.

Als nächstes wird die Datei ```SmartApplianceEnabler-*.jar``` mit dem eigentlichen Programmcode sowie die Konfigurationsdateien ```Appliances.xml``` und ```Device2EM.xml``` heruntergeladen:
```
pi@raspberrypi ~ $ sudo mkdir /app
pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/releases/download/v1.1.0/SmartApplianceEnabler-1.1.0.jar -P /app
pi@raspberrypi ~ $ sudo chown -R pi.pi /app
pi@raspberrypi ~ $ wget https://github.com/camueller/SmartApplianceEnabler/raw/master/example/Appliances.xml -P /app
pi@raspberrypi ~ $ wget https://github.com/camueller/SmartApplianceEnabler/raw/master/example/Device2EM.xml -P /app
```
Jetzt sollte man den *Smart Appliance Enabler* starten können. Auf einem aktuellen Raspberry Pi dauert der Start ca. 30 Sekunden.  Dabei sollte man folgende Ausgaben zu sehen bekommen:
```
pi@raspberrypi ~ $ sudo /etc/init.d/smartapplianceenabler start
[ ok ] Starting smartapplianceenabler (via systemctl): smartapplianceenabler.service.
```
Verwendet man `stop` anstelle von `start`, wird der Service *smartapplianceenabler* beendet.
Mit dem Parameter `status` kann geprüft werden, ob der Service läuft:
```
pi@raspberrypi ~ $ sudo /etc/init.d/smartapplianceenabler status
● smartapplianceenabler.service - LSB: Start Smart Appliance Enabler.
   Loaded: loaded (/etc/init.d/smartapplianceenabler)
   Active: active (running) since Sat 2016-01-09 16:27:07 UTC; 2min 49s ago
  Process: 17288 ExecStart=/etc/init.d/smartapplianceenabler start (code=exited, status=0/SUCCESS)
   CGroup: /system.slice/smartapplianceenabler.service
           └─17300 /usr/lib/jvm/jdk-8-oracle-arm-vfp-hflt/bin/java -Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC -Dsae.pidfile=/var/run/smartapplianceenabler.pid -Dsae.logfile=/var/log/smartapplianceenabler.log -Dsae.loglevel=INFO -Dappliance.dir=/app -jar...

Jan 09 16:26:36 raspberrypi smartapplianceenabler[17288]: Starting smartapplianceenabler: smartapplianceenablertouch: cannot touch ‘’: No such file or directory
Jan 09 16:27:07 raspberrypi smartapplianceenabler[17288]: .
Jan 09 16:27:07 raspberrypi systemd[1]: Started LSB: Start Smart Appliance Enabler..
```
Eigentlich lässt man Dienste wie *smartapplianceenabler* nicht unter dem Benutzer *root* laufen. Allerdings habe ich bisher keine Möglichkeit gefunden, die Rechte für den Zugriff auf die GPIO-Ports so zu setzen, dass diese auch für andere Benutzer möglich ist.

Falls beim Starten ein Fehler auftritt, kann man die Details mit `journalctl` anzeigen lassen (`sudo` nicht vergessen, sonst bekommt man nichts zu sehen!):
```
pi@raspberrypi /etc/logrotate.d $ sudo journalctl -xn
-- Logs begin at Sat 2016-01-09 06:17:01 UTC, end at Sat 2016-01-09 16:30:39 UTC. --
Jan 09 16:27:07 raspberrypi systemd[1]: Started LSB: Start Smart Appliance Enabler..
-- Subject: Unit smartapplianceenabler.service has finished start-up
-- Defined-By: systemd
-- Support: http://lists.freedesktop.org/mailman/listinfo/systemd-devel
-- 
-- Unit smartapplianceenabler.service has finished starting up.
-- 
-- The start-up result is done.
Jan 09 16:27:07 raspberrypi sudo[17271]: pam_unix(sudo:session): session closed for user root
Jan 09 16:28:25 raspberrypi sudo[17422]: pi : TTY=pts/0 ; PWD=/home/pi ; USER=root ; COMMAND=/bin/cat /var/log/smartapplianceenabler.log
Jan 09 16:28:25 raspberrypi sudo[17422]: pam_unix(sudo:session): session opened for user root by pi(uid=0)
Jan 09 16:28:25 raspberrypi sudo[17422]: pam_unix(sudo:session): session closed for user root
Jan 09 16:29:56 raspberrypi sudo[17437]: pi : TTY=pts/0 ; PWD=/home/pi ; USER=root ; COMMAND=/etc/init.d/smartapplianceenabler status
Jan 09 16:29:56 raspberrypi sudo[17437]: pam_unix(sudo:session): session opened for user root by pi(uid=0)
Jan 09 16:29:56 raspberrypi sudo[17437]: pam_unix(sudo:session): session closed for user root
Jan 09 16:30:39 raspberrypi sudo[17457]: pi : TTY=pts/1 ; PWD=/etc/logrotate.d ; USER=root ; COMMAND=/bin/journalctl -xn
Jan 09 16:30:39 raspberrypi sudo[17457]: pam_unix(sudo:session): session opened for user root by pi(uid=0)
```
Der *Smart Appliance Enabler* schreibt seine Log-Informationen in die Datei `/var/log/smartapplianceenabler.log`. Der Detaillierungsgrad wird durch den gewählten Log-Level in der Datei `/etc/default/smartapplianceenabler` bestimmt.
Mit dem Log-Level INFO sind nach dem Start des *Smart Appliance Enabler* folgende Einträge zu sehen:
```
pi@raspberrypi ~ $ sudo cat /var/log/smartapplianceenabler.log 
2016-01-09 16:27:06,864 INFO [Thread-4] o.f.c.UpnpServiceImpl [UpnpServiceImpl.java:71] >>> Starting UPnP service...
2016-01-09 16:27:06,874 INFO [Thread-4] o.f.c.UpnpServiceImpl [UpnpServiceImpl.java:73] Using configuration: de.avanux.smartapplianceenabler.semp.discovery.SempDiscovery$1
2016-01-09 16:27:07,078 INFO [Thread-4] o.f.c.t.Router [RouterImpl.java:94] Creating Router: org.fourthline.cling.transport.RouterImpl
2016-01-09 16:27:07,135 INFO [Thread-4] o.f.c.t.s.MulticastReceiver [MulticastReceiverImpl.java:76] Creating wildcard socket (for receiving multicast datagrams) on port: 1900
2016-01-09 16:27:07,153 INFO [Thread-4] o.f.c.t.s.MulticastReceiver [MulticastReceiverImpl.java:83] Joining multicast group: /239.255.255.250:1900 on network interface: eth0
2016-01-09 16:27:07,240 INFO [Thread-7] d.a.s.a.ApplianceManager [ApplianceManager.java:65] 1 appliance(s) configured.
2016-01-09 16:27:07,250 INFO [Thread-4] o.f.c.t.s.StreamServer [StreamServerImpl.java:80] Created socket (for receiving TCP streams) on: /192.168.69.5:47336
2016-01-09 16:27:07,266 INFO [Thread-4] o.f.c.t.s.DatagramIO [DatagramIOImpl.java:84] Creating bound socket (for datagram input/output) on: /192.168.69.5
2016-01-09 16:27:07,269 INFO [Thread-7] d.a.s.a.Switch [Switch.java:41] Switch uses pin GPIO 1 (reversed states)
2016-01-09 16:27:07,610 INFO [Thread-4] o.f.c.UpnpServiceImpl [UpnpServiceImpl.java:94] <<< UPnP service started successfully
2016-01-09 16:27:10,130 INFO [cling-5] d.a.s.s.d.SempDeviceDescriptorBinderImpl [SempDeviceDescriptorBinderImpl.java:70] SEMP UPnP will redirect to http://192.168.69.5:8080
```
