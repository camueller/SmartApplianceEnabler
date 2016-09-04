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
- USB-auf-TTL Serienadapter

## Stichpunkte
- nicht am Zähler löten (Hitze) - Zähler geschrottet
- Stromfluss im Zähler darf nicht modifiziert werden, sonst wird falsch/nicht gezählt  - Zähler geschrottet 

## Bauanleitung
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

## Programmieren des ESP8266-ESP01
Damit der **ESP8266-ESP01** die Impulse des Stromzählers zählt und über WLAN an den SAE weiterleitet, muss dieser entsprechend programmiert werden.  Zum Programmieren muss er an einen PC angeschlossen werden. Dazu ist ein **USB-auf-TTL Serienadapter** erforderlich, z.B. einer mit FT232RL-Chip von FTDI, das man bei Ebay für weniger als 5 Euro bekommt. Idealerweise unterstützt er neben 5V auch 3,3V (meist über einen Jumper), weil der **ESP8266-ESP01** mit 3,3V betrieben werden muss! Bei Verwendung unter Linux müssen zu dessen Verwendung keine weiteren Treiber installiert werden.

Bild vom Adapter

Bild von Breadboard mit Verkabelung



Das geht am einfachsten mit der [Arduino IDE](https://www.arduino.cc/en/Main/Software), dem Programmier-Tool für Mikrokontroller der Arduino-Platform, die zunächst heruntergeladen werden muss und ausgepackt werden muss (zum Zeitpunkt des Schreibens dieser Seite war Version 1.6.11 aktuell). Der **ESP8266-ESP01** gehört eigentlich nicht zur Arduino-Familie, aber die Unterstützung kann dank [dieses Projektes](https://github.com/esp8266/Arduino) zur Arduino IDE hinzugefügt werden. Die Installation ist wirklich einfach:

1. Starten der Arduino IDE
2. ```Datei->Voreinstellungen``` 
3. Unter *Zusätzliche Boardverwalter URLs* eintragen: ```http://arduino.esp8266.com/stable/package_esp8266com_index.json``` und mit ```OK``` bestätigen
4. Die Liste der Boardverwalter über ```Werkzeuge->Board->Boardverwalter``` öffnen, den Eintrag *esp8266* auswählen (Version stand bei mir auf 2.3.0) und ```Installieren``` drücken. Nachdem alle Dateien heruntergeladen wurden, den Dialog mit ```Schließen``` beenden.
5. Unter ```Werkzeuge->Board``` den Wert *Generic ESP8266 Module* auswählen
6. Das Beispiel ```Datei->Beispiele->ESP8266->Blink``` laden
7. Unter ```Werkzeuge->Port``` den Port einstellen, an dem der USB-auf-TTL Serienadapter angemeldet ist (kann unter Linux mit ```dmesg``` angezeigt werden)
8. Zum Flashen muss ```GPIO_0``` mit GND verbuunden werden
9. Mit ```Sketch->Hochladen``` das Beispiel flashen: dabei flackern die blaue LED des ESP8266-ESP01 sowie die beiden roten RX/TX-LEDs auf dem USB-auf-TTL Serienadapter. Auf der Konsole der Arduino IDE sollte etwa folgende Ausgabe stehen:

```
Der Sketch verwendet 222.125 Bytes (51%) des Programmspeicherplatzes. Das Maximum sind 434.160 Bytes.
Globale Variablen verwenden 31.492 Bytes (38%) des dynamischen Speichers, 50.428 Bytes für lokale Variablen verbleiben. Das Maximum sind 81.920 Bytes.
Uploading 226272 bytes from /tmp/buildac45c33e6729fe7f9a7166ce531b2259.tmp/Blink.ino.bin to flash at 0x00000000
................................................................................ [ 36% ]
................................................................................ [ 72% ]
.............................................................                    [ 100% ]
```
Wenn danach die blaue LED auf dem ESP8266-ESP01 langsam blinkt (jeweils 1s an, 2s aus), ist die Funktionstüchtigkeit des ESP8266-ESP01 und der Verkabelung erwiesen.
