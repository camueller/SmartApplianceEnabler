# Konfiguration mittels Web-Browser
Am einfachsten erfolgt die Konfiguration über das Web-Frontent des *Smart Appliance Enabler*. Dazu muss man im Web-Browser lediglich eingeben ```http://raspi:8080/```, wobei *raspi* durch den Hostnamen oder die IP-Adresses des Raspberry Pi ersetzt werden muss.
Die Web-Oberfläche ist bewusst einfach und dennoch komfortabel gehalten, um diverse Endgeräte optimal zu unterstützen.

<img src="https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/WebFrontend_Schaltzeiten.png" width="870">
<img src="https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/WebFrontend_Schaltzeiten_Mobile.png" width="240">


# Konfiguration mittels XML-Dateien

## Vorgehensweise
Die Konfiguration besteht aus zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien, die sich im ```/app```-Verzeichnis befinden müssen:
* die Datei `Device2EM.xml` enthält Gerätebeschreibung für den EnergyManager
* die Datei `Appliances.xml` enthält die Gerätekonfiguration für den *Smart Appliance Enabler*
Die Groß-/Kleinschreibung der Dateinamen muss genau so sein, wie hier angegeben!

Die XML-Dateien kann man entweder auf dem Raspberry Pi bearbeiten oder man transferiert sie dazu auf den PC. Letzteres bietete sich insbesondere für größere Änderungen an, nach denen noch eine Überprüfung der Inhalte auf Gültigkeit erfolgen soll (siehe unten). Zum Transferieren der Dateien zwischen Raspberry Pi und PC kann man unter Linux ```scp``` verwenden, unter Windows gibt es ```WinSCP``` ([Video mit WinSCP Anleitung auf Deutsch](https://www.youtube.com/watch?v=z6yJDMjTdMg)).

### Device-ID / Appliance-ID
Die Verbindung zwischen den konfigurierten Geräten in der Datei `Device2EM.xml` und den Appliances in der Datei `Appliances.xml` ist die Appliance-ID (`<Appliance id="F-00000001-000000000001-00">`), die mit der Device-ID ( `<DeviceId>F-00000001-000000000001-00</DeviceId>`) des zugehörigen Gerätes übereinstimmen muss.

Der Aufbau der Device-IDs ist in der SEMP-Spezifikation vorgegeben. Für den *Smart Appliance Enabler* bedeutet das:
* F unverändert lassen ("local scope")
* 00000001 ersetzen durch einen 8-stelligen Wert, der den eigenen Bereich definiert, z.B. das Geburtsdatum in der Form 25021964 für den 25. Februar 1964
* 000000000001 für jedes verwaltete Gerät hochzählen bzw. eine individuelle 12-stellige Zahl verwenden
* 00 unverändert lassen (sub device id)
Die Device-IDs werden vom Sunny-Portal direkt verwendet, d.h. wenn jemand anderes bereits diese ID verwendet, kann das Gerät nicht im Sunny-Portal angelegt werden. Durch die Verwendung individueller Bestandteile wie Geburtsdatum sollte das Risiko dafür jedoch gering sein.

### Überprüfung der Dateien
Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden.
Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:
Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert. 
In das Fenster *XSD Input* muss der Inhalt (nicht die URL selbst!) der nachfolgenden URL kopiert werden:
* beim Prüfen von Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.1.5.xsd
* beim Prüfen von Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-1.1.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is valid.*. Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

## Planung der Gerätelaufzeiten
Zur Planung der Gerätelaufzeit können einem Gerät ein oder mehrere `Schedule` zugewiesen werden, in denen jeweils die Mindest- und Maximallaufzeit in Sekunden für die zugehörigen Schaltvorgänge festgelegt ist. 

Zu jedem Schedule gehört entweder ein `DayTimeframe` oder ein `ConsecutiveDaysTimeframe`.

Wenn ein Timeframe bereits begonnen hat, spielt es eine Rolle, wie schnell der Sunny Home Manager auf die neue Anforderung reagiert. Damit eine realistische Chance besteht, dass noch eine Einschaltung vor Ende des Timeframe erfolgt, wird der Timeframe nur dann an den Sunny Home Manager gemeldet, wenn es bis zum Ende des Timeframe noch länger als Mindestlaufzeit plus einer zusätzlichen Verarbeitungszeit für den Sunny Home Manager (default: 900 Sekunden) dauert. Dieser Wert kann durch einen Konfigurationsparameter geändert werden:
```
<Appliances>
  <Configuration param="TimeframeIntervalAdditionalRunningTime" value="600"/>
  <Appliance>
  ...
  </Appliance>
</Appliances>
```

### DayTimeframe
Ein `DayTimeframe` enthält Ein- und Ausschaltzeiten, die sich auf ein 24-Stunden-Interval beziehen (z.B. 8:00-13:00 oder auch 22:00-2:00) und auf bestimmte Wochentage beschränkt werden können. Nachfolgendes Beispiel zeigt ein Laufzeit von 3 Stunden zwischen 6 Uhr und 14 Uhr montags, samstags und sonntags.
```
<Appliances>
  <Appliance>
    <Schedule minRunningTime="10800" maxRunningTime="10800">
      <DayTimeframe>
        <Start hour="6" minute="0" second="0"/>
        <End hour="14" minute="0" second="0"/>
        <DayOfWeek>1</DayOfWeek>
        <DayOfWeek>6</DayOfWeek>
        <DayOfWeek>7</DayOfWeek>
      </DayTimeframe>
    </Schedule>
  </Appliance>
</Appliances>
```
Durch die Angabe von ```<DayOfWeek>8</DayOfWeek>``` wird angebenen, dass der DayTimeframe an Feiertagen gelten soll. Er hat Vorrang vor anderen DayTimeframe, die entsprechend des Wochentages gelten würden. Sobald mindestens ein DayTimeframe mit ```<DayOfWeek>8</DayOfWeek>``` konfiguriert ist, wird das nachfolgend beschriebene Feiertags-Handling aktiviert.

Die Feiertage werden aus der Datei ```Holidays-JJJJ.txt``` gelesen, wobei JJJJ durch die Jahreszahl ersetzt wird, d.h. die Feiertag für 2017 finden sich in der Datei ```Holidays-2017.txt```. Die Datei muss sich im gleichen Verzeichnis wie die Datei ```Appliances.xml``` befinden und ist wie folgt aufgebaut:
```
2017-01-01 Neujahrstag
2017-04-14 Karfreitag
2017-04-17 Ostermontag
2017-05-01 Tag der Arbeit
...
```
Sofern der Raspberry Zugang zum Internet hat, werden die Feiertage einmal jährlich im Internet abgefragt und in dieser Datei gespeichert. Wenn die Datei vorhanden ist (entweder von einem vorangegangenen Download oder weil sie manuell dort erstellt wurde), erfolgt keine Abfrage im Internet. Standardmäßig werden nur die bundesweiten Feiertage berücksichtigt. Durch Angabe des folgenden Konfigurationsparameters kann sowohl die URL des Dienstes geändert als auch bundeslandspezifische Feiertage berücksichtigt werden:
```
<Appliances>
  <Configuration param="Holidays.Url" value="http://feiertage.jarmedia.de/api/?jahr={0}&#038;nur_land=HE"/>
  <Appliance>
  ...
  </Appliance>
</Appliances>
```
HE steht dabei für Hessen, die Abkürzung für andere Bundesländer und die vollständige API-Dokumentation findet sich auf http://feiertage.jarmedia.de. Anstelle der Jahreszahl muss "{0}" (ohne Anführungszeichen) verwendet werden, was zum Ausführungszeitpunkt durch die aktuelle Jahreszahl erstzt wird. Außerdem muss statt des "&"-Zeichens der Ausruck ```"&#038;"```  (ohne Anführungszeichen) verwendet werden.

### ConsecutiveDaysTimeframe
Ein `ConsecutiveDaysTimeframe` erlaubt die Festlegung eines Intervals, der länger als 24 Stunden ist (z.B. Freitag 16:00 bis Sonntag 18:00). Das nachfolgende Beispiel zeigt eine Laufzeit von mindestens 10 Stunden und maximal 12 Stunden zwischen Freitag 16:00 und Sonntag 20:00.
```
<Appliances>
  <Appliance>
    <Schedule minRunningTime="36000" maxRunningTime="43200">
      <ConsecutiveDaysTimeframe>
        <Start dayOfWeek="5" hour="16" minute="0" second="0" />
        <End dayOfWeek="7" hour="20" minute="0" second="0" />
      </ConsecutiveDaysTimeframe>
    </Schedule>
  </Appliance>
</Appliances>
```
Der *Smart Appliance Enabler* meldet dem Sunny Home Manager den Geräte-Laufzeitbedarf für die nächsten 48 Stunden, damit er auf dieser Basis optimal planen kann.

# Konfiguration mittels REST (JSON)
Für die Konfiguration mittels Web-Frontend existieren entsprechend REST-Services, die auch unabhängig vom Web-Frontend verwendet werden können.

TODO: REST-Services hier dokumentieren inkl. Beispiel mit curl


## Setzen der Schedules mittels REST (XML)
Normalerweise werden die Schedules aus der Datei `Appliance.xml` gelesen. Es ist jedoch möglich, die Schedules via REST an den SAE zu übergeben. Dazu müssen der/die Schedules in einem Root-Element `Schedules` zusammengefasst werden, das an SAE unter Angabe der Appliance-ID übergeben wird:
```
/usr/bin/curl -s -X POST -d '<Schedules><Schedule minRunningTime="36000" maxRunningTime="43200"><ConsecutiveDaysTimeframe><Start dayOfWeek="5" hour="16" minute="0" second="0" /><End dayOfWeek="7" hour="20" minute="0" second="0" /></ConsecutiveDaysTimeframe></Schedule></Schedules>' --header 'Content-Type: application/xml' http://localhost:8080/sae/schedules?ApplianceId=F-00000001-000000000001-00
```
Im Log des SAE sollte sich danach folgendes finden:
```
2016-12-17 15:27:49,802 DEBUG [http-nio-8080-exec-1] d.a.s.w.SaeController [SaeController.java:29] F-00000001-000000000001-00: Received request to set 1 schedule(s)
2016-12-17 15:27:49,804 DEBUG [http-nio-8080-exec-1] d.a.s.a.RunningTimeMonitor [RunningTimeMonitor.java:58] F-00000001-000000000001-00: Configured schedule: 16:00:00.000[5]-20:00:00.000[7]/36000s/43200s
```
