# Smart Appliance Enabler

## Wozu?
Der *Smart Appliance Enabler* dient dazu, beliebige Geräte (Wärmepumpe, Waschmaschine, ...) in eine **(Smart-Home-) Steuerung** zu integrieren. Dazu kann der *Smart Appliance Enabler* von der Steuerung **Schalt-Empfehlungen** entgegen nehmen und die von ihm verwalteten Geräte ein- oder ausschalten. Falls für diese Geräte individuelle, **digitale Stromzähler** verwendet werden, können diese ausgelesen werden und der Stromverbrauch an die (Smart-Home-) Steuerung gemeldet werden, um der Steuerung künftig energieeffiziente Schaltempfehlungen zu ermöglichen.

![SmartHomeEnablerSchema](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchema.png)

Damit der *Smart Appliance Enabler* in die (Smart-Home-) Steuerung integriert werden kann, muss er deren Protokoll(e) unterstützen. Obwohl die Unterstützung diverser Steuerungen konzeptionell berücksichtigt wurde, wird aktuell nur das **SEMP**-Protokoll zur Integration mit dem [Sunny Home Manager](http://www.sma.de/produkte/monitoring-control/sunny-home-manager.html) von [SMA](http://www.sma.de) unterstützt.

## Hardware
### Raspberry Pi
Der *Smart Appliance Enabler* benötigt einen [Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) als Hardware. Dieser extrem preiswerte Kleinstcomputer (ca. 40 Euro) ist perfekt zum Steuern und Messen geeignet, da er bereits [digitale Ein-/Ausgabe-Schnittstellen](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) enthält, die zum Schalten sowie zum Messen des Stromverbrauchs benötigt werden. Der aktuelle **Raspberry Pi 2 Model B** ist ist deutlich performanter als die Vorgängermodelle, was der vom *Smart Appliance Enabler* benötigten Software zugute kommt.

Für den Raspberry Pi existieren verschiedene, darauf zugeschnittene, Betriebsysteme (Images), wobei  [Raspbian Jessie](https://www.raspberrypi.org/downloads/raspbian) verwendet werden sollte, da dieses bereits die vom *Smart Appliance Enabler* benötigte Java-Runtime beinhaltet ([Installationsanleitung](http://www.pc-magazin.de/ratgeber/raspberry-pi-raspbian-einrichten-installieren-windows-mac-linux-anleitung-tutorial-2468744.html)).

An die GPIO-Pins des Raspberry können diverse Schalter und/oder Stromzähler angeschlossen werden, d.h. ein einziger Raspberry Pi kann eine Vielzahl von Geräten verwalten. Dabei darf jedoch die **Stromstärke** am 5V-Pin den Wert von 300 mA (Model B) bzw. 500mA (Model A) und am 3,3V-Pin den Wert von 50mA nicht überschreiten ([Quelle](http://elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29))!

Die Nummerierung der Pins richtet sich nach [Pi4J](http://pi4j.com/images/gpio-control-example-large.png) und weicht von der offiziellen Nummerierung ab!

### Schaltbeispiele
Die nachfolgenden Schaltbeispiele zeigen Schaltungen zum Schalten mittels **Solid-State-Relais** und zur Stromverbrauchsmessung mittels Stromzähler mit **S0-Schnittstelle**. Beides ist unabhängig voneinander, d.h. Solid-State-Relais oder Stromzähler können entfallen, falls nur geschaltet oder der Stromverbrauch ermittelt werden soll.

In den Schaltbeispielen ist der für den Stromzähler notwendige **Pull-Down-Widerstand** nicht eingezeichnet, weil dafür die auf dem Raspberry Pi vorhandenen Pull-Down-Widerstände per Software-Konfiguration aktiviert werden.

*Hinweis: Die Installation von steckerlosen 200/400V-Geräten sollte grundsätzlich durch einen autorisierten Fachbetrieb vorgenommen werden!*

#### Schaltbeispiel 1: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Schalten eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen:

![Schaltbeispiel1](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchaltung.png)

#### Schaltbeispiel 2: 400V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Schalten eines 400V-Gerätes (z.B. Heizstab) könnte wie folgt aussehen:

![Schaltbeispiel2](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/Schaltbeispiel400VMitMessung.png)

## Software
### Dank und Anerkennung
Der *Smart Appliance Enabler* verwendet intern folgende Open-Source-Software:
* [Spring Boot](http://projects.spring.io/spring-boot) für RESTful Web-Services (SEMP-Protokoll)
* [Cling](http://4thline.org/projects/cling) für UPnP (SEMP-Protokoll)

### Bauen
Bevor die Software auf dem Raspberry Pi installiert werden kann, muß diese zunächst gebaut werden.
Den dafür notwendigen Source-Code kann man mit einem Git-Client ([Git installieren](https://git-scm.com/book/de/v1/Los-geht%E2%80%99s-Git-installieren)) herunterladen
```
git clone https://github.com/camueller/SmartApplianceEnabler.git
```

oder als [ZIP-Datei](https://github.com/camueller/SmartApplianceEnabler/archive/master.zip). Letzteres muß natürlich erst noch ausgepackt werden.

Zum Bauen ist weiterhin [Maven](https://maven.apache.org) erforderlich, das gegebenenfalls noch [installiert](https://maven.apache.org/install.html) werden muss.

Um *Smart Appliance Enabler* zu bauen, ruft man Maven im Verzeichnis *SmartApplianceEnabler* zunächst nur mit dem Goal ```clean``` auf, damit die Bibliothek [J2Mod](https://sourceforge.net/projects/j2mod) im lokalen Maven-Repository installiert wird:
```
axel@tpw520:~/git/SmartApplianceEnabler$ mvn clean
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building SmartApplianceEnabler 0.1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ SmartApplianceEnabler ---
[INFO] Deleting /data/git/SmartApplianceEnabler/target
[INFO] 
[INFO] --- maven-install-plugin:2.5.2:install-file (default) @ SmartApplianceEnabler ---
[INFO] pom.xml not found in j2mod-1.06.jar
[INFO] Installing /data/git/SmartApplianceEnabler/lib/j2mod-1.06.jar to /home/axel/.m2/repository/com/ghgande/j2mod/1.06/j2mod-1.06.jar
[INFO] Installing /tmp/mvninstall9214044059867338460.pom to /home/axel/.m2/repository/com/ghgande/j2mod/1.06/j2mod-1.06.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 0.492 s
[INFO] Finished at: 2016-02-21T18:58:17+01:00
[INFO] Final Memory: 12M/304M
[INFO] ------------------------------------------------------------------------
```
Jetzt kann man den eigentlichen Build von *SmartApplianceEnabler* starten:

```
axel@tpw520:~/git/SmartApplianceEnabler$ mvn package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building SmartApplianceEnabler 0.1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ SmartApplianceEnabler ---
[INFO] Deleting /data/git/SmartApplianceEnabler/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ SmartApplianceEnabler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ SmartApplianceEnabler ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 33 source files to /data/git/SmartApplianceEnabler/target/classes
[WARNING] /data/git/SmartApplianceEnabler/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java: /data/git/SmartApplianceEnabler/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java uses unchecked or unsafe operations.
[WARNING] /data/git/SmartApplianceEnabler/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java: Recompile with -Xlint:unchecked for details.
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ SmartApplianceEnabler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /data/git/SmartApplianceEnabler/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ SmartApplianceEnabler ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.18.1:test (default-test) @ SmartApplianceEnabler ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.5:jar (default-jar) @ SmartApplianceEnabler ---
[INFO] Building jar: /data/git/SmartApplianceEnabler/target/SmartApplianceEnabler-0.1.0.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:1.3.0.RELEASE:repackage (default) @ SmartApplianceEnabler ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.348 s
[INFO] Finished at: 2015-12-24T17:49:00+01:00
[INFO] Final Memory: 28M/316M
[INFO] ------------------------------------------------------------------------
```
Nachdem man einmalig ```mvn clean``` aufgerufen hat, kann man nachfolgend immer die Goals ``` clean``` und ```build``` zusammenfassen zu ```mvn clean build```.

Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.jar` im Unterverzeichnis `target`.

## Konfiguration
Die Konfiguration besteht aus zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien:
* die Datei `Device2EM.xml` enthält Gerätebeschreibung für den EnergyManager
* die Datei `Appliances.xml` enthält die Gerätekonfiguration für den Raspberry Pi

Im Verzeichnis `example` finden sich Beispieldateien mit Kommentaren zu den einzelnen Angaben.
Diese sollen dabei helfen, die für die eigenen Geräte passenden Dateien `Device2EM.xml` und `Appliances.xml` (mit genau diesen Namen und entsprechender Groß-/Kleinschreibung!) zu erstellen.

Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden.
Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:
Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert. Bei *XSD Input* muss nur *Option 2* eingegeben werden:
* beim Prüfen von Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.1.5.xsd
* beim Prüfen von Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-1.0.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is fully valid.*. Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

### Installation
Die Installation des *Smart Appliance Enabler* besteht darin, folgende Dateien auf den Raspberry zu kopieren:
* die beim Bauen erstellte Datei `SmartApplianceEnabler-*.jar`
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

Damit das Zeitangaben zum Schalten der Geräte richtig interpretiert werden, sollte die Zeitzone des Raspberry auf die lokale Zeit gesetzt sein (nicht UTC!). Das kann mit folgendende Befehlen erreicht werden:
```
sudo /bin/bash -c "echo 'Europe/Berlin' > /etc/timezone"
sudo cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
```

## Integration in den SMA Home Manager
Der *SMA Home Manager* sollte jetzt den *Smart Appliance Enabler* finden und die von ihm verwalteten Geräte konfigurieren können. Falls das nicht so ist, sollen folgende Punkte geprüft werden:

### Erhöhung des Log-Levels
Standardmäßig ist der Log-Level auf INFO gesetzt. Zur Fehlersuche sollte dieser in der Datei `/etc/default/smartapplianceenabler` auf ALL gesetzt werden, damit in der Log-Datei alle verfügbaren Informationen in der Log-Datei `/var/log/smartapplianceenabler.log` protokolliert werden.

### Verbindung zwischen Home Manager und Smart Appliance Enabler
Home Manager auf den *Smart Appliance Enabler* müssen sich im gleichen Netz befinden!
Wenn der Log-Level mindestens auf DEBUGgesetzt wurde, kann man in der Log-Datei sehen, wenn der Home Manager auf den *Smart Appliance Enabler* zugreift:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```
## Fragen / Fehler
Bei Verdacht auf Fehler in der Software oder bei Fragen zur Verwendung des *Smart Appliance Enabler* sollte [Issue](https://github.com/camueller/SmartApplianceEnabler/issues) erstellt werden.

## Lizenz
Die Inhalte in diesem Repository sind lizensiert unter der [GNU GENERAL PUBLIC LICENSE](LICENSE.txt), falls nicht anders angegeben.
