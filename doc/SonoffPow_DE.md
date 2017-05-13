# Sonoff Pow

Der [Sonoff Pow der Firma ITead](https://www.itead.cc/sonoff-pow.html) ist ein preisgünstiger Schalter, der mit dem WLAN verbunden ist und auch den aktuellen Stromverbrauch des geschalteten Gerätes messen kann.

Der Sonoff Pow basiert auf einm ESP8266-Mikrocontroller, für den es die alternative Firmware [Sonoff-Tasmota](https://github.com/arendst/Sonoff-Tasmota) gibt, durch die der *Smart Appliance Enabler* über HTTP mit Sonoff Pow kommunizieren kann. Im Tasmota-Wiki ist beschrieben, wie zum Flashen der Firmware und zur Konfiguration von Tasmota vorgegangen werden muss. Punkte, die dabei leicht übersehen werden können, sind:
- Nach dem Flashen muss über die Web-Oberfläche der Typ des Sonoff-Gerätes (6 für Sonoff Pow) gesetzt werden, sonst wird die aktuelle Leistungsaufnahme falsch gemessen

TODO verwendete ArduinoIDE beschreiben
TODO Foto vom Aufbau

TODO SmartConfig beschreiben mit Bildern
