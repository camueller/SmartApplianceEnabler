# Smart Appliance Enabler

[![Build Status](https://travis-ci.org/pubnub/java.svg?branch=master)](https://travis-ci.org/camueller/SmartApplianceEnabler)
[![codecov.io](https://codecov.io/gh/camueller/SmartApplianceEnabler/coverage.svg)](https://codecov.io/gh/camueller/SmartApplianceEnabler)
[![Download](https://img.shields.io/badge/Download-1.0.0-brightgreen.svg)](https://github.com/camueller/SmartApplianceEnabler/releases/download/v1.0.0/SmartApplianceEnabler-1.0.0.jar)
[![License](https://img.shields.io/badge/license-GPLv2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)

## Wozu?
Der *Smart Appliance Enabler* dient dazu, beliebige Geräte (Wärmepumpe, Waschmaschine, ...) in eine **(Smart-Home-) Steuerung** zu integrieren. Dazu kann der *Smart Appliance Enabler* von der Steuerung **Schalt-Empfehlungen** entgegen nehmen und die von ihm verwalteten Geräte ein- oder ausschalten. Falls für diese Geräte individuelle, **digitale Stromzähler** verwendet werden, können diese ausgelesen werden und der Stromverbrauch an die (Smart-Home-) Steuerung gemeldet werden, um der Steuerung künftig energieeffiziente Schaltempfehlungen zu ermöglichen.

![SmartHomeEnablerSchema](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchema.png)

Damit der *Smart Appliance Enabler* in die (Smart-Home-) Steuerung integriert werden kann, muss er deren Protokoll(e) unterstützen. Obwohl die Unterstützung diverser Steuerungen konzeptionell berücksichtigt wurde, wird aktuell nur das **SEMP**-Protokoll zur Integration mit dem [Sunny Home Manager](http://www.sma.de/produkte/monitoring-control/sunny-home-manager.html) von [SMA](http://www.sma.de) unterstützt.

## Hardware

*Hinweis: Die Installation von steckerlosen 200/400V-Geräten sollte grundsätzlich durch einen autorisierten Fachbetrieb vorgenommen werden!*

### Raspberry Pi
Der *Smart Appliance Enabler* benötigt einen [Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) als Hardware. Dieser extrem preiswerte Kleinstcomputer (ca. 40 Euro) ist perfekt zum Steuern und Messen geeignet, da er bereits [digitale Ein-/Ausgabe-Schnittstellen](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) enthält, die zum Schalten sowie zum Messen des Stromverbrauchs benötigt werden. Hinsichtlich der Leistung empfiehlt es sich, einen **Raspberry Pi 2 Model B** oder neuer zu verwenden.

Für den Raspberry Pi existieren verschiedene, darauf zugeschnittene, Betriebsysteme (Images), wobei  [Raspbian Jessie](https://www.raspberrypi.org/downloads/raspbian) verwendet werden sollte, da dieses bereits die vom *Smart Appliance Enabler* benötigte Java-Runtime beinhaltet ([Installationsanleitung](http://www.pc-magazin.de/ratgeber/raspberry-pi-raspbian-einrichten-installieren-windows-mac-linux-anleitung-tutorial-2468744.html)).

An die GPIO-Pins des Raspberry können diverse Schalter und/oder Stromzähler angeschlossen werden, d.h. ein einziger Raspberry Pi kann eine Vielzahl von Geräten verwalten. Dabei darf jedoch die **Stromstärke** am 5V-Pin den Wert von 300 mA (Model B) bzw. 500mA (Model A) und am 3,3V-Pin den Wert von 50mA nicht überschreiten ([Quelle](http://elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29))!

Die Nummerierung der Pins richtet sich nach [Pi4J](http://pi4j.com/images/gpio-control-example-large.png) und weicht von der offiziellen Nummerierung ab!

TODO Bauanleitung mit Hutschienenhalter hinzufügen

### Stromzähler

Aktuell unterstützt der *Smart Appliance Enabler* folgende Möglichkeiten, den Stromverbrauch eines Gerätes zu messen, um ihn an die (Smart-Home-) Steuerung zu melden:

- [S0](doc/S0Meter_DE.md)
- [Modbus](doc/ModbusMeter_DE.md)
- HTTP
- [WLAN-Stromzähler selbst gebaut](doc/WifiS0PulseForwarder_DE.md)

### Schalter

Zum Ein-/Ausschalten eines Gerätes unterstützt der *Smart Appliance Enabler* derzeit folgende Möglichkeiten:

- [Solid-State-Relais](doc/SolidStateRelais_DE.md)
- S0
- Modbus
- HTTP

## Software
Zur Verwendung des *Smart Appliance Enabler* zusammen mit dem *SMA Sunny Home Manager* sind mindesten die in den 3 nachfolgenden Kapiteln (_Konfiguration_, _Installation_ und _Integration_) genannten Schritte erforderlich.

### [Konfiguration](doc/Configuration_DE.md)
Damit der *Smart Appliance Enabler* die Geräte steuern und deren Stromverbrauch messen kann, ist zusätzlich zur Installation der Software die Erstellung einer [Konfiguration](doc/Configuration_DE.md) erforderlich.

### [Installation](doc/Installation_DE.md)
Der einfachste Weg zu einem lauffähigen *Smart Appliance Enabler* besteht in der [Installation](doc/Installation_DE.md) einer releasten Version.

### Integration in den SMA Sunny Home Manager
TODO

### [Bauen aus Sourcen](doc/Build_DE.md)
Zur Nutzung von Features, die noch nicht in der releasten Version enthalten sind, muss der *Smart Appliance Enabler* [aus Sourcen gebaut werden](doc/Build_DE.md).

### Dank und Anerkennung
Der *Smart Appliance Enabler* verwendet intern folgende Open-Source-Software:
* [Spring Boot](http://projects.spring.io/spring-boot) für RESTful Web-Services (SEMP-Protokoll)
* [Cling](http://4thline.org/projects/cling) für UPnP (SEMP-Protokoll)

## Fragen / Fehler
Bei Verdacht auf Fehler in der Software oder bei Fragen zur Verwendung des *Smart Appliance Enabler* sollte [Issue](https://github.com/camueller/SmartApplianceEnabler/issues) erstellt werden.

## Lizenz
Die Inhalte in diesem Repository sind lizensiert unter der [GNU GENERAL PUBLIC LICENSE](LICENSE.txt), falls nicht anders angegeben.
