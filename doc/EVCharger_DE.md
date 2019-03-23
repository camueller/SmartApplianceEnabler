# Ladegeräte für Elektro-Autos (Wallboxen)
## Konfiguration
### Appliance
Um ein Gerät zum Laden von Elektroautos konfigurieren zu können, muss als Typ des Gerätes ```Elektroauto-Ladegerät``` eingestellt werden.

![Elektroauto-Ladegerät](../pics/fe/GeraetEV.png)

### Schalter
Im *Smart Appliance Enabler* wird ein Ladegerät für Elektro-Autos als komplexer Schalter mit diversen Konfigurationsparametern für den Controller und Fahrzeuge repräsentiert.
#### Controller
Die Konfigurationsparameter richten sich dabei vor allem nach dem im Ladegerät verbauten Controller. Momentan unterstützt der *Smart Appliance Enabler* folgende Controller:
*  [Phoenix Contact EM-CP-PP-ETH](https://www.phoenixcontact.com/online/portal/de?uri=pxc-oc-itemdetail:pid=2902802)
  * DIP-Schalter: DIP-Schalter 10 an, alle anderen DIP-Schalter aus

Damit die Konfigurationsparameter nicht manuell eingegeben werden müssen, existieren für die unterstützten Controller **Vorlagen**, aus denen die benötigte Konfiguration geladen werden kann.

![Konfiguration Phoenix Contact](../pics/fe/EVChargerPhoenixContact.png)

Grundsätzlich sollte auch die Verwendung von Controllern möglich sein, die nicht direkt untersützt werden, solange diese über ein unterstütztes Protokoll angebunden werden:
* [Modbus/TCP](Modbus_DE.md)

#### Fahrzeuge
Die Konfiguration von Fahrzeugen beinhaltet Parameter zur Steuerung des Ladevorgangs und Standardwerte für Dialoge.
Für eine möglichst genaue Ermittlung des Energiebedarfs muss der Ladezustand (State of Charge bzw. SOC) beim Verbinden des Fahrzeugs mit dem Ladegerät bekannt sein. Während dieser Wert beim [manuellen Start des Ladevorganges](#Ampel-Steuerung) auch eingegeben werden kann, kann dieser Wert bei Verwendung von [Zeitplänen](#Zeitpläne) nur durch ein [Script zum automatisierten Abfragen des SOC](soc/SOC_DE.md) ermittelt werden. Wird kein solches Script konfiguriert, wird beim Verbinden des Fahrzeugs mit dem Ladegerät der SOC mit 0 angenommen und ein entsprechend großer Energiebedarf gemeldet.

![Fahrzeugkonfiguration](../pics/fe/EV.png)

### Zeitpläne
Die grundsätzliche Konfiguration der Zeitpläne ist [hier](Configuration_DE.md#zeitpläne) beschrieben.
Abweichend davon existiert bei E-Auto-Ladegeräten nicht nur die **Anforderungsart** mit dem Wert ```Laufzeit```, sondern zwei weitere Optionen:

Mit der Anforderungsart ```Energiemenge``` kann eine bestimmte minimale und/oder maximale Energiemenge (in Wh) zum Ausdruck gebracht werden. Wenn die minimale Energiemenge auf ```0``` gesetzt wird und die maximale Energiemenge auf einen größeren Wert, wird damit zum Ausdruck gebracht, dass dieses Gerät *Überschussenenergie* aufnehmen kann bzw. soll.

![Anforderungsart Energiemenge](../pics/fe/SchaltzeitenTagesplanEnergiemenge.png)

Mit der Anforderungsart ```Laden bis SOC``` wird genau die Energiemenge angefordert, die notwendig ist, um einen bestimmten SOC zu erreichen. Zur Berechnung dieser Energiemenge wird die Batteriekapazität und der SOC des Fahrzeugs bei Ladebeginn herangezogen. Für letzteres ist es notwendig, dass der [SOC des Fahrzeugs via Script](soc/SOC_DE.md) abgefragt werden kann.

![Anforderungsart SOC](../pics/fe/SchaltzeitenTagesplanSOC.png)

## Status-Anzeige und manuelle Steuerung

Für Geräte vom Typ ```Elektroauto-Ladegerät``` unterscheiden sich die Eingabe-Felder zum manuellen Schalten, die nach einem **Klick auf die grüne Lampe der Ampel** angezeigt werden, von den Feldern, die bei anderen Geräten angezeigt werden.

![Eingabefelder manueller Start](../pics/fe/StatusEVAmpelEdit.png)

Wenn ein [SOC-Script](soc/SOC_DE.md) für das ausgewählte Fahrzeug angegeben wurde, wird das Eingabefled ```Ist-SOC``` vorbelegt mit dem aktuellen Wert zu diesem Zeitpunkt. Ansonsten kann er im Auto abgelesen und eingegeben werden, wenn man dem SHM eine gute Planung ermöglichen will. Ansonsten wird 0 angenommen und ein entsprechend hoher Energiebedarf gemeldet.

Wird im Eingabefeld ```Soll-SOC``` kein Wert eingegeben, wird 100% angenommen und ein entsprechend hoher Energiebedarf gemeldet.

Wenn im Eingabefeld ``bis`` ein Wochentag/Zeit eingegeben, wird dem SHM der Bedarf gemeldet aber der SAE schaltet den Lader nicht selbst an, d.h. das Laden beginnt erst, wenn der SHM einen Einschaltbefehl schickt, wobei auch die Ladeleistung von ihm vorgegeben wird. Der SAE schaltet den Lader sofort ein, wenn kein Wochentag/Zeit eingegeben werden. Weil der SAE in diesem Fall die Ladeleistung nicht reduziert, lädt der Lader mit voller Leistung.

Auch die Status-Anzeige sieht bei Geräten vom Typ ```Elektroauto-Ladegerät``` anders aus.

![Statusanzeige Laden Manuell](../pics/fe/StatusEVAmpelView.png)

Für eine via Schaltzeiten anforderte Energiemenge sieht die Statusanzeige vor dem Einschalten wie folgt aus:

![Statusanzeige Laden Energiemenge](../pics/fe/StatusEVSchaltzeiten.png)
