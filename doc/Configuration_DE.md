# Konfiguration

Die Konfiguration erfolgt über das Web-Frontend des *Smart Appliance Enabler*. Dazu muss man im Web-Browser lediglich eingeben ```http://raspi:8080/```, wobei *raspi* durch den Hostnamen oder die IP-Adresse des Raspberry Pi ersetzt werden muss. Es öffnet sich die Status-Seite mit dem Hinweis, dass noch keine Geräte konfiguert sind.

Die Web-Oberfläche ist bewusst einfach und dennoch komfortabel gehalten, um Browser auf PC, Tablett und Handy gleichermaßen zu unterstützen.

Grundsätzlich gilt, dass Eingaben/Änderungen erst nach dem Klicken der ```Speichern```-Schaltfläche gespeichert sind. Beim Wechsel auf ein andere Seite erfolgt eine Warnung, wenn nicht gespeicherte Eingaben/Änderungen vorhanden sind. Werden bei Eingabefeldern Inhalte mit grauer Schrift angezeigt, so handelt es sich um Voreinstellungen, d.h. wenn kein Wert eingegeben wird, gilt dieser Wert.

Beim Speichern werden die Daten in zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien geschrieben, die sich in dem Verzeichnis befinden, auf das die Variable SAE_HOME verweist (normalerweise ```/app```):
* die Datei `Device2EM.xml` enthält Gerätebeschreibung für den EnergyManager
* die Datei `Appliances.xml` enthält die Gerätekonfiguration für den *Smart Appliance Enabler*
Die Groß-/Kleinschreibung der Dateinamen muss genau so sein, wie hier angegeben!

Beim ersten Start ohne vorhandene Konfigurationsdateien wird folgende Seite angezeigt:

![Ohne Konfiguration](../pics/fe/OhneKonfiguration.png)

Im oberen Bereich der Seite findet sich der Menüpunkt zur Verwaltung der zentralen [Einstellungen](Settings_DE.md).

Das Seitenmenü zur Konfiguration der Geräte, Zähler, Schalter und Schaltzeiten ist nicht sichtbar und muss durch Klick auf ```Geräte``` geöffnet werden. Jetzt sieht die Seite wie folgt aus:

![Ohne Konfiguration Seitenmenü](../pics/fe/OhneKonfigurationSeitenmenu.png)

Durch Klick auf ```Neues Gerät``` wird die Konfiguration eines neuen Geräte begonnen und es öffnet sich folgende Seite:

![Neues Gerät](../pics/fe/NeuesGeraet.png)

## Gerätekonfiguration

Ein sehr wichtiges Attribut der Gerätekonfiguration ist die ```ID```. Der Aufbau der Device-ID ist in der SEMP-Spezifikation vorgegeben. Für den *Smart Appliance Enabler* bedeutet das:
* F unverändert lassen ("local scope")
* 00000001 ersetzen durch einen 8-stelligen Wert, der den eigenen Bereich definiert, z.B. das Geburtsdatum in der Form 25021964 für den 25. Februar 1964
* 000000000001 für jedes verwaltete Gerät hochzählen bzw. eine individuelle 12-stellige Zahl verwenden
* 00 unverändert lassen (sub device id)
Die Device-IDs werden vom Sunny-Portal direkt verwendet, d.h. wenn jemand anderes bereits diese ID verwendet, kann das Gerät nicht im Sunny-Portal angelegt werden. Durch die Verwendung individueller Bestandteile wie Geburtsdatum sollte das Risiko dafür jedoch gering sein.

Außer der Device-ID müssen allgemeine Angaben und Eigenschaften des Gerätes eingegeben werden. Wenn alle erforderlichen Eingaben erfolgt sind, wird die ```Speichern```-Schaltfläche freigegeben. Erst nach dem Drücken dieser Schaltfläche erscheinen im Seitenmenü die Unterpunkte ```Zähler``` und ```Schalter```.

Die Konfiguration eines gespeicherten Gerätes kann man durch auf den Geräte-Typ und -Namen im Seitenmenü ansehen und ändern.

![Gerät](../pics/fe/Geraet.png)

Das Ändern der ```ID``` führt alledings dazu, dass der SHM das Gerät als Neugerät betrachtet.

Sobald Änderungen vorgenommen wurden und die Korrektheit der Eingaben überprüft wurde, wird wiederum die ```Speichern```-Schaltfläche freigegeben und die Änderungen können gespeichert werden. Außerdem kann das Gerät durch Klicken der ```Löschen```-Schaltfläche gelöscht werden.

