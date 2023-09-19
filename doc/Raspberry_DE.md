# Raspberry Pi
Der *Smart Appliance Enabler* benötigt einen **[Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) 2 Model B (oder neuer)** als Hardware. Dieser extrem preiswerte Kleinstcomputer (ca. 40 Euro) ist perfekt zum Steuern und Messen geeignet, da er bereits [digitale Ein-/Ausgabe-Schnittstellen](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) enthält, die zum Schalten sowie zum Messen des Stromverbrauchs benötigt werden. Grundsätzlich kann man den *Smart Appliance Enabler* auch auf einem Raspberry Pi Zero betreiben, aber diese Platform ist offiziell nicht unterstützt und man sollte dann keinen Support bei Problemen erwarten.

An die GPIO-Pins des Raspberry können diverse Schalter und/oder Stromzähler angeschlossen werden, d.h. ein einziger Raspberry Pi kann eine Vielzahl von Geräten verwalten. Dabei darf jedoch die **Stromstärke** am 5V-Pin den Wert von 300 mA (Model B) bzw. 500mA (Model A) und am 3,3V-Pin den Wert von 50mA nicht überschreiten ([Quelle](http://elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29))!

Um auf die GPIO-Pins zuzugreifen verwendet der *Smart Appliance Enabler* intern die Java-Bibliothek [pigpioj](https://github.com/mattjlewis/pigpioj). Dabei wird der Socket-Mode der Bibliothek verwendet, damit der *Smart Appliance Enabler* auch ohne root-Privilegien auf die GPIO-Pins zugreifen kann. 

[pigpioj](https://github.com/mattjlewis/pigpioj) wiederum verwendet [pigpio](https://abyz.me.uk/rpi/pigpio/). Von dieser Bibliothek gibt es auch einen Daemon `pigpiod`, auf den [pigpioj](https://github.com/mattjlewis/pigpioj) im Socket-Mode zugreift. Voraussetzung dafür ist, dass [pigpiod installiert wurde](InstallationManual_DE.md)!

Für die [Numerierung der Anschlusse wird dabei nicht die Pin-Nummer auf dem Raspberry Pi verwendet, sondern die GPIO-Nummer des Broadcom-Prozessors](https://raspberrypi.stackexchange.com/questions/12966/what-is-the-difference-between-board-and-bcm-for-gpio-pin-numbering).

Demzufolge muss im *Smart Appliance Enabler* diese Nummer als *GPIO-Anschluss* eingegeben werden.

![Raspberry Pi Pinout](../pics/raspberry-pi-15b.jpg)

## Ausschalten / Neu starten

Wie jeder Computer mit einem Speichermedium mit Schreibzugriff sollte der Raspberry Pi nicht einfach von der Stromquelle getrennt werden, um ihn auszuschalten oder neu zu starten. Dabei kann das Dateisystem oder die SD-Karte kaputt gehen.

Das Herunterfahren mit nachfolgendem Ausschalten erfolgt mit dem Befehl:

```bash
$ sudo shutdown now
```

Das Herunterfahren mit nachfolgendem Neustart erfolgt mit dem Befehl:

```bash
$ sudo shutdown -r now
```

*Webmin*: Wenn man im Seiten-Menü den Punkt `System` wählt und den Unterpunkt `Bootup and Shutdown` anklickt, finden sich ganz unten auf der Seite die Schaltflächen für `Reboot System` und `Shutdown System`.
