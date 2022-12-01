# Wert-Extraktion 
Bei Abfragen erhält der *Smart Appliance Enabler* oft eine umfangreiche Antwort (XML, JSON, ...), aus welcher der eigentlich benötigte Zahlenwert erst extrahiert werden muss.

Dazu kann an diversen Stellen im *Smart Appliance Enabler* entweder ein (JSON-) Pfad (Feldname `Pfad für Extraktion`) oder ein regulärer Ausdruck (Feldname `Regex für Extraktion`) konfiguriert werden.

## Mit JSON-Pfad
Diese Methode zur Wert-Extraktion funktioniert nur, wenn die Antwort im JSON-Format vorliegt!

Ob eine Antwort im JSON-Format vorliegt lässt sich relativ einfach an den geschweiften Klammern erkennen, welche deren Struktur bestimmen. 

Zur Bestimmung des JSON-Pfaded ist es hilfreich, wenn man die JSON-Antwort so formatiert, dass die Struktur erkennbar ist. Dazu eignet sich beispielsweise [JSON Pretty Print](https://jsonformatter.org/json-pretty-print), bei dem man in der linken Browser-Hälfte die unformatierte JSON-Antwort einträgt (kann aus dem Log entnommen werden, wenn der *Smart Appliance Enabler* bereits mit diesem Gerät kommuniziert):
```json
{"StatusSNS":{"Time":"2019-09-06T20:06:19","ENERGY":{"TotalStartTime":"2019-08-18T11:07:55","Total":0.003,"Yesterday":0.000,"Today":0.003,"Power":26,"ApparentPower":25,"ReactivePower":25,"Factor":0.06,"Voltage":239,"Current":0.106}}}
```
Wenn man danach auf `Make Pretty` drückt, erhält man in der rechten Browser-Hälfte das schön formatierte JSON:
```json
{
  "StatusSNS": {
    "Time": "2019-09-06T20:06:19",
    "ENERGY": {
      "TotalStartTime": "2019-08-18T11:07:55",
      "Total": 0.003,
      "Yesterday": 0,
      "Today": 0.003,
      "Power": 26,
      "ApparentPower": 25,
      "ReactivePower": 25,
      "Factor": 0.06,
      "Voltage": 239,
      "Current": 0.106
    }
  }
}
```
Für den Pfad zum gewünschten Wert ist jeweils jeder letzte Eintrag relevant, bevor die Einrücktiefe zunimmt. Diese Einträge werden aneinandergereiht und durch einen Punkt getrennt. Am Ende dieses Pfades muss der Name stehen, dessen Wert extrahiert werden soll.

Für den Wert "Power" ergibt sich der Pfad entsprechend als:
`StatusSNS.ENERGY.Power`

Bei der Konfiguration des Pfades im *Smart Appliance Enabler* muss diesem Pfad noch ein `$.` vorangestellt werden, d.h. für obiges Beispiel muss im *Smart Appliance Enabler* konfiguriert werden:

`Pfad für Extraktion`: `$.StatusSNS.ENERGY.Power`

Damit der *Smart Appliance Enabler* weiss, dass die Antwort als JSON interpretiert werden soll, muss ausserdem als `Format` `JSON` angegeben werden!

## Mit Regulärem Ausdruck

Die Wert-Extraktion mit einem [Regulären Ausdruck](http://www.regexe.de/hilfe.jsp) funktioniert immer. Allerdings erschliesst sich deren Formulierung nicht jedem sofort. 

Zum Testen, ob der gewählte Reguläre Ausdruck den gewünschten Wert aus der Antwort extrahiert, eignet sich dieser [Java Regex-Tester](https://www.freeformatter.com/java-regex-tester.html).

Ausser dem Regulären Ausdruck benötigt man die Antwort, aus welcher der Wert extrahiert werden soll. Wenn der *Smart Appliance Enabler* bereits mit diesem Gerät kommuniziert, kann dessen Antwort dem Log entnommen werden.

Die kursiv dargestellten Bezeichnung beziehen sich auf die entsprechenden Felder der Regex-Tester-Seite.

_Java Regular Expression_: `.*"Power":(\d+).*`

_Entry to test against_
```json
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

Der Wert 26 wurde also erfolgreich mit obigem Regulären Ausdruck aus der Antwort extrahiert.

Wenn ein Regulärer Ausdruck konfiguriert wird, sollte im *Smart Appliance Enabler* als `Format` kein Wert konfiguriert werden!

Im *Smart Appliance Enabler* muss für dieses Beispiel konfiguriert werden:

`Regex für Extraktion`: `.*"Power":(\d+).*`

`Format`: leer lassen
