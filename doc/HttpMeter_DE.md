# HTTP-basierte Stromzähler

Richtige Stromzähler, deren Werte man per HTTP abfragen kann, gibt es meines Wissens nicht. Wenn man allerdings andere Hausautomatisierungen (z.B. FHEM) verwendet, kann man dort eingebundene Stromzähler via HTTP abfragen:
```
<Appliances ...>
    <Appliance ...>
        <HttpElectricityMeter url="http://192.168.1.1/control?device=123&amp;cmd=getpower" factorToWatt="1000" />
    </Appliance>
</Appliances>
```
Zu beachten ist, dass in der URL anstatt des "&"-Zeichens der Ausruck ```"&amp;"``` (ohne Anführungszeichen) verwendet werden muss!

Wenn nicht nur eine Zahl für den Verbrauchswert geliefert wird, sondern der Verbrauchswert irgendwo in einem Text (XML, JSON, ...) enthalten ist, muss im Parameter ```extractionRegex``` ein Regulärer Ausdruck angegeben werden. Dieser Reguläre Ausruck muss im Java-Style angegeben sein und als _Capture Group 1_ den Verbrauchswert liefern. Zum Testen des Regulären Ausdrucks eignet sich https://www.freeformatter.com/java-regex-tester.html, wobei:
- als _Replace with_ $1 eingetragen sein muss
- das Flag _Dotall_ gesetzt sein muss
Wird danach der _Replace First_-Button gedrückt, sollte als _String replacement result_ der extrahierte Zahlenwert angezeigt werden.

Falls der über HTTP geliefert Verbrauchswert nicht in Watt geliefert wird, muss über den Parameter ```factorToWatt``` der Faktor angegeben werden, mit dem der gelieferte Wert multipliziert werden muss, um den Verbrauch in Watt zu erhalten. Wird beispielsweise der Verbrauch in mW geliefert, muss ```factorToWatt="1000"``` angegeben werden.

Optional können die weiteren, folgenden Parameter gesetzt werden:
- measurementInterval in Sekunden (default=60) : Zeitraum, für den der durchschnittliche Verbrauch berechnet wird
- pollInterval in Sekunden (default=10) : die Zeit zwischen zwei Verbrauchsabfragen beim Zähler

Allgemeine Hinweise zu diesem Thema finden sich im Kapitel [Konfiguration](Configuration_DE.md).


## Beispiel Edimax SP2101W
An diesem Beispiel soll gezeigt werden, wie man mit dem Java-Regex-Tester https://www.freeformatter.com/java-regex-tester.html arbeitet. Die kursiv dargestellten Bezeichnung beziehen sich auf die entsprechenden Felder.

_Java Regular Expression_
```.*NowPower>(\d*.{0,1}\d+).*```


_Entry to test against_
```
<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax"><CMD id="get"><NOW_POWER><Device.System.Power.NowCurrent>0.2871</Device.System.Power.NowCurrent><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>
```

_Replace with (Optional)_
```$1```

_Flags_
```
[x] Dotall
```
Nach Klick auf ```REPLACE FIRST``` wird angezeigt:
```
Results
.matches() method: true
.lookingAt() method: true
String replacement result:
52.49
```

## Beispiel Stromverbrauchsmessung über Fritz!Box Home Automation
Die Fritz!Box erlaubt die Abfrage der über eine Steckdose entnommenen Leistung via HTTP. Die Konfiguration dafür würde wie folgt aussehen, wobei die Session ID (ain) über ein zwischengeschaltetes Script gesetzt werden müsste:
```
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <HttpElectricityMeter url="http://192.168.2.1/webservices/homeautoswitch.lua?ain=xxx&amp;switchcmd=getswitchpower" factorToWatt="1000" />
    </Appliance>
</Appliances>
```
