# Zeitpläne

Der *Sunny Home Manager* wird nur dann Geräte einplanen und einschalten, wenn eine Anforderung vorliegt. Neben Ad-Hoc-Anforderungen über die [Ampel](Status_DE.md) sind Zeitpläne das zentrale Element für (potentiell) regelmäßige Anforderungen (z.B. für Gschirrspüler, Waschmaschine, ...)

Falls ein Schalter-Typ konfiguriert wurde, der die Steuerung des Gerätes erlaubt, können Zeitpläne konfiguriert werden.

Ein Zeitplan hat einen Zeitrahmen, auf den er sich bezieht:
- `Tagesplan`: bezieht sich auf einen/alle Wochentage
- `Mehrtagesplan`: bezieht sich auf eine Dauer länger als 24 Stunden aber höchstens eine Woche

Für einen Tagesplan kann angegeben werden, dass dieser an Feiertagen gelten soll.
Dieser hat Vorrang vor anderen Tagesplänen, die entsprechend des Wochentages gelten würden. Voraussetzung dafür ist, dass das [Feiertagshandling in der Konfiguration aktiviert](Settings_DE.md#Feiertage) wurde.

Neben dem Zeitrahmen hat ein Zeitplan auch eine `Anforderungsart`, die (außer für Wallboxen) immer `Laufzeit` ist. Dabei wird über die `Startzeit` und die `Endzeit` ein Zeitfenster definiert, innerhalb dessen der *Sunny Home Manager* die `maximale Laufzeit` unabhängig vom Vorhandensein von PV-Strom sicherstellen muss. Wird zusätzlich die (optionale) `minimale Laufzeit` angegeben, wird der *Sunny Home Manager* nur diese Laufzeit sicherstellen, aber bei Vorhandensein von *Überschussenenergie* die Laufzeit bis zur `maximalen Laufzeit` erweitern. Im Extremfall führt das Setzen einer `minimalen Laufzeit` von 0 dazu, dass das Gerät ausschliesslich mit Überschussenergie betrieben wird. Wenn diese nicht vorhanden ist, wird das Gerät nicht eingeschaltet.

Es ist möglich für ein Gerät auch mehrere Zeitpläne anzulegen. Hierbei muss darauf geachtet werden, dass die Zeitpläne **nicht überlappen**, zum Beispiel muss "Zeitplan 1" um 13:59 Uhr enden, wenn "Zeitplan 2" um 14:00 Uhr beginnen soll.

Beispiel für einen Tagesplan:

![Schaltzeiten Tagesplan](../pics/fe/SchaltzeitenTagesplanLaufzeit.png)

Beispiel für einen Mehrtagesplan:

![Schaltzeiten Mehrtagesplan](../pics/fe/SchaltzeitenMehrtagesplanLaufzeit.png)

Der *Smart Appliance Enabler* meldet dem Sunny Home Manager den Geräte-Laufzeitbedarf für die nächsten 48 Stunden, damit er auf dieser Basis optimal planen kann.

## Besonderheiten für Wallboxen

Bei Wallboxen existieren als **Anforderungsart** zwei Optionen:

### Laden bis SOC
Mit der Anforderungsart `Laden bis SOC` wird genau die Energiemenge angefordert, die notwendig ist, um einen bestimmten SOC zu erreichen. Zur Berechnung dieser Energiemenge wird die Batteriekapazität und der SOC des Fahrzeugs bei Ladebeginn herangezogen. Für letzteres ist es notwendig, dass der [SOC des Fahrzeugs via Script](soc/SOC_DE.md) abgefragt werden kann.
![Anforderungsart SOC](../pics/fe/SchaltzeitenTagesplanSOC.png)

### Laden einer Energiemenge

Mit der Anforderungsart `Energiemenge` kann eine bestimmte Energiemenge (in Wh) angefordert werden. Normalerweise wird nur die `max. Ernergiemenge` angegeben, die auf jeden Fall geladen werden soll.

Optional kann für die `min. Energiemenge` ein kleinerer Wert angegeben werden. Falls er angegeben ist, wird nur dieser Wert auf jeden Fall geladen und die darüber hinausgehende Energiemenge bis zur `max. Ernergiemenge` nur dann, wenn **Überschussenergie** verfügbar ist.
![Anforderungsart Energiemenge](../pics/fe/SchaltzeitenTagesplanEnergiemenge.png)

Siehe auch: [Allgemeine Hinweise zur Konfiguration](Configuration_DE.md)
