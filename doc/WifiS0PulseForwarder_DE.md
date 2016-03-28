# Stromzähler mit WLAN-Anbindung an Smart Appliance Enabler

## Wozu?
Nicht immer ist eine dedizierte Leitung vom Sicherungskasten zu dem Verbraucher vorhanden, dessen Verbrauch gemessen werden soll.
In diesen Fällen ist das Hinzufügen eines Zähler im Sicherungskasten also keine Option.
Damit der Zähler nur den Verbrauch des zu messenden Verbrauchers misst, muss der Zähler direkt an der Steckdose des Verbrauchers installiert werden.
Für diesen Zweck gibt es die Bluetooth-Steckdosen von SMA oder auch die Circles von Plugwise.
Leider verwenden zumindest letztere eine Funktechnik, welche a normalen Geschossdecken scheitert, so dass die Verwendung auf mehreren Etagen nicht möglich ist.
Die WLAN-Funktechnik hat weniger Probleme damit, aber leider gibt es keine kommerziellen WLAN-basierten Stromzähler, die sich in den *Smart Appliance Enabler* integrieren lassen.
Also habe ich nach einer Do-It-Yourself-Lösung gesucht ...

## Wie?
Zunächst wollte ich einfach einen Zähler auf einem Stück DIN-Schiene neben der Steckdose befestigen.
Das sieht aber nicht gerade gut aus und auch die Anschlüsse sind nicht vor Berührung geschützt.
Vielmehr sollte es optisch ähnlich wie die SMA-Bluetooth-Steckdosen oder die Plugwise-Circles sein.
In dieses Gehäuse muss ein Stromzähler mit S0-Schnittstelle sowie ein Mikro-Controller mit WLAN-Anbindung passen.
Leider scheint es keine Miniatur-Stromzähler ohne Display aber mit S0-Schnittstelle zu geben.
Also muss ein normaler, aber möglichst kleiner Stromzähler, irgendwie in das Gehäuse passen.

Auf der Suche nach einem entsprechenden Gehäuse bin ich bei den Überspannungsschutz-Adaptern gelandet.
Der **Universal Überspannungsschutz-Adapter 919.004 von Bachmann** hat ein relativ grosses Gehäuse.
In dieses Gehäuse passt der Zähler **DRS155B von B+G E-Tech**, wenn man einige Anpassungen vornimmt.
Glücklicherweise gibt es mit dem **ESP8266-ESP01** einen kleine Mikro-Controller-Platine (1x2cm !!!), die bereits WLAN an Bord hat.
Diese bnötigt 3,3V bei 80 mA maximalem Strom. Ein sehr kompaktes Schaltnetzteil ist das **SUNNY NE1000**, das allerdings 5V liefert.
Also benötigt man zusätzlich noch einen **3,3V-Spannungsregler wie den LF33CV** (Gehäuseform TO-220).
Ausserdem benötigt man:
- Lötkolben
- Kabelreste (TODO: genauer angeben) 
- Kabelschuh-Ringösen (TODO: genauer angeben)
- Schrauben zur Befestigung der Kabelschuhe den Zählerkontakten
- PIN-Header als Sockel für ESP8266-ESP01 und zum Flaschen
- FTDI USB-zu-Seriell Adapter (z.B. FT32RL)

## Stichpunkte
- nicht am Zähler löten (Hitze) - Zähler geschrottet
- Stromfluss im Zähler darf nicht modifiziert werden, sonst wird falsch/nicht gezählt  - Zähler geschrottet 

## Bilder
![Bachmann 919.004](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/bachmann_919004.jpg)
Universal Überspannungsschutz-Adapter 919.004 von Bachmann