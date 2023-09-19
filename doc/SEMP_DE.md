# SEMP
## Protokoll
Der *Sunny Home Manager* findet den *Smart Appliance Enabler* über das [UPnP-Protokoll](https://de.wikipedia.org/wiki/Universal_Plug_and_Play), über das sich auch Multimedia-Geräte einfach finden. Über dieses Protokoll teilt der *Smart Appliance Enabler* dem *Sunny Home Manager* die eigentliche **SEMP-URL** mit.

Aus der Verwendung des UPnP-Protokolls ergibt sich die Notwendigkeit, dass *Sunny Home Manager* und *Smart Appliance Enabler* sich **im gleichen Netz befinden** und per Multicast miteinander kommunizieren können müssen!

Normalerweise kann der *Smart Appliance Enabler* diese URL selbst korrekt bestimmen. Wenn der Host allerdings mehrere Netzwerk-Interfaces hat oder der *Smart Appliance Enabler* in einer virtuellen Maschine oder einem Container läuft, kann es notwendig sein, dem *Smart Appliance Enabler* mitzuteilen, welche URL er dem *Sunny Home Manager* kommunizieren soll. Das erfolgt über den Konfigurationsparameter `semp.gateway.address` in der Datei `/etc/default/smartapplianceenabler`.

## <a name="url"></a> SEMP-URL

Nachdem der *Sunny Home Manager* den *Smart Appliance Enabler* gefunden hat, besteht die weitere Kommunikation **ausschliesslich** darin, dass der *Sunny Home Manager* **alle 60 Sekunden** die folgende **SEMP-URL** des *Smart Appliance Enabler* aufruft (wobei Hostnamen / IP-Adresse entsprechend anzupassen ist):

```
http://raspi:8080/semp
```

Die vom *Smart Appliance Enabler* an den *Sunny Home Manager* kommunizierte URL des SEMP-Hosts wird direkt nach dem Start in das Log geschrieben: 

```bash
$ grep "SEMP UPnP" /tmp/rolling-2020-12-31.log
2020-12-31 14:36:22,744 INFO [main] d.a.s.s.d.SempDiscovery [SempDiscovery.java:57] SEMP UPnP will redirect to http://192.168.1.1:8080
```

*Webmin*: In [View Logfile](#webmin-logs) gibt man hinter `Only show lines with text` den Text `SEMP UPnP` ein und drückt Refresh.

Die Eingabe dieser URL in einen Web-Browser, ergänzt um den Pfad `/semp` (entsprechend des obigen Beispiels wäre das `http://192.168.1.1:8080/semp`), muss zur Anzeige des nachfolgend beschrieben SEMP-XML führen. Wenn das nicht funktioniert, weil diese URL nicht korrekt ist, kann der *Sunny Home Manager* nicht mit dem *Smart Appliance Enabler* kommunizieren, d.h es können keine Geräte im *Sunny Portal* hinzugefügt werden und Geräte werden nicht gemessen und geschaltet!

## <a name="xml"></a> SEMP-XML

Durch Eingabe der [SEMP-URL](#url) in einen ganz normalen Web-Browser kann man sich anzeigen lassen, welche Informationen der *Smart Appliance Enabler* dem *Sunny Home Manager* übermittelt.

Die SEMP-URL liefert ein [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language) -Dokument gemäss der SEMP-Spezifikation, in der für jedes Gerät ein `DeviceInfo` und ein `DeviceStatus` enthalten ist. Optional können auch `PlanningRequest` enthalten sein. 

`DeviceInfo`, `DeviceStatus` und `PlanningRequest` enthalten jeweils ein Element `DeviceId`, über das zum Ausdruck gebracht wird, für welches Gerät es gilt. Dabei ist die `DeviceId` identisch mit der `ID`, welche beim Anlegen des Gerätes im *Smart Appliance Enabler* vergeben wurde.

Bei einer Fehlersuche wird man genau den *DeviceStatus* betrachten, in dem die *DeviceId* des problematischen Gerätes enthalten ist.
