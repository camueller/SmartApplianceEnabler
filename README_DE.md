# Smart Appliance Enabler

## Wozu?
Der *Smart Appliance Enabler* dient dazu, beliebige Geräte (Wärmepumpe, Waschmaschine, ...) in eine **(Smart-Home-) Steuerung** zu integrieren. Dazu kann der *Smart Appliance Enabler* von der Steuerung **Schalt-Empfehlungen** entgegen nehmen und die von ihm verwalteten Geräte ein- oder ausschalten. Falls für diese Geräte individuelle, **digitale Stromzähler** verwendet werden, können diese ausgelesen werden und der Stromverbrauch an die (Smart-Home-) Steuerung gemeldet werden.

TODO: Bild mit Symbolen für Gateway, Steuerung, Waschmaschine, Wärmepumpe

Damit der *Smart Appliance Enabler* in die (Smart-Home-) Steuerung integriert werden kann, muss er deren Protokoll(e) unterstützen. Obwohl die Unterstützung diverser Steuerungen konzeptionell berücksichtigt wurde, wird aktuell nur das **SEMP**-Protokoll zur Integration mit dem [Sunny Home Manager](http://www.sma.de/produkte/monitoring-control/sunny-home-manager.html) von [SMA](http://www.sma.de) unterstützt.

## Aufbau
Der *Smart Appliance Enabler* besteht aus der hier bei Github befindlichen Software und benötigt einen [Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) als Hardware. Dieser extrem preiswerte Kleinstcomputer ist perfekt zum Steuern und Messen geeignet, da er bereits [digitale Ein-/Ausgabe-Schnittstellen](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) enthält, die zum Schalten sowie zum Messen des Stromverbrauchs benötigt werden.
