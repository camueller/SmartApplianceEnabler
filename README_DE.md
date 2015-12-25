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

### Schaltbeispiel 1: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Schalten eines Pumpe durch ein Solid-State-Relais inklusive der Messung des Stromverbrauchs könnte wie folgt aussehen:

![SmartHomeEnablerSchaltung](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchaltung.png)

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

Um *Smart Appliance Enabler* zu bauen, ruft man Maven im Verzeichnis *SmartApplianceEnabler* wie folgt auf:
```
axel@tpw520:~/git/SmartApplianceEnabler$ mvn clean package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building SmartApplianceEnabler 0.1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ SmartApplianceEnabler ---
[INFO] Deleting /data/git/SmartApplianceEnabler.old/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ SmartApplianceEnabler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ SmartApplianceEnabler ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 33 source files to /data/git/SmartApplianceEnabler.old/target/classes
[WARNING] /data/git/SmartApplianceEnabler.old/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java: /data/git/SmartApplianceEnabler.old/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java uses unchecked or unsafe operations.
[WARNING] /data/git/SmartApplianceEnabler.old/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java: Recompile with -Xlint:unchecked for details.
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ SmartApplianceEnabler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /data/git/SmartApplianceEnabler.old/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ SmartApplianceEnabler ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.18.1:test (default-test) @ SmartApplianceEnabler ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.5:jar (default-jar) @ SmartApplianceEnabler ---
[INFO] Building jar: /data/git/SmartApplianceEnabler.old/target/SmartApplianceEnabler-0.1.0.jar
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
Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.jar` im Unterverzeichnis `target`.

### Installation
Dateien mit ssh auf rsaspi schieben

## Konfiguration
### Geräte
Device2EM.xml - siehe Template

### Gerätesteueung
Appliances.xml - siehe Template

## Fragen / Fehler
Bei Verdacht auf Fehler in der Software oder bei Fragen zur Verwendung des *Smart Appliance Enabler* sollte [Issue](https://github.com/camueller/SmartApplianceEnabler/issues) erstellt werden.

## Lizenz
Die Inhalte in diesem Repository sind lizensiert unter der [GNU GENERAL PUBLIC LICENSE](LICENSE.txt), falls nicht anders angegeben.
