# Wert-Extraktion 
Bei Abfragen erhält der *Smart Appliance Enabler* oft eine umfangreiche Antwort (XML, JSON, ...), aus welcher der eigentliche Zahlenwert erst extrahiert werden muss.

Dazu kann an diversen Stellen im *Smart Appliance Enabler* entweder ein (JSON-) Pfad (Feldname `Pfad für Extraktion`) oder ein regulärer Ausdruck (Feldname `Regex für Extraktion`) konfiguriert werden.

## Mit JSON-Pfad
Diese Methode zur Wert-Extraktion funktioniert nur, wenn die Antwort im JSON-Format vorliegt!

Ob eine Antwort im JSON-Format vorliegt, lässt sich recht gut an den geschweiften Klammern erkennen, welche deren Struktur bestimmen. 

Der JSON-Pfad lässt sich relativ einfach mit dem [JSON Path Finder](https://jsonpathfinder.com/) betimmen: Einfach das JSON auf der _linken Seite_ einfügen (kann aus dem Log entnommen werden, wenn der *Smart Appliance Enabler* bereits mit diesem Gerät kommuniziert). Danach kann man auf der _rechten Seite_ die Datenstruktur aufklappen und den gewünschten Wert selektieren. Der jeweilige JSON-Pfad (englisch: path) wird dann oberhalb angezeigt, beginnend mit `x`. Bei der Übernahme des Pfades in den *Smart Appliance Enabler* muss dieses `x` durch ein `$` ersetzt werden.

![JSON Path Finder](../pics/JsonPathFinder.png)

Für obiges Beispiel muss im *Smart Appliance Enabler* konfiguriert werden:

`Pfad für Extraktion`: `$.StatusSNS.ENERGY.Power`

Damit der *Smart Appliance Enabler* weiss, dass die Antwort als JSON interpretiert werden soll, muss ausserdem als `Format` `JSON` angegeben werden!

## Mit regulärem Ausdruck

Die Wert-Extraktion mit einem [regulären Ausdruck](http://www.regexe.de/hilfe.jsp) funktioniert immer. Allerdings erschliesst sich deren Formulierung nicht jedem sofort. 

Zum Testen, ob der gewählte reguläre Ausdruck den gewünschten Wert aus der Antwort extrahiert, eignet sich dieser [Java Regex-Tester](https://www.freeformatter.com/java-regex-tester.html).

Ausser dem regulären Ausdruck benötigt man die Antwort, aus welcher der Wert extrahiert werden soll. Wenn der *Smart Appliance Enabler* bereits mit diesem Gerät kommuniziert, kann dessen Antwort dem Log entnommen werden.

Die kursiv dargestellten Bezeichnungen beziehen sich auf die entsprechenden Felder der Regex-Tester-Seite.

_Java Regular Expression_: `.*"Power":(\d+).*`

_Entry to test against_:

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

Der Wert 26 wurde also erfolgreich mit obigem regulären Ausdruck aus der Antwort extrahiert.

Wenn ein regulärer Ausdruck konfiguriert wird, sollte im *Smart Appliance Enabler* als `Format` kein Wert konfiguriert werden!

Im *Smart Appliance Enabler* muss für dieses Beispiel konfiguriert werden:

`Regex für Extraktion`: `.*"Power":(\d+).*`

`Format`: leer lassen
