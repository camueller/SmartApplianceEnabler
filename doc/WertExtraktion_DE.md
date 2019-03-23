# Wert-Extraktion mittels Regulärem Ausdruck

Bei Abfragen erhält der *Smart Appliance Enabler* oft eine umfangreiche Antwort (XML, JSON, ...), aus der der eigentlich benötigte Zahlenwert erst extrahiert werden muss. Dazu kann an diversen Stellen im *Smart Appliance Enabler* ein regulärer Ausdruck konfiguriert werden.
Am Beispiel der XML-Antwort eines Edimax SP2101W soll gezeigt werden, wie man mit dem Java-Regex-Tester https://www.freeformatter.com/java-regex-tester.html arbeitet, um einen funktionierenden Regulären Ausdruck zu bestimmen. Die kursiv dargestellten Bezeichnung beziehen sich auf die entsprechenden Felder der Regex-Tester-Seite.

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
