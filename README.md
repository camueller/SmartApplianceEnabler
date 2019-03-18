<p align="center">
  <img src="https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/logo.png">
  <h3 align="center">Smart Appliance Enabler</h3>
  <p align="center">
    <a href="https://travis-ci.org/camueller/SmartApplianceEnabler">
      <img src="https://travis-ci.org/camueller/SmartApplianceEnabler.svg?branch=master">
    </a>
    <a href="https://codecov.io/gh/camueller/SmartApplianceEnabler">
      <img src="https://codecov.io/gh/camueller/SmartApplianceEnabler/coverage.svg">
    </a>
    <a href="https://camueller.github.io/SmartApplianceEnabler-web-coverage">
      <img src="https://camueller.github.io/SmartApplianceEnabler-web-coverage/SmartApplianceEnabler-web-coverage.svg">
    </a>
    <a href="https://github.com/camueller/SmartApplianceEnabler/releases/download/v1.2.1/SmartApplianceEnabler-1.2.1.war">
      <img src="https://img.shields.io/badge/Download-1.2.1-brightgreen.svg">
    </a>
    <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.html">
      <img src="https://img.shields.io/badge/license-GPLv2-blue.svg">
    </a>
  </p>
</p>

![Donate](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/donate.jpeg)

**Seit mehreren Jahren habe ich einen Grossteil meiner Freizeit in Entwicklung, Dokumentation und Support dieses Open-Source-Projektes gesteckt. Ich habe mich bewusst dazu entschieden, diese Software kostenfrei zur Verfügung zu stellen und kontinuierlich um Features zu erweitern, die für möglichst viele Nutzer von Interesse sein könnten. Durch die Nutzung dieser Software lassen sich die Stromkosten signifikant senken, indem die Nutzung des selbst erzeugten Stroms maximiert wird. Ich würde es als Anerkennung meiner Arbeit betrachten, wenn ein Teil dieser Ersparnis als Beitrag zur Förderung dieses Projekts verwendet werden würde. Das geht ganz einfach per [Paypal](https://paypal.me/CarlAxelMueller) oder als klassische Banküberweisung (Kontoinhaber: Axel Müller, IBAN: DE83 5185 0079 1140 0764 37, BIC: HELADEF1FRI, Verwendungszweck: Förderbeitrag Smart Appliance Enabler)**

## Wozu?
Der *Smart Appliance Enabler* dient dazu, beliebige Geräte ([Ladegeräte/Wallboxen für E-Autos](doc/EVCharger_DE.md), Wärmepumpe, Waschmaschine, Geschirrspüler, ...) mit dem [Sunny Home Manager (SHM)](http://www.sma.de/produkte/monitoring-control/sunny-home-manager.html) von [SMA](http://www.sma.de) zu integrieren.

![SmartHomeEnablerSchema](pics/SmartApplianceEnabler.png)

Dazu meldet der *Smart Appliance Enabler* dem SHM **Bedarfsanforderungen** dieser Geräte um diesem eine optimale Planung des Eigenverbrauchs zu ermöglich. Entsprechend dieser Planung empfängt der *Smart Appliance Enabler* **Schaltbefehle**, die er an die von ihm verwalteten Geräte weiterleitet. Falls für diese Geräte individuelle, **digitale Stromzähler** verwendet werden, können diese ausgelesen werden und der Stromverbrauch an den SHM gemeldet werden, um diesen beim Lernen der Verbrauchscharakteristik zu unterstüzen und Verbräuche zu visualieren.

Ein Wintertag mit hohem Energiebedarf für die Heizungswärmepumpe und zusätzlichen Verbrauchern in Fom von Waschmaschine, Geschirrspüler und Herd kann so aussehen:

![SHM_Verbraucherbilanz_GuterTag](pics/SHM_Verbraucherbilanz_GuterTag.png)

## Hardware

### [Raspberry Pi](doc/Raspberry_DE.md)
Der *Smart Appliance Enabler* benötigt einen [Raspberry Pi als Hardware](doc/Raspberry_DE.md). 

### Stromzähler

Aktuell unterstützt der *Smart Appliance Enabler* folgende Möglichkeiten, den Stromverbrauch eines Gerätes zu messen, um ihn an die (Smart-Home-) Steuerung zu melden:

| Protokolle    | Produkte      |
| ------------- | ------------- |
| [S0](doc/S0Meter_DE.md)  | [WLAN-Stromzähler selbst gebaut](doc/WifiS0PulseForwarder_DE.md) |
| [Modbus](doc/ModbusMeter_DE.md)  |  |
| [HTTP](doc/HttpMeter_DE.md)      | [Sonoff Pow](doc/SonoffPow_DE.md) <br> [Edimax SP-2101W](doc/EdimaxSP2101W_DE.md) <br> [Shelly 4 Pro](doc/Shelly4Pro_DE.md)|

### Schalter

Zum Ein-/Ausschalten eines Gerätes unterstützt der *Smart Appliance Enabler* derzeit folgende Möglichkeiten:

| Protokolle    | Produkte      |
| ------------- | ------------- |
| GPIO          | [Solid-State-Relais](doc/SolidStateRelais_DE.md) |
| [Modbus](doc/ModbusSwitch_DE.md) | |
| [HTTP](doc/HttpSwitch_DE.md) | [Sonoff Pow](doc/SonoffPow_DE.md) <br> [Edimax SP-2101W](doc/EdimaxSP2101W_DE.md)  <br> [Shelly 4 Pro](doc/Shelly4Pro_DE.md)|

Alle aufgeführten Schalter können mit einer [Anlaufstromerkennung](doc/Anlaufstromerkennung_DE.md) verwendet werden, um die Programmierung des Gerätes zu ermöglichen.

Ein Schalter, der neben dem Schaltzustand auch eine vorgegebene Leistungsaufnahme ermöglicht, existiert für [Ladegeräte von Elektro-Autos](EVCHarger_DE.md).

### Montage
Für den reibungslosen Einsatz des *Smart Appliance Enabler* sollten die [Montage-Hinweise](Montage_DE.md) beachtet werden.

## Software
Zur Verwendung des *Smart Appliance Enabler* zusammen mit dem *SMA Sunny Home Manager* sind mindesten die in den 3 nachfolgenden Kapiteln (_Installation_,_Konfiguration_ und _Integration_) genannten Schritte erforderlich.

### [Installation](doc/Installation_DE.md)
Der einfachste Weg zu einem lauffähigen *Smart Appliance Enabler* besteht in der [Installation](doc/Installation_DE.md) einer releasten Version.

### [Konfiguration](doc/Configuration_DE.md)
Damit der *Smart Appliance Enabler* die Geräte steuern und deren Stromverbrauch messen kann, ist zusätzlich zur Installation der Software die Erstellung einer [Konfiguration](doc/Configuration_DE.md) erforderlich.

### Integration in den SMA Sunny Home Manager
Der Sunny Home Manager kann die Geräte erst dann steuern und deren Verbrauch messen, nachdem sie auch im [Sunny Home Manager konfiguriert](doc/SunnyHomeMangerKonfiguration_DE.md) worden sind.

### [Bauen aus Sourcen](doc/Build_DE.md)
Zur Nutzung von Features, die noch nicht in der releasten Version enthalten sind, muss der *Smart Appliance Enabler* [aus Sourcen gebaut werden](doc/Build_DE.md).

### Dank und Anerkennung
Der *Smart Appliance Enabler* verwendet intern folgende Open-Source-Software:
* [Pi4J](http://pi4j.com) für den Zugriff auf die GPIO-Ports des Raspberry
* [Spring Boot](http://projects.spring.io/spring-boot) für RESTful Web-Services (SEMP-Protokoll)
* [Angular](https://angular.io) für das Web-Frontend
* [Semantic UI](https://semantic-ui.com/) für das Web-Frontend
* [Cling](http://4thline.org/projects/cling) für UPnP (SEMP-Protokoll)

## [Fragen / Probleme](doc/Troubleshooting_DE.md)
Durch die Vielzahl von Hard- und Softwarekomponenten können bei der Verwendung des *Smart Appliance Enabler* naturgemäß Fragen oder Probleme auftreten. In diesem Fall sollten [diese Hinweise](doc/Troubleshooting_DE.md) beachtet werden. 

## Lizenz
Die Inhalte in diesem Repository sind lizensiert unter der [GNU GENERAL PUBLIC LICENSE](LICENSE.txt), falls nicht anders angegeben.
