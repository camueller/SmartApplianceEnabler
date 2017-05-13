# Sonoff Pow

Der [Sonoff Pow der Firma ITead](https://www.itead.cc/sonoff-pow.html) ist ein preisgünstiger Schalter, der mit dem WLAN verbunden ist und auch den aktuellen Stromverbrauch des geschalteten Gerätes messen kann.

Der Sonoff Pow basiert auf einm ESP8266-Mikrocontroller, für den es die alternative Firmware [Sonoff-Tasmota](https://github.com/arendst/Sonoff-Tasmota) gibt, durch die der *Smart Appliance Enabler* über HTTP mit Sonoff Pow kommunizieren kann. Im Tasmota-Wiki ist beschrieben, wie zum Flashen der Firmware und zur Konfiguration von Tasmota vorgegangen werden muss. Punkte, die dabei leicht übersehen werden können, sind:
- Nach dem Flashen muss über die Web-Oberfläche der Typ des Sonoff-Gerätes (6 für Sonoff Pow) gesetzt werden, sonst wird die aktuelle Leistungsaufnahme falsch gemessen
- Vor _jedem_ Flashen nach Änderungen an user_config.h muss der Wert für CFG_HOLDER geändert werden, sonst bleiben die Änderungen ohne Wirkung!

TODO verwendete ArduinoIDE beschreiben
TODO Foto vom Aufbau

TODO SmartConfig beschreiben mit Bildern
