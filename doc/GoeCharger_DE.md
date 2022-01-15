# go-eCharger

Ein sehr vielseitiges, kompaktes Ladegerät, das auch für den mobilen Einsatz geeignet ist, ist der [go-eCharger](https://go-e.co/go-echarger-home/).

Bei Verwendung dieser Wallbox ist kein separater Stromzähler erforderlich, weil der Zählerwert von der Wallbox selbst mit hoher Genauigkeit bereitgestellt wird und der *Smart Appliance Enabler* daraus die aktuelle Leistungsaufnahme berechnet.

Den go-eCharger gibt es mittlerweile in mehreren Versionen.
Die aktuellste Version ist die V3, welche mehr features als die Vorgängerversion unterstützt.
Um einen V3 go-eCharger voll kompatibel mit den unten genannten Parametern zu machen, muss die lokale HTTP API v1 in der App des go-eCharger
aktiviert werden unter: Internet -> erweiterte Einstellungen -> Aktiviere lokale HTTP API v1

## Geräte-Konfiguration

Der go-eCharger muss mit WLAN verbunden sein, in dem sich auch der *Smart Appliance Enabler* befindet oder das zumindest für ihn erreichbar ist.
Die HTTP-Schnittstelle des go-eCharger muss aktiviert werden, damit der *Smart Appliance Enabler* mit ihm darüber kommunizieren kann.

## Konfiguration im Smart Appliance Enabler

### Wallbox

Für die Konfiguration sollte die Vorlage `go-eCharger` verwendet werden - dadurch werden alle Felder korrekt ausgefüllt. Lediglich die IP-Adresse bzw. der Hostname in den URL-Feldern muss auf die des go-eChargers angepasst werden. 

![Konfiguration des go-eCharger als Schalter](../pics/fe/EVChargerGoeCharger.png)

### Zähler

Wie oben geschrieben muss der go-eCharger selbst als Zähler angegeben werden, d.h.
die IP-Adresse bzw. der Hostname in den URL-Feldern muss auf die des go-eChargers angepasst werden. 

Als `Format` muss `JSON` ausgewählt werden, damit die Antworten des go-eCharger korrekt interpretiert werden können.

Das Feld `Pfad` muss den Wert `$.dws` enthalten, damit der *Smart Appliance Enabler* weiss, an welcher Stelle in der Antwort des go-eChargers der Wert für die Energie enthalten ist. 

Im Feld `Umrechnungsfaktor` muss die Zahl `0.0000027778` eingegeben werden, weil der go-eCharger die Energie in 10 Deka-Watt-Sekunden liefert.

Als Parameter ist `Zählerstand` zu wählen.

![Konfiguration des go-eCharger als Zähler](../pics/fe/EVChargerGoeChargerMeter.png)

### Leistung

Der go-eCharger kann auch seine aktuell gemessene Leistung ausgeben, d.h. er kann auch als `Leistung` im *Smart Appliance Enabler*
konfiguriert werden, dies hat jedoch zur folge das die Abfrage des go-eCharger öfter erfolgen muss als in der Konfiguration oben mit `Zählerstand`.
Die IP-Adresse bzw. der Hostname in den URL-Feldern muss auf die des go-eChargers angepasst werden. 

Als `Format` muss `JSON` ausgewählt werden, damit die Antworten des go-eCharger korrekt interpretiert werden können.

Das Feld `Pfad` muss den Wert `$.nrg[11]` enthalten, damit der *Smart Appliance Enabler* weiss, an welcher Stelle in der Antwort des go-eChargers der Wert für die Energie enthalten ist, in diesem Fall findet sich der Wert in dem Array nrg auf der Stelle 12. 

Im Feld `Umrechnungsfaktor` muss die Zahl `10` eingegeben werden, weil der go-eCharger die Leistung in 0.01kW liefert.
Da die erste Generation go-eCharger relativ ungenau messen, habe ich den Faktor 11.4 eingetragen und komme so annährend auf realistische Werte.

Als Parameter ist `Leistung` zu wählen.
