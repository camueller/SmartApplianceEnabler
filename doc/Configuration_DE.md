# Konfiguration

## Dateien
Die Konfiguration besteht aus zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien:
* die Datei `Device2EM.xml` enthält Gerätebeschreibung für den EnergyManager
* die Datei `Appliances.xml` enthält die Gerätekonfiguration für den *Smart Appliance Enabler*

Die Verbindung zwischen den konfigurierten Geräten in der Datei `Device2EM.xml` und den Appliances in der Datei `Appliances.xml` ist die Appliance-ID (`<Appliance id="F-00000001-000000000001-00">`), die mit der Device-ID ( `<DeviceId>F-00000001-000000000001-00</DeviceId>`) des zugehörigen Gerätes übereinstimmen muss.

Der Aufbau der Device-IDs ist in der SEMP-Spezifikation vorgegeben. Für den *Smart Appliance Enabler* bedeutet das:
* F unverändert lassen ("local scope")
* 00000001 ersetzen durch einen 8-stelligen Wert, der den eigenen Bereich definiert, z.B. das Geburtsdatum in der Form 25021964 für den 25. Februar 1964
* 000000000001 für jedes verwaltete Gerät hochzählen bzw. eine individuelle 12-stellige Zahl verwenden
* 00 unverändert lassen (sub device id)
Die Device-IDs werden vom Sunny-Portal direkt verwendet, d.h. wenn jemand anderes bereits diese ID verwendet, kann das Gerät nicht im Sunny-Portal angelegt werden. Durch die Verwendung individueller Bestandteile wie Geburtsdatum sollte das Risiko dafür jedoch gering sein.

Im Verzeichnis `example` finden sich Beispieldateien mit Kommentaren zu den einzelnen Angaben.
Diese sollen dabei helfen, die für die eigenen Geräte passenden Dateien `Device2EM.xml` und `Appliances.xml` (mit genau diesen Namen und entsprechender Groß-/Kleinschreibung!) zu erstellen.

## Überprüfung der Dateien
Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden.
Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:
Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert. Bei *XSD Input* muss nur *Option 2* eingegeben werden:
* beim Prüfen von Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.1.5.xsd
* beim Prüfen von Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-1.1.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is fully valid.*. Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

## Planung der Gerätelaufzeiten
Zur Planung der Gerätelaufzeit können einem Gerät ein oder mehrere `schedule` zugewiesen werden, in denen jeweils die Mindest- und Maximallaufzeit in Sekunden für die zugehörigen Schaltvorgänge festgelegt ist.

Zu jedem Schedule gehört entweder ein `DayTimeframe` oder ein `ConsecutiveDaysTimeframe`.

Ein `DayTimeframe` enthält Ein- und Ausschaltzeiten, die sich auf ein 24-Stunden-Interval beziehen (z.B. 8:00-13:00 oder auch 22:00-2:00) und auf bestimmte Wochentage beschränkt werden können. Nachfolgendes Beispiel zeigt ein Laufzeit von 3 Stunden zwischen 6 Uhr und 14 Uhr montags, samstags und sonntags.
```
<Schedule minRunningTime="10800" maxRunningTime="10800">
  <DayTimeframe>
    <Start hour="6" minute="0" second="0"/>
    <End hour="14" minute="0" second="0"/>
    <DayOfWeek>1</DayOfWeek>
    <DayOfWeek>6</DayOfWeek>
    <DayOfWeek>7</DayOfWeek>
  </DayTimeframe>
</Schedule>
```

Ein `ConsecutiveDaysTimeframe` erlaubt die Festlegung eines Intervals, der länger als 24 Stunden ist (z.B. Freitag 16:00 bis Sonntag 18:00). Das nachfolgende Beispiel zeigt eine Laufzeit von mindestens 10 Stunden und maximal 12 Stunden zwischen Freitag 16:00 und Sonntag 20:00.
```
<Schedule minRunningTime="36000" maxRunningTime="43200">
  <ConsecutiveDaysTimeframe>
    <Start dayOfWeek="5" hour="16" minute="0" second="0" />
    <End dayOfWeek="7" hour="20" minute="0" second="0" />
  </ConsecutiveDaysTimeframe>
</Schedule>
```
Der *Smart Appliance Enabler* meldet dem Sunny Home Manager den Geräte-Laufzeitbedarf für die nächsten 48 Stunden, damit er auf dieser Basis optimal planen kann.

## Setzen der Schedules via REST
Normalerweise werden die Schedules aus der Datei `Appliance.xml` gelesen. Es ist jedoch möglich, die Schedules via REST an den SAE zu übergeben. Dazu müssen der/die Schedules in einem Root-Element `Schedules` zusammengefasst werden, das an SAE unter Angabe der Appliance-ID übergeben wird:
```
/usr/bin/curl -s -X POST -d '<Schedules><Schedule minRunningTime="36000" maxRunningTime="43200"><ConsecutiveDaysTimeframe><Start dayOfWeek="5" hour="16" minute="0" second="0" /><End dayOfWeek="7" hour="20" minute="0" second="0" /></ConsecutiveDaysTimeframe></Schedule></Schedules>' --header 'Content-Type: application/xml' http://localhost:8080/sae/schedules?ApplianceId=F-00000001-000000000001-00
```
Im Log des SAE sollte sich danach folgendes finden:
```
2016-12-17 15:27:49,802 DEBUG [http-nio-8080-exec-1] d.a.s.w.SaeController [SaeController.java:29] F-00000001-000000000001-00: Received request to set 1 schedule(s)
2016-12-17 15:27:49,804 DEBUG [http-nio-8080-exec-1] d.a.s.a.RunningTimeMonitor [RunningTimeMonitor.java:58] F-00000001-000000000001-00: Configured time frame is 16:00:00.000[5]-20:00:00.000[7]36000s/43200s
```
