# Wallboxen

Damit der Sunny Home Manager die Leistung von Wallboxen steuern kann, **muss zur Bestimmung der aktuellen Leistungsaufnahme ein Stromzähler im Smart Appliance Enabler konfiguriert werden**!

## Konfiguration
### Appliance
Um ein Gerät als Wallbox konfigurieren zu können, muss als Typ des Gerätes `Elektroauto-Ladegerät` eingestellt werden.

![Wallbox](../pics/fe/GeraetEV.png)

### Schalter
Im *Smart Appliance Enabler* wird eine Wallbox als komplexer Schalter mit diversen Konfigurationsparametern und die Fahrzeuge repräsentiert.

Momentan unterstützt der *Smart Appliance Enabler* folgende Wallboxen:
* [Wallboxen mit Phoenix Contact EM-CP-PP-ETH-Controller](PhoenixContactEMCPPPETH_DE.md) wie z.B. Walli
* [go-eCharger](GoeCharger_DE.md)
* wallbe

Der *Smart Appliance Enabler* stellt Vorlagen bereit, welche die Konfiguration für die genannten Wallboxen beinhalten.
Darüber hinaus sollte auch die Verwendung von anderen Wallboxen möglich sein, solange diese über ein unterstütztes Protokoll angebunden werden:
* [Modbus/TCP](Modbus_DE.md)
* HTTP

#### Fahrzeuge
Die Konfiguration von Fahrzeugen beinhaltet Parameter zur Steuerung des Ladevorgangs und Standardwerte für Dialoge.

Für den Ziel-Ladezustand können Standardwerte festgelegt werden.
Der Standardwert für manuelles Laden beinhaltet lediglich die Vorbelegung des Feldes für den Soll-Ladezustand in der Eingabemaske, die nach Klick auf die grüne Ampelleuchte angezeigt wird.
Wird ein Standardwert für Überschussenergie gesetzt, wird nach dem Verbinden des Fahrzeugs nur bis zu diesem Wert mit Überschussenergie geladen und danach der Ladevorgang gestoppt.

