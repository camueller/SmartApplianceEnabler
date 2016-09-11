# Konfiguration

Die Konfiguration besteht aus zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien:
* die Datei `Device2EM.xml` enthält Gerätebeschreibung für den EnergyManager
* die Datei `Appliances.xml` enthält die Gerätekonfiguration für den Raspberry Pi

Die Verbindung zwischen den konfigurierten Geräten in der Datei `Device2EM.xml` und den Appliances in der Datei `Appliances.xml` ist die Appliance-ID (`<Appliance id="F-00000001-000000000001-00">`), die mit der Device-ID ( `<DeviceId>F-00000001-000000000001-00</DeviceId>`) des zugehörigen Gerätes übereinstimmen muss.

Der Aufbau der Device-IDs ist in der SEMP-Spezifikation vorgegeben. Für den *Smart Appliance Enabler* bedeutet das:
* F unverändert lassen ("local scope")
* 00000001 ersetzen durch einen 8-stelligen Wert, der den eigenen Bereich definiert, z.B. das Geburtsdatum in der Form 25021964 für den 25. Februar 1964
* 000000000001 für jedes verwaltete Gerät hochzählen bzw. eine individuelle 12-stellige Zahl verwenden
* 00 unverändert lassen (sub device id)
Die Device-IDs werden vom Sunny-Portal direkt verwendet, d.h. wenn jemand anderes bereits diese ID verwendet, kann das Gerät nicht im Sunny-Portal angelegt werden. Durch die Verwendung individueller Bestandteile wie Geburtsdatum sollte das Risiko dafür jedoch gering sein.

Im Verzeichnis `example` finden sich Beispieldateien mit Kommentaren zu den einzelnen Angaben.
Diese sollen dabei helfen, die für die eigenen Geräte passenden Dateien `Device2EM.xml` und `Appliances.xml` (mit genau diesen Namen und entsprechender Groß-/Kleinschreibung!) zu erstellen.

Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden.
Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:
Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert. Bei *XSD Input* muss nur *Option 2* eingegeben werden:
* beim Prüfen von Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.1.5.xsd
* beim Prüfen von Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-1.0.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is fully valid.*. Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

