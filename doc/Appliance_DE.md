# Geräte

Durch Klick auf ```Neues Gerät``` wird die Konfiguration eines neuen Geräte begonnen und es öffnet sich folgende Seite:

![Neues Gerät](../pics/fe/NeuesGeraet.png)

Ein sehr wichtiges Attribut der Gerätekonfiguration ist die ```ID```. Der Aufbau der Device-ID ist in der SEMP-Spezifikation vorgegeben. Für den *Smart Appliance Enabler* bedeutet das:
* F unverändert lassen ("local scope")
* 00000001 ersetzen durch einen 8-stelligen Wert, der den eigenen Bereich definiert, z.B. das Geburtsdatum in der Form 25021964 für den 25. Februar 1964
* 000000000001 für jedes verwaltete Gerät hochzählen bzw. eine individuelle 12-stellige Zahl verwenden
* 00 unverändert lassen (sub device id)
  Die Device-IDs werden vom Sunny-Portal direkt verwendet, d.h. wenn jemand anderes bereits diese ID verwendet, kann das Gerät nicht im Sunny-Portal angelegt werden. Durch die Verwendung individueller Bestandteile wie Geburtsdatum sollte das Risiko dafür jedoch gering sein.

Das Ändern der ```ID``` führt dazu, dass der *Sunny Home Manager* das Gerät als Neugerät betrachtet.

Außer der Device-ID müssen allgemeine Angaben und Eigenschaften des Gerätes eingegeben werden.Minimal sind das folgende Angaben:

![Gerät](../pics/fe/Geraet.png)

Wenn alle erforderlichen Eingaben erfolgt sind, wird die ```Speichern```-Schaltfläche freigegeben.

Nach dem Drücken dieser Schaltfläche erscheint im Menü ein Eintrag für das angelegte Gerät. Zusätzlich erscheinen die Unterpunkte [Zähler](#zähler), [Schalter](#schalter) und [Schaltzeiten](#schaltzeiten).

Durch Klicken der ```Löschen```-Schaltfläche und Bestätigung der Löschabsicht wird das Gerät gelöscht.
