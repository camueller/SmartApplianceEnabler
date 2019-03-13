# Ladegeräte für Elektro-Autos (Wallboxen)
## Konfiguration
### Appliance
Um ein Gerät zum Laden von Elektroautos konfigurieren zu können, muss als Typ des Gerätes ```Elektroauto-Ladegerät``` eingestellt werden.

### Schalter
Im *Smart Appliance Enabler* wird ein Ladegerät für Elektro-Autos als komplexer Schalter mit diversen Konfigurationsparametern für den Controller und Fahrzeuge repräsentiert.
#### Controller
Die Konfigurationsparameter richten sich dabei vor allem nach dem im Ladegerät verbauten Controller. Momentan unterstützt der *Smart Appliance Enabler* folgende Controller:
* [Phoenix Contact EM-CP-PP-ETH](https://www.phoenixcontact.com/online/portal/de?uri=pxc-oc-itemdetail:pid=2902802)

Damit die Konfigurationsparameter nicht manuell eingegeben werden müssen, existieren für die unterstützten Controller Vorlagen, aus denen die benötigte Konfiguration geladen werden kann. Grundsätzlich sollte auch die Verwendung von Controllern möglich sein, die nicht direkt untersützt werden, solange diese über ein unterstütztes Protokoll angebunden werden:
* [Modbus/TCP](Modbus_DE.md)

#### Fahrzeuge
Die Konfiguration von Fahrzeugen beinhaltet Parameter zur Steuerung des Ladevorgangs und Standardwerte für Dialoge.
Ein [Script zum automatisierten Abfragen des State of Charge (SOC)](SOC_DE.md) läßt sich für jedes Fahrzeug konfigurieren.

### Zeitpläne
Die grundsätzlich Konfiguration der Zeitpläne ist [hier](Configuration_DE.md#zeitpläne) beschrieben.
Abweichend davon existiet bei E-Auto-Ladegeräten nicht nur die ```Anforderungenart``` mit dem Wert ```Laufzeit```, sondern zwei weitere Optionen:

Mit der Anforderungsart ```Energiemenge``` kann eine bestimmte minimale und/oder maximale Energiemenge (in Wh) zum Ausdruck gebracht werden. Wenn die minimale Energiemenge auf ```0``` gesetzt wird und die maximale Energiemenge auf einen größeren Wert, wird damit zum Ausdruck gebracht, dass dieses Gerät *Überschussenenergie* aufnehmen kann bzw. soll.

Mit der Anforderungsart ```Laden bis SOC``` wird genau die Energiemenge angefordert, die notwendig ist, um einen bestimmten SOC zu erreichen. Zur Berechnung dieser Energiemenge wird die Batteriekapazität und der SOC des Fahrzeugs bei Ladebeginn herangezogen. Für letzteres ist es notwendig, dass der SOC des Fahrzeugs via Script abgefragt werden kann.

## Ampel-Steuerung
Der Ist-SOC wird bei Konfiguration eines SOC-Scripts vorbelegt mit dem aktuellen Wert zu diesem Zeitpunkt, ansonsten kann er im Auto angelesen und eingegeben werden, wenn man dem SHM eine gute Planung ermöglichen will (ansonsten wird 0 angenommen).

Wird kein Soll-SOC eingegeben, wird 100% angenommen.

Wird bei "bis" ein Wochentag/Zeit eingegeben, wird dem SHM der Bedarf gemeldet aber der SAE schaltet den Lader nicht selbst an, d.h. das Laden beginnt erst, wenn der SHM einen Einschaltbefehl schickt, wobei ja auch die Ladeleistung von ihm vorgegeben wird.

Wird bei "bis" kein Wochentag/Zeit eingegeben, wird dem SHM der Bedarf gemeldet, aber der SAE schaltet den Lader sofort ein. Weil der SAE keine Ladeleistung setzt, lädt der Lader mit voller Leistung.