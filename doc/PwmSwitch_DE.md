# PWM-Schalter

Ein PWM-Schalter ermöglicht die Steuerung von Verbrauchern mit **variabler Leistungsaufnahme** sofern sich diese über [PWM (Pulsweitenmodulation)](https://de.wikipedia.org/wiki/Pulsdauermodulation) steuern lassen. Die **GPIO-Anschlüsse** des Raspberry Pi können nicht nur ein- und ausgeschaltet werden, sondern auch ein PWM-Signal senden.

In den [Einstellungen für das Gerät](Appliance_DE.md) muss ein Wert für die *Min. Leistungsaufnahme* eingegeben werden. Ausserdem muss ein [Zeitplan für Überschussenergie](Schedules_DE.md) konfiguriert sein.

Die nachfolgende Abbildung zeigt die PWM-Einstellungen für ein Modellbau-Servo:

![PWM Switch](../pics/fe/PwmSwitch.png)

Bei der Konfiguration des GPIO-Anschlusses sollten unbedingt die [Hinweise zum Raspberry Pi und zur Numerierung der GPIO-Anschlüsse](Raspberry_DE.md) beachtet werden!

Für die Konfiguration des PWM-Signals sind mindestens die `PWM-Frequenz` und der `Max. Tastgrad` angeben werden. Letzteres ist das Verhältnis von Impulsdauer zur Periodendauer (in Prozent) und drückt die maximale Leistungsaufnahme aus. 

Optional kann bei Bedarf auch der `Min. Tastgrad` angegeben werden, der das Verhältnis von Impulsdauer zur Periodendauer (in Prozent) bei minimaler Leistungsaufnahme ausdrückt.

## Log
Wird ein Gerät (hier `F-00000001-000000000001-00`) mit konfiguriertem PWM-Schalter gesteuert, kann man die Steuerbefehle im [Log](Logging_DE.md) mit folgendem Befehl anzeigen:

```console
sae@raspi2:~ $ grep "c.PwmSwitch" /tmp/rolling-2022-03-27.log | grep F-00000001-000000000001-00
2022-03-27 08:00:49,798 INFO [MQTT Call: F-00000001-000000000001-00-PwmSwitch] d.a.s.c.PwmSwitch [PwmSwitch.java:144] F-00000001-000000000001-00: Setting power to 2000
2022-03-27 08:00:49,799 INFO [MQTT Call: F-00000001-000000000001-00-PwmSwitch] d.a.s.c.PwmSwitch [PwmSwitch.java:158] F-00000001-000000000001-00: Setting GPIO 17 duty cycle to 310
```

*Webmin*: In [View Logfile](Logging_DE.md#user-content-webmin-logs) gibt man hinter `Only show lines with text` ein `c.PwmSwitch` und drückt Refresh.