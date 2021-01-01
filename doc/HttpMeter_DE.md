# HTTP-basierte Stromzähler

Für HTTP-basierte Stromzähler muss eine URL angegeben. Bei der Eingabe einer URL ist zu beachten, dass bei Angabe der URL bestimmte Sonderzeichen "encoded" angegeben werden müssen. Zum Beispiel muss anstatt des "&"-Zeichens der Ausruck ```"&amp;"``` (ohne Anführungszeichen) verwendet werden! Zum "encoden" von URLs kann https://coderstoolbox.net/string/#!encoding=url&action=encode&charset=us_ascii verwendet werden.

Falls erforderlich, können Benutzername und Passwort für eine __Basic Authentication__ angegeben werden.

Wenn nicht nur eine Zahl ohne weitere Zeichen für den Verbrauchswert geliefert wird, sondern der Verbrauchswert irgendwo in einem Text (XML, JSON, ...) enthalten ist, muss ein [Regulärer Ausdruck zum Extrahieren](WertExtraktion_DE.md) der Leistung mit angegeben werden. Dies gilt auch wenn die abgefragte URL nach dem Zahlenwert einen Zeilenumbruch (CR/LF) liefert, wie es häufig bei Hausautomationen wie zum Beispiel fhem der Fall ist. Hier als [Regulärer Ausdruck zum Extrahieren](WertExtraktion_DE.md)  dann einfach ```(\d+)``` angeben, um zu verhindern das in der [Log-Datei](Support.md#Log) ungewollte Zeilenumbrüche protokolliert werden.

Falls der über HTTP gelieferte Verbrauchswert nicht in Watt geliefert wird, muss ein ```Faktor zur Umrechnung in Watt``` angegeben werden, mit dem der gelieferte Wert multipliziert werden muss, um den Verbrauch in Watt zu erhalten. Wird beispielsweise der Verbrauch in mW geliefert, muss dieser Faktor mit dem Wert ```1000``` angegeben werden.

Mit dem ```Abfrage-Intervall``` kann festgelegt werden, in welchen Abständen die aktuelle Leistungsaufnahme bei der Datenquelle abgefragt wird.

Ausserdem kann ein ```Messinterval``` angegeben werden für die Durchschnittsberechnung der Leistungsaufnahme.

![HTTP-basierter Zähler](../pics/fe/HttpMeter.png)

## Log
Wird ein HTTP-Zähler für das Gerät `F-00000001-000000000005-00` verwendet, kann man die ermittelte Leistungsaufnahme im [Log](Logging_DE.md) mit folgendem Befehl anzeigen:

```console
sae@raspi:~ $ grep 'Http' /tmp/rolling-2021-01-01.log | grep F-00000001-000000000005-00
2021-01-01 09:42:50,472 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:107] F-00000001-000000000005-00: Sending GET request url=http://espressomaschine/cm?cmnd=Status%208
2021-01-01 09:42:50,516 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:168] F-00000001-000000000005-00: Response code is 200
2021-01-01 09:42:50,531 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:86] F-00000001-000000000005-00: url=http://espressomaschine/cm?cmnd=Status%208 httpMethod=GET data=null path=null
2021-01-01 09:42:50,532 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:89] F-00000001-000000000005-00: Response: {"StatusSNS":{"Time":"2021-01-01T09:42:50","ENERGY":{"TotalStartTime":"2019-08-18T10:55:03","Total":164.950,"Yesterday":0.482,"Today":0.124,"Power":1279,"ApparentPower":1481,"ReactivePower":747,"Factor":0.86,"Voltage":233,"Current":6.370}}}
2021-01-01 09:42:50,533 DEBUG [Timer-0] d.a.s.h.HttpHandler [HttpHandler.java:58] F-00000001-000000000005-00: value=1279.0 protocolHandlerValue={"StatusSNS":{"Time":"2021-01-01T09:42:50","ENERGY":{"TotalStartTime":"2019-08-18T10:55:03","Total":164.950,"Yesterday":0.482,"Today":0.124,"Power":1279,"ApparentPower":1481,"ReactivePower":747,"Factor":0.86,"Voltage":233,"Current":6.370}}} valueExtractionRegex=,.Power.:(\d+) extractedValue=1279
2021-01-01 09:42:55,632 DEBUG [http-nio-8080-exec-9] d.a.s.m.HttpElectricityMeter [HttpElectricityMeter.java:154] F-00000001-000000000005-00: average power = 1280W
2021-01-01 09:42:55,636 DEBUG [http-nio-8080-exec-9] d.a.s.m.HttpElectricityMeter [HttpElectricityMeter.java:154] F-00000001-000000000005-00: average power = 1280W
```
