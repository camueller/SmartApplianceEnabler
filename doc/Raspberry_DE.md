# Raspberry Pi
Der *Smart Appliance Enabler* benötigt einen **[Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) 2 Model B (oder neuer) oder Pi Zero** als Hardware. Dieser extrem preiswerte Kleinstcomputer (ca. 40 Euro) ist perfekt zum Steuern und Messen geeignet, da er bereits [digitale Ein-/Ausgabe-Schnittstellen](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) enthält, die zum Schalten sowie zum Messen des Stromverbrauchs benötigt werden.

An die GPIO-Pins des Raspberry können diverse Schalter und/oder Stromzähler angeschlossen werden, d.h. ein einziger Raspberry Pi kann eine Vielzahl von Geräten verwalten. Dabei darf jedoch die **Stromstärke** am 5V-Pin den Wert von 300 mA (Model B) bzw. 500mA (Model A) und am 3,3V-Pin den Wert von 50mA nicht überschreiten ([Quelle](http://elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29))!

Der *Smart Appliance Enabler* verwendet intern [Pi4J](https://pi4j.com) um auf die GPIO-Pins zuzugreifen. Diese Bibliothek verwendet eine eigene Numerierung der GPIO-Pins, die ungleich der Pin-Nummer ist! Demzufolge muss im *Smart Appliance Enabler* diese Nummer als *GPIO-Anschluss* eingegeben werden.

Die GPIO-Nummern sind abhängig vom konkreten Raspberry Pi-Model. Für alle unterstützten Modelle findet sich auf [Pi4J-Homepage](https://pi4j.com) links unter **Pin Numbering** eine Grafik mit dem Mapping der Pin-Nummer auf die vom *Smart Appliance Enabler* benötigte GPIO-Nummer.  
