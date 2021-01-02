# SEMP
## Protokoll

Der *Sunny Home Manager* findet den *Smart Appliance Enabler* über das [UPnP-Protokoll](https://de.wikipedia.org/wiki/Universal_Plug_and_Play), über das sich auch Multimedia-Geräte einfach finden. Über dieses Protokoll teilt der *Smart Appliance Enabler* dem *Sunny Home Manager* die eigentliche **SEMP-URL** mit.

Aus der Vewendung des UPnP-Protokolls ergibt sich die Notwendigkeit, dass *Sunny Home Manager* und *Smart Appliance Enabler* sich **im gleichen Netz befinden** und per Multicast miteinander kommunizieren können müssen!

Normalerweise kann der *Smart Appliance Enabler* diese URL selbst korrekt bestimmen. Wenn der Host allerdings mehrere Netzwerk-Interfaces hat oder der *Smart Appliance Enabler* in einer virtuellen Maschine oder einem Container läuft, kann es notwendig sein, dem *Smart Appliance Enabler* mitzuteilen, welche URL er dem *Sunny Home Manager* kommunizieren soll. Das erfolgt über den Konfigurationsparameter `semp.gateway.address` in der Datei `/etc/default/smartapplianceenabler`.

## SEMP-URL
<a name="url">

Nachdem der *Sunny Home Manager* den *Smart Appliance Enabler* gefunden hat, besteht die weitere Kommunikation **ausschliesslich** darin, dass der *Sunny Home Manager* **alle 60 Sekunden** die folgende **SEMP-URL** des *Smart Appliance Enabler* aufruft (wobei Hostnamen / IP-Adresse entsprechend anzupassen ist):
```
http://raspi:8080/semp
```
Die vom *Smart Appliance Enabler* an den *Sunny Home Manager* kommunizierte [SEMP-URL findet sich im Log](Logging_DE.md#anzeige-der-semp-url). Wenn diese URL nicht korrekt ist, kann der *Sunny Home Manager* nicht mit dem *Smart Appliance Enabler* kommunizieren, d.h es können keine Geräte im *Sunny Portal* hinzugefügt werden und Geräte werden nicht gemessen und geschaltet!

## SEMP-XML
<a name="xml">

Durch Eingabe der [SEMP-URL](#url) in einen ganz normalen Web-Browser kann man sich anzeigen lassen, welche Informationen der *Smart Appliance Enabler* dem *Sunny Home Manager* übermittelt.

Die SEMP-URL liefert ein [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language) -Dokument gemäss der SEMP-Spezifikation, in der für jedes Gerät ein `DeviceInfo` und ein `DeviceStatus` enthalten ist. Optional können auch `PlanningRequest` enthalten sein. 

`DeviceInfo`, `DeviceStatus` und `PlanningRequest` enthalten jeweils ein Element `DeviceId` über das zum Ausdruck gebracht wird, für welches Gerät es gilt. Dabei ist die `DeviceId` identisch mit der `ID`, welche beim Anlegen des Gerätes im *Smart Appliance Enabler* vergeben wurde.

Bei einer Fehlersuche wird man genau den *DeviceStatus* betrachten, in dem die *DeviceId* des problematischen Gerätes enthalten ist.
