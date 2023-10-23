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

Zum Testen, ob der gewählte reguläre Ausdruck den gewünschten Wert aus der Antwort extrahiert, eignet sich die Webseite [RegEx101](https://regex101.com/). Der Vorteil dieses Testers ist, dass er direkt während der Eingabe evaluiert, es muss also nicht nach jeder Änderung auf einen Button geklickt und die Übertragung des Ergebnisses gewartet werden.

Ausser dem regulären Ausdruck benötigt man die Antwort, aus welcher der Wert extrahiert werden soll. Wenn der *Smart Appliance Enabler* bereits mit diesem Gerät kommuniziert, kann dessen Antwort dem Log entnommen werden.

**Wenn ein regulärer Ausdruck konfiguriert wird, sollte im *Smart Appliance Enabler* als `Format` kein Wert konfiguriert werden!**

Liegen die auszuwertenden Daten als Key-Value-Paare vor, so muss beachtet werden, dass der reguläre Ausdruck auf die gesamte Zeichenkette zur Anwendung kommt! Also nicht nur auf das ermittelte Value. Auch das kann mit dem [RegEx101](https://regex101.com/) gegrüft werden.

Im folgenden Beispiel liegt der Zählerstand für die Wärmepumpe als Key-Value-Paar vor:

![MeteringKeyValueExample](../pics/MeteringKeyValueExample.png)

Damit ist der reguläre Ausdruck sowie der Test-String für die Wärmepumpe wie folgt:

_Regular Expression_: `(\d+.?\d*)`

_Test String_: `waermepumpe=235.419998`

![RegEx101-Example](../pics/RegEx101-Example.png)

Der Wert `235.419998` wurde also erfolgreich mit obigem regulären Ausdruck aus der Antwort extrahiert.

Im *Smart Appliance Enabler* muss also der folgende reguläre Ausdruck konfiguriert werden:

`Regex für Extraktion`: `(\d+.?\d*)`

`Format`: leer lassen