Die vom *Smart Appliance Enabler* unterstützten Wechselstrom-Wallboxen können nicht den aktuellen Ist-Ladezustand vom Fahrzeug ermitteln und an den *Smart Appliance Enabler* kommunizieren! Für eine möglichst genaue Ermittlung des Energiebedarfs muss dieser Wert aber bekannt sein. Der *Smart Appliance Enabler* bietet deshalb die Möglichkeit der Einbindung eines [Scripts zum automatisierten Abfragen des SOC](soc/SOC_DE.md), sofern dies vom Fahrzeug-Hersteller unterstützt wird. Zusätzlich besteht die Möglichkeit, den Ist- und Soll-Ladezustand einzugeben beim [manuellen Start des Ladevorganges](#status-anzeige-und-manuelle-steuerung).

Wenn ein *SOC-Script* konfiguriert wurde, wird dieses **automatisch nach dem Verbinden des Fahrzeuges mit der Wallbox** ausgeführt.

Auf Basis der Werte für
- `Batteriekapazität`: aus der Fahrzeug-Konfiguration
- `Ladeverluste`: aus der Fahrzeug-Konfiguration
- `Ist-SOC`: geliefert vom SOC-Script oder eingegeben über die [Ampel-Steuerung]
- `Soll-SOC` Standardwert aus der Fahrzeug-Konfiguration oder eingegeben über [Ampel-Steuerung](#manuelle-steuerung))

wird die initial vom *Sunny Home Manager* anzufordernde Energiemenge berechnet. 

Der *Smart Appliance Enabler* **berechnet fortlaufend den SOC** entsprechend der bereits geladenen Energiemenge. Von dieser Energiemenge müssen aber die Ladeverluste abgezogen werden, weil sie nicht zu einer Erhöhung des SOC im Fahrzeug führen. Aus diesem Grund sollte hier ein möglichst zutreffender Wert eingetragen werden, der Standardwert beträgt 10%.

Wenn ein SOC-Script konfiguriert wurde und sich der berechnete SOC entweder um den konfigurierte Wert (Standard: 20%) erhöht oder seit der letzten Ausführung des SOC-Script die konfigurierte Zeit vergangen ist, wird das **SOC-Script erneut ausgeführt**. Der berechnete SOC wird mit dem tatsächlichen SOC verglichen und daraus die tatsächlichen Ladeverluste berechnet. Für alle nachfolgenden Berechnungen des SOC bis zur nächsten Ausführung des SOC-Scripts während des aktuellen Ladevorganges werden die tatsächlichen Ladeverluste berücksichtigt.

**Ohne SOC-Script** und ohne Eingabe des aktuellen Ist-Ladezustands geht der *Smart Appliance Enabler* von einem Ist-Ladezustand von 0% aus und meldet einen entsprechend großen Energiebedarf. Das verschlechtert zwar die Planung des *Sunny Home Manager*, aber unabhängig davon beendet die Wallbox das Laden spätestens, wenn das Fahrzeug voll geladen ist.

Wenn vom SOC-Script nicht nur eine Zahl ohne weitere Zeichen geliefert wird, sondern der Verbrauchswert irgendwo in einem Text (XML, JSON, ...) enthalten ist, muss ein [Regulärer Ausdruck/Regex](WertExtraktion_DE.md) zum Extrahieren der Leistung mit angegeben werden.

![Fahrzeugkonfiguration](../pics/fe/EV.png)

## Manuelle Steuerung

Nach einen **Klick auf das grüne Ampellicht** kann man den `Lademodus` für den aktuellen Ladevorgang festelegen.

In Abhängigkeit des gewählten Lademodus werden die Felder `Ladezustand: Ist` und/oder `Ladezustand: Soll` angzeigt, wobei Folgendes gilt:
- wenn ein [SOC-Script](soc/SOC_DE.md) für das ausgewählte Fahrzeug angegeben wurde, wird das Eingabefeld ```Ladezustand: Ist``` vorbelegt mit dem aktuellen Wert zu diesem Zeitpunkt. Ohne SOC-Script kann er im Auto abgelesen und hier eingegeben werden, wenn man dem *Sunny Home Manager* eine gute Planung ermöglichen will. Ansonsten wird 0 angenommen und ein entsprechend hoher Energiebedarf gemeldet.
- wird im Eingabefeld ```Ladezustand: Soll``` kein Wert eingegeben, wird 100% angenommen und ein entsprechend hoher Energiebedarf an den *Sunny Home Manager* gemeldet.

### Lademodus: Schnell
Das Fahrzeug wird sofort mit der konfigurierten, maximalen Leistung geladen. Es erfolgt keine Optimierung hinsichtlich Stromkosten und der Nutzung von PV-Strom.

![Eingabefelder Lademodus Schnell](../pics/fe/StatusEVAmpelEdit.png)

### Lademodus: Optimiert

Das Ladegerät wird mit so viel überschüssigem PV-Strom wie möglich betrieben. Dabei wird sichergestellt, dass der vorgegebene Ladezustand (SOC) zum eingegebenen Zeitpunkt erreicht ist, notfalls durch Bezug von Strom aus den Netz. Danach wird automatisch in den Lademodus "PV-Überschuss" gewechselt.

![Eingabefelder Lademodus Optimiert](../pics/fe/StatusEVAmpelEditOptimized.png)

### Lademodus: PV-Überschuss

Das Fahrzeug wird mit überschüssigem PV-Strom, der andernfalls ins Netz eingespeist oder abgeregelt werden würde, geladen. Dieser Lademodus ist automatisch aktiv, sobald das Fahrzeug mit dem Ladegerät verbunden ist und solange kein anderer Lademodus aktiviert wurde. Insofern dient die Auswahl dieses Lademodus nur dazu, einen Soll-SOC festzulegen, der von den in der Konfiguration des Fahrzeugs festgelegten Werten abweicht. In diesem Lademodus kann die Ladung des Fahrzeugs nicht in allen Fällen sichergestellt werden. Reicht der überschüssige PV-Strom nicht zur Ladung aus, findet keine Ladung statt.

![Eingabefelder Lademodus PV-Überschuss](../pics/fe/StatusEVAmpelEditExcessEnergy.png)

### Zeitpläne
Abweichend von der [Konfiguration von Zeitplänen für andere Geräte](Configuration_DE.md#zeitpläne) existieren bei Wallboxen als **Anforderungsart** zwei Optionen:

#### Laden bis SOC
Mit der Anforderungsart `Laden bis SOC` wird genau die Energiemenge angefordert, die notwendig ist, um einen bestimmten SOC zu erreichen. Zur Berechnung dieser Energiemenge wird die Batteriekapazität und der SOC des Fahrzeugs bei Ladebeginn herangezogen. Für letzteres ist es notwendig, dass der [SOC des Fahrzeugs via Script](soc/SOC_DE.md) abgefragt werden kann.

![Anforderungsart SOC](../pics/fe/SchaltzeitenTagesplanSOC.png)

#### Laden einer Energiemenge

Mit der Anforderungsart `Energiemenge` kann eine bestimmte Energiemenge (in Wh) angefordert werden. Normalerweise wird nur die `max. Ernergiemenge` angegeben, die auf jeden Fall geladen werden soll.

Optional kann für die `min. Energiemenge` ein kleinerer Wert angegeben werden. Falls er angegeben ist, wird nur dieser Wert auf jeden Fall geladen und die darüber hinausgehende Energiemenge bis zur `max. Ernergiemenge` nur dann, wenn **Überschussenergie** verfügbar ist.

![Anforderungsart Energiemenge](../pics/fe/SchaltzeitenTagesplanEnergiemenge.png)

## Status-Anzeige
Wenn das Fahrzeug nicht mit der Wallbox verbunden ist, wird lediglich der Status angezeigt:

![Statusanzeige ohne verbundenes Fahrzeug](../pics/fe/StatusEVAmpelViewNotConnected.png)

Nachdem das Fahrzeug verbunden wurde, werde weitere Details angezeigt. Der SOC wird mit "0%" angezeigt, falls kein [SOC-Script](#fahrzeuge) konfiguriert wurde.

![Statusanzeige ohne verbundenes Fahrzeug](../pics/fe/StatusEVAmpelViewConnected.png)

Wenn ein Ladevorgang aktiv ist, sieht die Statusanzeige wie folgt aus:

![Statusanzeige ohne verbundenes Fahrzeug](../pics/fe/StatusEVAmpelViewCharging.png)

Nach einer Status-Änderung (Ladebeginn, Ladeende) wird der Status nur dann korrekt angezeigt, wenn die für `Statuserkennung-Unterbrechung` konfigurierte Dauer (Standardwert: 300s) abgelaufen ist.
