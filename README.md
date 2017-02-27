# Smart Appliance Enabler

[![Build Status](https://travis-ci.org/pubnub/java.svg?branch=master)](https://travis-ci.org/camueller/SmartApplianceEnabler)
[![codecov.io](https://codecov.io/gh/camueller/SmartApplianceEnabler/coverage.svg)](https://codecov.io/gh/camueller/SmartApplianceEnabler)
[![Download](https://img.shields.io/badge/Download-1.0.0-brightgreen.svg)](https://github.com/camueller/SmartApplianceEnabler/releases/download/v1.0.0/SmartApplianceEnabler-1.0.0.jar)
[![License](https://img.shields.io/badge/license-GPLv2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)

## Wozu?
Der *Smart Appliance Enabler* dient dazu, beliebige Geräte (Wärmepumpe, Waschmaschine, ...) in eine **(Smart-Home-) Steuerung** zu integrieren. Dazu kann der *Smart Appliance Enabler* von der Steuerung **Schalt-Empfehlungen** entgegen nehmen und die von ihm verwalteten Geräte ein- oder ausschalten. Falls für diese Geräte individuelle, **digitale Stromzähler** verwendet werden, können diese ausgelesen werden und der Stromverbrauch an die (Smart-Home-) Steuerung gemeldet werden, um der Steuerung künftig energieeffiziente Schaltempfehlungen zu ermöglichen.

![SmartHomeEnablerSchema](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchema.png)

Damit der *Smart Appliance Enabler* in die (Smart-Home-) Steuerung integriert werden kann, muss er deren Protokoll(e) unterstützen. Obwohl die Unterstützung diverser Steuerungen konzeptionell berücksichtigt wurde, wird aktuell nur das **SEMP**-Protokoll zur Integration mit dem [Sunny Home Manager](http://www.sma.de/produkte/monitoring-control/sunny-home-manager.html) von [SMA](http://www.sma.de) unterstützt.

![SHM_Verbraucherbilanz_GuterTag](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SHM_Verbraucherbilanz_GuterTag.png)

## Hardware

*Hinweis: Die Installation von steckerlosen 200/400V-Geräten sollte grundsätzlich durch einen autorisierten Fachbetrieb vorgenommen werden!*

![Schaltschrank](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/schaltschrank.jpg)

### [Raspberry Pi](doc/Raspberry_DE.md)
Der *Smart Appliance Enabler* benötigt einen [Raspberry Pi als Hardware](doc/Raspberry_DE.md). 

### Stromzähler

Aktuell unterstützt der *Smart Appliance Enabler* folgende Möglichkeiten, den Stromverbrauch eines Gerätes zu messen, um ihn an die (Smart-Home-) Steuerung zu melden:

- [S0](doc/S0Meter_DE.md)
- [Modbus](doc/ModbusMeter_DE.md)
- [HTTP](doc/HttpMeter_DE.md)
- [WLAN-Stromzähler selbst gebaut](doc/WifiS0PulseForwarder_DE.md)

### Schalter

Zum Ein-/Ausschalten eines Gerätes unterstützt der *Smart Appliance Enabler* derzeit folgende Möglichkeiten:

- [Solid-State-Relais](doc/SolidStateRelais_DE.md)
- [Modbus](doc/ModbusSwitch_DE.md)
- [HTTP](doc/HttpSwitch_DE.md)

Alle aufgeführten Schalter können mit einer [Anlaufstromerkennung](doc/Anlaufstromerkennung_DE.md) verwendet werden, um die Programmierung des Gerätes zu ermöglichen.

## Software
Zur Verwendung des *Smart Appliance Enabler* zusammen mit dem *SMA Sunny Home Manager* sind mindesten die in den 3 nachfolgenden Kapiteln (_Konfiguration_, _Installation_ und _Integration_) genannten Schritte erforderlich.

### [Konfiguration](doc/Configuration_DE.md)
Damit der *Smart Appliance Enabler* die Geräte steuern und deren Stromverbrauch messen kann, ist zusätzlich zur Installation der Software die Erstellung einer [Konfiguration](doc/Configuration_DE.md) erforderlich.

### [Installation](doc/Installation_DE.md)
Der einfachste Weg zu einem lauffähigen *Smart Appliance Enabler* besteht in der [Installation](doc/Installation_DE.md) einer releasten Version.

### Integration in den SMA Sunny Home Manager
Damit der Sunny Home Manager das Gerät steuern und seinen Verbrauch messen kann, muss es im [Sunny Home Manager konfiguriert](doc/SunnyHomeMangerKonfiguration_DE.md) werden.

### [Bauen aus Sourcen](doc/Build_DE.md)
Zur Nutzung von Features, die noch nicht in der releasten Version enthalten sind, muss der *Smart Appliance Enabler* [aus Sourcen gebaut werden](doc/Build_DE.md).

### Dank und Anerkennung
Der *Smart Appliance Enabler* verwendet intern folgende Open-Source-Software:
* [Pi4J](http://pi4j.com) für den Zugriff auf die GPIO-Ports des Raspberry
* [Spring Boot](http://projects.spring.io/spring-boot) für RESTful Web-Services (SEMP-Protokoll)
* [Cling](http://4thline.org/projects/cling) für UPnP (SEMP-Protokoll)

## [Fragen / Probleme](doc/Troubleshooting_DE.md)
Durch die Vielzahl von Hard- und Softwarekomponenten können bei der Verwendung des *Smart Appliance Enabler* naturgemäß Fragen oder Probleme auftreten. In diesem Fall sollten [diese Hinweise](doc/Troubleshooting_DE.md) beachtet werden. 

## Lizenz
Die Inhalte in diesem Repository sind lizensiert unter der [GNU GENERAL PUBLIC LICENSE](LICENSE.txt), falls nicht anders angegeben.