Aktuell unterstützt der Sunny Home Manager maximal 12 Geräte. Zur Gesamtzahl zählen neben den Geräten, die mit dem SEMP Protokoll angesprochen werden, auch z.B. SMA Bluetooth Funksteckdosen - also alle Geräte, die im Sunny Portal in der Verbraucherübersicht angezeigt werden. Wird die zulässige Höchstzahl überschritten, wird das neu angelegte Gerät im Sunny Portal angezeigt, kann jedoch nicht hinzugefügt werden (gelbes Ausrufezeichen, Fehlermeldung "Weder eine Erweiterung noch ein Austausch ist in dieser Geräteklasse möglich").
Geräte die zu viel oder falsch im SAE hinzugefügt wurden verschwinden nach dem Löschen im SAE erst nach ein paar Wochen automatisch aus dem Sunny Portal. Es ist aktuell keine Möglichkeit bekannt diesen Vorgang zu beschleunigen.

## Zähler

Um einen Zähler zu konfigurieren muss in der ```Typ```-Auswahlbox der Typ des Zählers ausgewählt werden. Entsprechend dieser Auswahl werden die für den gewählten Zähler-Typ konfigurierbaren Felder eingeblendet.

Derzeit unterstützt der *Smart Appliance Enabler* Zähler mit folgenden Protokollen:

* [S0](SOMeter_DE.md)
* [Modbus](ModbusMeter_DE.md)
* [HTTP](HttpMeter_DE.md)

Nachdem alle erforderlichen Eingaben erfolgt sind, wird die ```Speichern```-Schaltfläche freigegeben.

## Schalter

Um einen Schalter zu konfigurieren muss in der ```Typ```-Auswahlbox der Typ des Schalters ausgewählt werden. Entsprechend dieser Auswahl werden die für den gewählten Schalter-Typ konfigurierbaren Felder eingeblendet.
Falls der gewählte Schalter-Typ mit [Anlaufstromerkennung](Anlaufstromerkennung_DE.md) kombiniert werden kann und diese durch Anklicken der Checkbox aktiviert wurde, werden weitere Felder mit Konfigurationsparametern der Anlaufstromerkennung eingeblendet.

Derzeit unterstützt der *Smart Appliance Enabler* Schalter mit folgenden Protokollen:

* [GPIO](GPIOSwitch_DE.md)
* [Modbus](ModbusSwitch_DE.md)
* [HTTP](HttpSwitch_DE.md)

Ein besonderer Schalter-Typ ist ```Immer eingeschaltet```. Dieser eignet sich für Geräte, die immer eingeschaltet sind (z.B. Kühlschrank) und bei denen lediglich der Verbrauch überwacht werden soll. Die Konfiguration dieses Schalters ist erforderlich, weil nur Verbräuche eingeschalteter Geräte berücksichtigt werden.

Nachdem alle erforderlichen Eingaben erfolgt sind, wird die ```Speichern```-Schaltfläche freigegeben. Falls ein Schalter konfiguriert wurden, erscheint nach dem Drücken dieser Schaltfläche erscheinen im Seitenmenü der Unterpunkt ```Schaltzeiten```.

Wenn als Gerätetyp ```Elektroauto-Ladegerät``` angegeben ist, kann auf dieser Seite die [Konfiguration des Lade-Controllers sowie die Verwaltung der Fahrzeuge](EVCharger_DE.md) vorgenommen werden.

## Zeitpläne

Wenn ein Gerät schaltbar ist, können Zeitpläne konfiguriert werden. Ein Zeitplan hat einen Zeitrahmen, auf den er sich bezieht. Das kann entweder ein ```Tagesplan``` (bezieht sich auf Kalendertag) oder ein ```Mehrtagesplan``` (dauert länger als 24 Stunden aber höchstens eine Woche) sein.

