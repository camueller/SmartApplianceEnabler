# Installation
## Betriebssystem
Für den Raspberry Pi existieren verschiedene, darauf zugeschnittene, Linux-Distributionen (Images), wobei [Raspbian](https://www.raspberrypi.org/downloads/raspbian) vermutlich das geläufigste ist (auf dieses beziehe ich mich nachfolgend). 
Beim Raspbian-Image ist die Lite-Version ist ausreichend, was insbesondere bei Verwendung von 4GB-SD-Karten hilfreich ist. Damit der *Smart Appliance Enabler* darauf lauffähig ist, muss bei der Wahl des Images ist darauf geachtet werden, dass dieses eine Java8-Runtime enthält oder dass diese nachinstallierbar ist.
```
axel@tpw520:~/Downloads/raspberry$ sudo dd bs=4M if=2017-04-10-raspbian-jessie-lite.img of=/dev/mmcblk0
[sudo] password for axel:
309+1 records in      
309+1 records out
1297862656 bytes (1,3 GB, 1,2 GiB) copied, 216,029 s, 6,0 MB/s
```
### SSH einrichten
Auf neueren Images ist SSH aus Sicherheitsgründen standardmäßig deaktiviert. Zum Aktivieren gibt es verschiedene Möglichkeiten (siehe https://linuxundich.de/raspberry-pi/ssh-auf-dem-raspberry-pi-aktivieren-jetzt-unter-raspian-noetig oder 
https://kofler.info/geaenderte-ssh-server-konfiguration-von-raspbian), wobei ich den nachfolgend beschriebenen Weg bevorzuge (geht nur unter Linux):

1. Mounten der Boot-Partition der SD-Karte
```
axel@tpw520:~$ sudo mount /dev/mmcblk0p1 /media/axel/tmp
```
2. Erzeugen einer leeren Datei mit dem Namen ```ssh```:
```
axel@tpw520:~$ touch /media/axel/tmp/ssh
```
3. Unmounten der gemounteten Partition der SD-Karte
axel@tpw520:~$ sudo umount /media/axel/tmp

Nachdem der Raspberry Pi mit der so modifizierten SD-Karte gebootet wurde, sollte der Zugriff mit SSH funktionieren.

### Hostnamen ändern
Unabhängig von dem Hostnamen, über den der Raspberry im lokalen Netzwerk erreicht werden kann, ist sein Hostname standradmäig ```raspberry``` (auch sichtbar am Prompt: ```pi@raspberrypi:~ $```). Vor allem, wenn man mehrere Raspberries im Netz hat, will man auch am Prompt sehen, auf welchem Raspberry man gerade die Befehle eingibt. Zum Ändern des Hostnames kann nachfolgender Befehl verwendet werden:
```
sudo hostname -b raspi3
```

### Zeitzone einstellen
Damit Zeitangaben zum Schalten der Geräte richtig interpretiert werden, sollte die Zeitzone des Raspberry auf die lokale Zeit gesetzt sein (nicht UTC!). Das kann mit folgendende Befehlen erreicht werden:
```
sudo /bin/bash -c "echo 'Europe/Berlin' > /etc/timezone"
sudo cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
```

### Java8 installieren
Die Installation des vom *Smart Appliance Enabler* benötigten Java8 erfolgt ganz einfach mit
```
sudo apt-get update
sudo apt-get install oracle-java8-jdk
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
* die Datei `SmartApplianceEnabler-*.jar` mit dem eigentlichen Programmcode (heruntergeladenes Release oder aus Sourcen gebaut)
* die Konfigurationsdatei `Device2EM.xml`
* die Konfigurationsdatei `Appliances.xml`
* das Startscript und die Konfigurationsdateien aus dem Verzeichnis `run` 

Dazu sollte entweder die IP-Adresse des Raspberry bekannt sein, oder der der Raspberry einen festen Hostnamen besitzen. Nachfolgend gehe ich von Letzterem aus, da er bei mir im Netz unter dem Namen `raspi` erreichbar ist.

Zunächst sollte auf dem Raspberry ein eigenes Verzeichis für diese Dateien erstellt werden (z.B. `/app`) sowie einige temporäre Verzeichnisse (das Passwort für den User *pi* ist *raspberry*, wenn Ihr das noch nicht geändert habt):
```
axel@tpw520:/data/git/SmartApplianceEnabler$ ssh pi@raspi
pi@raspi's password: 

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Sun Dec  6 19:17:12 2015
pi@raspberrypi ~ $ sudo mkdir /app
pi@raspberrypi ~ $ sudo chown pi.pi /app
pi@raspberrypi ~ $ mkdir /tmp/init.d
pi@raspberrypi ~ $ mkdir /tmp/logrotate.d
pi@raspberrypi ~ $ mkdir /tmp/default
pi@raspberrypi ~ $ exit
```
Danach muss man die genannten Dateien auf den Raspberry kopieren:
```
axel@tpw520:/data/git/SmartApplianceEnabler$ scp target/SmartApplianceEnabler-0.1.0.jar pi@raspi:/app
pi@raspi's password:
SmartApplianceEnabler-0.1.0.jar                         100%   15MB   1.4MB/s   00:11
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/Appliances.xml pi@raspi:/app
pi@raspi's password:
Appliances.xml                                          100%  590     0.6KB/s   00:00
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/Device2EM.xml  pi@raspi:/app
pi@raspi's password:
Device2EM.xml                                           100% 1288     1.3KB/s   00:00
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/etc/init.d/smartapplianceenabler pi@raspi:/tmp/init.d
pi@raspi's password: 
smartapplianceenabler                                   100% 6150     6.0KB/s   00:00    
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/etc/default/smartapplianceenabler pi@raspi:/tmp/default
pi@raspi's password: 
smartapplianceenabler                                   100% 1472     1.4KB/s   00:00    
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/etc/logrotate.d/smartapplianceenabler pi@raspi:/tmp/logrotate.d
pi@raspi's password: 
smartapplianceenabler                                   100%   98     0.1KB/s   00:00    
axel@tpw520:/data/git/SmartApplianceEnabler$
```
Jetzt müssen die Scripte und Dateien aus dem temporären Verzeichnissen an die richtige Stelle geschoben und deren Rechte gesetzt werden:
```
axel@tpw520:/data/git/SmartApplianceEnabler$ ssh pi@raspi
pi@raspi's password: 

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Sat Jan  9 07:01:59 2016 from axel-laptop-wlan.fritz.box
pi@raspberrypi ~ $ sudo mv /tmp/init.d/smartapplianceenabler /etc/init.d
pi@raspberrypi ~ $ sudo chown root.root /etc/init.d/smartapplianceenabler
pi@raspberrypi ~ $ sudo chmod 755 /etc/init.d/smartapplianceenabler
pi@raspberrypi ~ $ sudo mv /tmp/default/smartapplianceenabler /etc/default
pi@raspberrypi ~ $ sudo chown root.root /etc/default/smartapplianceenabler
pi@raspberrypi ~ $ sudo chmod 644 /etc/default/smartapplianceenabler
pi@raspberrypi ~ $ sudo mv /tmp/logrotate.d/smartapplianceenabler /etc/logrotate.d
pi@raspberrypi ~ $ sudo chown root.root /etc/logrotate.d/smartapplianceenabler
pi@raspberrypi ~ $ sudo chmod 644 /etc/logrotate.d/smartapplianceenabler
```
In der Datei `/etc/default/smartapplianceenabler` finden sich die Konfigurationseinstellungen für den Dienst *smartapplianceenabler*. Die darin befindlichen Parameter sind in der Datei selbst dokumentiert. Normalerweise sollte man die Datei unverändert lassen können.

Damit der Dienst *smartapplianceenabler* beim Systemstart ebenfalls gestartet wird, muss folgender Befehl ausgeführt werden:
```
pi@raspberrypi /etc/init.d $ sudo systemctl enable smartapplianceenabler.service
```
Nach diesen Änderungen muss der [Systemd](https://de.wikipedia.org/wiki/Systemd) dazu gebracht werden, die Service-Konfigurationen neu zu lesen:
```
pi@raspberrypi /etc/logrotate.d $ sudo systemctl daemon-reload
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
