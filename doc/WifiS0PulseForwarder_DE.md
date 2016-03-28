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
![919004.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/919004.jpg)  
Universal Überspannungsschutz-Adapter 919.004 von Bachmann
- der Adapter wird von zwei kreuzschlitz-ähnlichen Schrauben, die 3 statt 4 Schlitze haben (dreieckig angeordnet)
- Schrauben liessen sich mit kleinem Flachschraubendreher herausdrehen
![919004_stecker.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/919004_stecker.jpg)  
Stecker-Kontakte des Universal Überspannungsschutz-Adapter 919.004 von Bachmann  
![drs155b.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/drs155b.jpg)  
Stromzähler DRS155B von B+G E-Tech ohne Gehäuse  
![919004_gehauserueckseite.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/919004_gehauserueckseite.jpg)  
Gehäusehälfte (Platienenhalterungen mit grossem Bohrer weggebohrt, Seitenstege entfernt)  
![steckerverbindung_hochgebogen.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/steckerverbindung_hochgebogen.jpg)  
- Verbindungssteeg zwischen Stecker und Steckdose zwecks Unterbrechnung hochgebogen
- Kabel mit Flachtecker zum Zählereingang  
![steckerverbindung_isolation.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/steckerverbindung_isolation.jpg)  
Isolation (hier rote Mantelleitung-Hälfte) zwischen Stecker und Steckdose  
![zaehler_angeschlossen.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/zaehler_angeschlossen.jpg)   
Zähler im Gehäuse
- Zähler-Kupfer-Anschlüsse begradigt (auf dem Bild sind die Kabel noch angelötet - wurden bei Folgeversuch geschraubt)  
![netzteil_mit_spannungsregler.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/netzteil_mit_spannungsregler.jpg)  
Netzteil SUNNY NE1000 mit Spannungsregler wie den LF33CV  
![netzteil_platzierung.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/netzteil_platzierung.jpg)  
Platzierung des Netzteils  
![netzteil_verkabelt.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/netzteil_verkabelt.jpg)  
- Netzteils mit Heisskleber im Deckel befestigt
- ESP8266-Sockel: 2 Pin-Header (4 Pins) mit Heisskleber zusammengeklebt, Pins hochgebogen, mit Heisskleber im Deckel befestigt  
![zaehler_kabelschuhe.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/zaehler_kabelschuhe.jpg)  
Kabelschuhe an Zähleranschlüssen angeschraubt  
![elektronik_komplett.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/elektronik_komplett.jpg)  
Elektronik komplett und WLAN aktiv (siehe rote LED auf ESP8266)  
![gehaeuse_geschlossen_nach_umbau.jpg](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/gehaeuse_geschlossen_nach_umbau.jpg)  
Gehäuse geschlossen nach Umbau
- das Display ist leider defekt, nachdem mir die Zählerplatine einmal heruntergefallen ist (die S0-Impulse kommen aber) 
- Öffnungen neben Display und auf gegenüberliegender Seite kann man mit Kunststoffresten und Heisskleber verblenden
