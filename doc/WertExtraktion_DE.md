# Wert-Extraktion mittels Regulärem Ausdruck

Bei Abfragen erhält der *Smart Appliance Enabler* oft eine umfangreiche Antwort (XML, JSON, ...), aus der der eigentlich benötigte Zahlenwert erst extrahiert werden muss.

Dazu kann an diversen Stellen im *Smart Appliance Enabler* ein regulärer Ausdruck konfiguriert werden.

Am Beispiel der Antwort eines [Adapters mit Tasmota-Firmware](Tasmota_DE.md) soll gezeigt werden, wie man mit dem Java-Regex-Tester https://www.freeformatter.com/java-regex-tester.html arbeitet, um einen funktionierenden Regulären Ausdruck zu bestimmen. Die kursiv dargestellten Bezeichnung beziehen sich auf die entsprechenden Felder der Regex-Tester-Seite.

_Java Regular Expression_: `.*"Power":(\d+).*`

_Entry to test against_
```
{"StatusSNS":{"Time":"2019-09-06T20:06:19","ENERGY":{"TotalStartTime":"2019-08-18T11:07:55","Total":0.003,"Yesterday":0.000,"Today":0.003,"Power":26,"ApparentPower":25,"ReactivePower":25,"Factor":0.06,"Voltage":239,"Current":0.106}}}
```

_Replace with (Optional)_: `$1`

_Flags_: `[x] Dotall`

Nach Klick auf `REPLACE FIRST` wird angezeigt:

```
Results
.matches() method: true
.lookingAt() method: true
String replacement result:
26
```

Die Leistung von 26W wurde also erfolgreich mit obigem Regulären Ausdruck aus der Antwort der Tasmota-Firmware extrahiert. Dieser Ausdruck enthält `"`-Zeichen - deshalb unbedingt beachten:
