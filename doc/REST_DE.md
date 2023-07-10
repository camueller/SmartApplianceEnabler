# REST-Schnittstelle
Für die Konfiguration mittels Web-Frontend existieren entsprechende REST-Services. Diese können, wie die SEMP-Schnittstelle selbst, auch unabhängig vom Web-Frontend verwendet werden.

## Schalten eines Gerätes
Zum Einschalten eines Gerätes kann folgender Befehl verwendet werden, wobei die URL und die Device-ID (identisch mit Appliance-ID) anzupassen ist:

```bash
$ curl \
    -X POST \
    -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000002-00</DeviceId><On>true</On></DeviceControl></EM2Device>' \
    --header 'Content-Type: application/xml' \
    http://127.0.0.1:8080/semp
```

Zum Ausschalten muss lediglich `<On>false</On>` statt `<On>true</On>` gesetzt werden.

Ein Gerät, das manuell eingeschaltet wird bleibt nur dann in diesem Zustand wenn es sich in einem aktiven Timeframe befindet. Andernfalls schaltet es der *Smart Appliance Enabler* wieder aus.
Um vor dem Einschalten zusätzlich einen Timeframe für eine bestimmte Laufzeit zu erzeugen ist ein zweiter Befehl notwendig. Beide Befehle direkt hintereinander abgesetzt (zuerst Timeframe, dann Einschalten) bewirken das selbe was die Ampelfunktion zur Verfügung stellt (Klick für sofortigen Start + Auswahl der entsprechenden Laufzeit).
Zum aktivieren eines zusätzlichen Timeframe für 10 Minuten ab sofort funktioniert folgender Befehl:

```bash
$ curl \
    -s \
    -X PUT \
    -F id=F-00000001-000000000002-00 \
    -F runtime=600 \
    http://127.0.0.1:8080/sae/runtime
```

## Setzen der Schedules
Normalerweise werden die Schedules aus der Datei `Appliance.xml` gelesen. Es ist jedoch möglich, die Schedules via REST an den *Smart Appliance Enabler* zu übergeben. Dazu müssen der/die Schedules in einem Root-Element `Schedules` zusammengefasst werden, das an *Smart Appliance Enabler* unter Angabe der Appliance-ID übergeben wird:

```bash
$ curl \
    -s \
    -X POST \
    -d '<Schedules xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.4"><Schedule><RuntimeRequest min="1800" max="3600" /><DayTimeframe><Start hour="0" minute="0" second="0" /><End hour="18" minute="59" second="59" /></DayTimeframe></Schedule></Schedules>' \
    --header 'Content-Type: application/xml' \
    http://localhost:8080/sae/schedules?id=F-00000001-000000000001-00
```

Das `xmlns`-Attribut (insbesondere die Version des *Smart Appliance Enabler* am Ende) muss dabei übereinstimmen mit dem `xmlns`-Attribut in der Datei `Appliances.xml`.

Im Log des *Smart Appliance Enabler* sollte sich danach folgendes finden:

```bash
2020-01-12 18:31:10,606 DEBUG [http-nio-8080-exec-3] d.a.s.w.SaeController [SaeController.java:413] F-00000001-000000000099-00: Received request to activate 1 schedule(s)
2020-01-12 18:31:10,614 DEBUG [http-nio-8080-exec-3] d.a.s.a.RunningTimeMonitor [RunningTimeMonitor.java:82] F-00000001-000000000099-00: Using enabled time frame 00:00:00.000-18:59:59.000/1800s/3600s
```
