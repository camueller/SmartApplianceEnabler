# Digitale Stromzähler mit S0-Ausgang

Der Raspberry Pi verfügt über **GPIO-Anschlüsse**, an denen Impulse eines digitalen Stromzählers mit S0-Ausgang ausgewertet werden können.
Dabei sollten unbedingt die Hinweise im Kapitel zum [Raspberry Pi](Raspberry_DE.md) beachtet werden, insbesondere auch zur **Numerierung der GPIO-Anschlüsse**.

Zur Konfiguration eines S0-Stromzählers gehört die Nummer des GPIO-Anschlusses, die Konfiguration des internen Widerstands (Pull-Up / Pull-Down - siehe unten) sowie die Anzahl der Impulse pro kWh.

Ausserdem kann ein ```Messinterval``` angegeben werden für die Durchschnittsberechnung der Leistungsaufnahme.

Für die Genauigkeit des Zählers ist die Anzahl der Impulse pro kWh wichtig. Die meisten aktuellen Zähler bieten hier 1000imp. Persönlich bin ich sehr zufrieden mit den Zählern von [B+G E-Tech](http://www.bg-etech.de/), die zudem recht günstig sind.

![S0 Meter](../pics/fe/S0Meter.png)

Wird ein S0-Zähler verwendet, finden sich in der [Log-Datei](Support.md#Log) für jede empfangenen Impuls folgende Zeilen (High-Low-Reihenfolge abhängig von Pull-Up/Pull-Down):
```
2017-04-03 09:42:23,123 DEBUG [pi4j-gpio-event-executor-1358] d.a.s.a.S0ElectricityMeter [S0ElectricityMeter.java:92] F-00000001-000000000001-00: GPIO "GPIO 0" <GPIO 0> changed to HIGH
2017-04-03 09:42:23,133 DEBUG [pi4j-gpio-event-executor-1358] d.a.s.a.PulseElectricityMeter [PulseElectricityMeter.java:363] F-00000001-000000000001-00: timestamps added/removed/total: 1/361/1
2017-04-03 09:42:23,188 DEBUG [pi4j-gpio-event-executor-1358] d.a.s.a.S0ElectricityMeter [S0ElectricityMeter.java:92] F-00000001-000000000001-00: GPIO "GPIO 0" <GPIO 0> changed to LOW
```

## Schaltung
Der für den Zähler verwendete GPIO-Eingang muß auf einen definierten Grundzustand gesetzt werden, um den Einfluss von Störungen zu minimieren. Dabei unterscheidet man zwischen **Pull-Up** und **Pull-Down** (für Details siehe https://www.elektronik-kompendium.de/sites/raspberry-pi/2006051.htm).

Grundsätzlich sollte das Kabel zwischen Zähler und Raspberry Pi möglichst kurz sein (max. 20-30 cm). Sollte ein längeres Kabel notwendig sein, ist nach meinen Erfahrungen die Schaltung als **Pull-Up** weniger anfällig für Störungen.

In den nachfolgenden Schaltbeispielen ist der notwendige Widerstand für Pull-Down/Pull-Down nicht eingezeichnet, weil dafür dieser auf dem Raspberry Pi selbst vorhandenen ist und vom *Smart Appliance Enabler* per Software-Konfiguration aktiviert wird.

### Schaltbeispiel Pull-Up
Die Schaltung zum Messen des Stromverbrauchs eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen:

![Schaltbeispiel Pull-Up](../pics/SchaltungS0ZaehlerPullUp.png)

### Schaltbeispiel Pull-Down
Die Schaltung zum Messen des Stromverbrauchs eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen:

![Schaltbeispiel Pull-Down](../pics/SchaltungS0ZaehlerPullDown.jpg)