Für einen Tagesplan kann angegeben werden, dass dieser an Feiertagen gelten soll.
Diese hat Vorrang vor anderen Tagesplänen, die entsprechend des Wochentages gelten würden. Voraussetzung dafür ist, dass das [Feiertagshandling in der Konfiguration aktiviert](Settings_DE.md#Feiertage) wurde.

Neben dem Zeitrahmen hat ein Zeitplan auch eine ```Anforderungsart```, die (abhängig vom Geräte-Typ) meist ```Laufzeit``` ist. In diesem Fall bringt der Zeitplan eine bestimme minimale und/oder maximale gewünschte Laufzeit zum Ausdruck. Wenn die minimale Laufzeit auf ```0``` gesetzt wird und die maximale Laufzeit auf einen größeren Wert, wird damit zum Ausdruck gebracht, dass dieses Gerät *Überschussenenergie* aufnehmen kann bzw. soll. Wenn die minimale Laufzeit auf ```0``` gesetzt wird muss die maximale Laufzeit auf einen Wert größer ```0``` gesetzt werden, ansonsten ignoriert der Sunny Home Manager diesen Zeitplan.

![Schaltzeiten Tagesplan](../pics/fe/SchaltzeitenTagesplanLaufzeit.png)
Schaltzeiten mit Tagesplan

![Schaltzeiten Mehrtagesplan](../pics/fe/SchaltzeitenMehrtagesplanLaufzeit.png)
Schaltzeiten mit Mehrtagesplan

Der *Smart Appliance Enabler* meldet dem Sunny Home Manager den Geräte-Laufzeitbedarf für die nächsten 48 Stunden, damit er auf dieser Basis optimal planen kann.

# Konfiguration mittels XML-Dateien

## Vorgehensweise

Die XML-Dateien kann man entweder auf dem Raspberry Pi bearbeiten oder man transferiert sie dazu auf den PC. Letzteres bietete sich insbesondere für größere Änderungen an, nach denen noch eine Überprüfung der Inhalte auf Gültigkeit erfolgen soll (siehe unten). Zum Transferieren der Dateien zwischen Raspberry Pi und PC kann man unter Linux ```scp``` verwenden, unter Windows gibt es ```WinSCP``` ([Video mit WinSCP Anleitung auf Deutsch](https://www.youtube.com/watch?v=z6yJDMjTdMg)).

## Überprüfung der Dateien
Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden.
Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:
Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert. 
In das Fenster *XSD Input* muss der Inhalt (nicht die URL selbst!) der nachfolgenden URL kopiert werden:
* beim Prüfen von Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.1.5.xsd
* beim Prüfen von Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-1.3.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is valid.*. Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

## Konfiguration mittels REST
Für die Konfiguration mittels Web-Frontend existieren entsprechende REST-Services. Diese können, wie die SEMP-Schnittstelle selbst, auch unabhängig vom Web-Frontend verwendet werden.

### Schalten eines Gerätes
Zum Einschalten eines Gerätes kann folgender Befehl verwendet werden, wobei die URL un die Device-ID (identisch mit Appliance-ID) anzupassen ist:
```
curl -X POST -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000002-00</DeviceId><On>true</On></DeviceControl></EM2Device>' --header 'Content-Type: application/xml' http://127.0.0.1:8080/semp
```
Zum Ausschalten muss lediglilch ```<On>false</On>``` statt ```<On>true</On>``` gesetzt werden.

### Setzen der Schedules
Normalerweise werden die Schedules aus der Datei `Appliance.xml` gelesen. Es ist jedoch möglich, die Schedules via REST an den SAE zu übergeben. Dazu müssen der/die Schedules in einem Root-Element `Schedules` zusammengefasst werden, das an SAE unter Angabe der Appliance-ID übergeben wird:
```
/usr/bin/curl -s -X POST -d '<Schedules><Schedule minRunningTime="36000" maxRunningTime="43200"><ConsecutiveDaysTimeframe><Start dayOfWeek="5" hour="16" minute="0" second="0" /><End dayOfWeek="7" hour="20" minute="0" second="0" /></ConsecutiveDaysTimeframe></Schedule></Schedules>' --header 'Content-Type: application/xml' http://localhost:8080/sae/schedules?ApplianceId=F-00000001-000000000001-00
```
Im Log des SAE sollte sich danach folgendes finden:
```
2016-12-17 15:27:49,802 DEBUG [http-nio-8080-exec-1] d.a.s.w.SaeController [SaeController.java:29] F-00000001-000000000001-00: Received request to set 1 schedule(s)
2016-12-17 15:27:49,804 DEBUG [http-nio-8080-exec-1] d.a.s.a.RunningTimeMonitor [RunningTimeMonitor.java:58] F-00000001-000000000001-00: Configured schedule: 16:00:00.000[5]-20:00:00.000[7]/36000s/43200s
```
