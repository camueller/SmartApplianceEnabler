## Status-Anzeige

Beim Öffnen der Web-Anwendung oder durch Klick auf den Menüpunkt `Status` gelangt man auf die Statusseite.

Die Statusseite zeigt den Status jedes schaltbaren Gerätes in Form einer **Ampel**, damit man den Status sofort erkennen kann:

![Statusanzeige](../pics/fe/StatusView.png)

Die Ampel dient nicht nur der Status-Anzeige, sondern auch zum **manuellen Schalten** von Geräten.

Durch einen Klick auf die grüne Lampe kann das Gerät unabhängig von konfigurierten Zeitplänen sofort eingeschaltet werden:

![Klick auf grünes Ampellicht](../pics/fe/StatusViewGreenHover.png)

Ein Klick auf die rote Lampe bewirkt das Ausschalten des Gerätes, wobei auch das Wiedereinschalten durch den *Sunny Home Manager* für den aktiven Timeframe-Interval unterbunden wird.

![Klick auf rotes Ampellicht](../pics/fe/StatusViewRedHover.png)

Damit der *Smart Appliance Enabler* dem *Sunny Home Manager* die geplante Laufzeit mitteilen kann, muss diese eingeben werden. Falls für das Gerät ein Zeitplan existiert wird das Eingabefeld vorbelegt mit dessen Wert für Laufzeit.

![Eingabe der Laufzeit bei Ampel](../pics/fe/StatusEdit.png)

Durch Klick auf die ```Starten```-Schaltfläche wird das Gerät sofort eingeschaltet.
