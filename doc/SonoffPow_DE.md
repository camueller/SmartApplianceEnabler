# Sonoff Pow

Der [Sonoff Pow der Firma ITead](https://www.itead.cc/sonoff-pow.html) ist ein preisgünstiger Schalter, der mit dem WLAN verbunden ist und auch den aktuellen Stromverbrauch des geschalteten Gerätes messen kann.

Die Nutzung mit dem *Smart Appliance Enabler* ist nur möglich, wenn der Adapter mit der [Tasmota-Firmware](doc/Tasmota_DE.md) geflasht wird.

Im Tasmota-Wiki ist beschrieben, wie zum Flashen der Firmware und zur Konfiguration von Tasmota vorgegangen werden muss. Punkte, die dabei leicht übersehen werden können, sind:
- Nach dem Flashen muss über die Web-Oberfläche der Typ des Sonoff-Gerätes (6 für Sonoff Pow) gesetzt werden, sonst wird die aktuelle Leistungsaufnahme falsch gemessen
- Vor _jedem_ Flashen nach Änderungen an user_config.h muss der Wert für CFG_HOLDER geändert werden, sonst bleiben die Änderungen ohne Wirkung!

Ich konnte das Flashen der Tasmota-Firmware mit der Arduino-IDE 1.6.11 erfolgreich durchführen.

## WLAN konfigurieren
Zur Konfiguration des WLAN wird ein Android-Gerät benötigt, auf dem die App [ESP8266 SmartConfig](https://play.google.com/store/apps/details?id=com.cmmakerclub.iot.esptouch) installiert und das in dem gewünschten WLAN eingebucht ist. Nach dem Starten der App _ESP8266 SmartConfig_ sollte die SSID des WLAN angezeigt werden. In das Feld _Passwort_ muss das Passwort dieses WLANs eingegeben werden.

Am Sonoff Pow muss der Taster dreimal hintereinander kurz gedrück werden, woraufhin die blaue LED schnell blinkt und damit anzeigt, dass der *Wifi SmartConfig-Modus* aktiv ist.

In der App _ESP8266 SmartConfig_ sollte jetzt die Schaltfläche _Confirm_ betätigt werden. Die blaue LED sollte dann etwas länger ausgehen, einmal etwas länger angehen und dann noch zweimal kurz blinken, bevor sie dauerhaft ausgeht. Danach sollte man auf seinem Router nachsehen, um die IP-Adresse herauszufinden, unter dem der Sonoff Pow im WLAN erreichbar ist.
