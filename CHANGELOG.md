# Changelog

All wesentlichen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
und das Projekt folgt den Leitlinien des [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.9](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.9) - 17.01.2021

### Neu
- im Menü finden sich Links zur Fragen&Antworen, zum Forum und zur Projekt-Homepage
- an Eingabefeldern für Shellscripts (derzeit Script für Benachrichtigungen und SOC-Script) wird angezeigt, ob das Shell-Script existiert und ausführbar ist. Andernfalls wird eine Fehlermeldung angezeigt und das Speichern ist nicht möglich
- in der Web-Oberfläche wird (neben dem Titel) auf das Vorhandensein neuer Versionen hingewiesen mit direktem Link zur neuen Version
- stark vereinfachter Installationsprozess
- Projekt-Homepage inkl. Screenshots und Video wurde komplett aktualisert

### Geändert
- Nach dem Abbrechen einer Sofortladung durch Klick auf die rote Ampel war es nicht möglich, den SOC für Überschussladen wirksam zu ändern. Das ist jetzt möglich.

## [1.6.8](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.8) - 04.01.2021

### Neu
- Geräte mit Tasmota-Firmware können via Tastendruck eine Laufzeitanforderung stellen - siehe [#111](https://github.com/camueller/SmartApplianceEnabler/issues/111)
- Auf der Projekt-Homepage findet sich jetzt auch ein Video der Web-Oberfläche "in Action", dass regelmässig im Rahmen der automatsierten Tests entsteht. Ich habe es dort verlinkt, damit sich Neu-User einen besseren Eindruck von der Web-Oberfläche verschaffen können.

### Geändert
- das grüne Ampellicht blinkt nicht nur, wenn Wallboxen Überschussenergie laden, sodern auch bei allen anderen Geräte, sobald diese Überschussenergie verwenden - siehe [#107](https://github.com/camueller/SmartApplianceEnabler/issues/107)
- vor der Ausführung eines SOC-Scripts wird geprüft, ob dieses vorhanden und ausführbar ist; falls nicht, werden hilfereiche Meldungen geloggt
- ein regulärer Ausdruck für ein SOC-Script kann wieder gelöscht werden, ohne dass das als leere Regex interpretiert wird
- bei mehrmaligem Verbinden des Fahrzeugs mit der Wallbox wurde der Zeitpunkt des erstmaligen Verbindens angezeigt; jetzt immer der Zeitpunkt des letzten Verbindens
- auf allen Seiten des SAE findet sich mindestens ein rotes Fragezeichen-Symbol, das beim Anklicken direkt auf die entsprechende Seite der SAE-Dokumentation führt. Dazu habe ich die gesamte Dokumentation umstrukturiert, konsolidiert und aktualisiert. Auch die Screenshots sind jetzt aktuell!
- für Walboxen konnten die URLs des zugehörigen Zählers nicht gelöscht werden, sobald beide URLs eingegeben waren
- die Eingabe von Zeichen wie "<> in die Eingabefelder der Web-Oberfläche konnte dazu führen, dass die `Appliances.xml` ungültig wird. Ab sofort werden solche Zeichen automatisch "escaped", d.h. der Anwender muss sich darüber keine Gedanken machen (entsprechende Hinweise wurden aus der Dokumentation entfernt)
- an das Shell-Script, dass für Benachrichtigungen aufgerufen wird, werden jetzt neben der ID auch Name, Hersteller, Typ und Seriennummer übergeben. Falls jemand bereits die Benachrichtigungen per Telegram verwendet muss das aktualisierte Script verwendet werden!

## [1.6.7](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.7) - 26.12.2020

### Neu
- Für Ereignisse ("Einschalten", "Ausschalten", "Kommunikationsstörung", ...) können Benachrichtigungen versendet werden (siehe doc/Configuration_DE.md#benachrichtigungen )
- Falls einem Gerät kein Schalter zugewiesen ist, wird automatisch der neue Schaltertyp "Zählerbasierter Zustandsmelder") zugewiesen. Dieser meldet "eingeschaltet", wenn die Leistungsaufnahme oberhalb des Schwellwertes (Standard: 10W) liegt, ansonsten "ausgeschaltet". Für taktende Geräte (typischerweise solche, bei denen eine Temperatur gehalten werden muss), kann eine Ausschaltverzögerung konfuguriert werden, damit das Takten nicht erfasst wird (siehe doc/MeterReportingSwitch_DE.md )
- Es gibt eine neue Schema-Datei für die `Appliances.xml`, welche alle Neuerungen der bisherigen 1.6er-Versionen reflektiert. Die Migration der aktuellen Appliances.xml erfolgt automatisch. Allerdings ist die migrierte Datei nicht mehr mit bisherigen Versionen verwendbar - also ggf. vorher sichern.

### Geändert
- Nachdem kürzlich meine Änderungen an der Web-Oberfläche erst nach "Neu laden" sichtbar wurden, habe ich mir das Caching-Verhalten der Web-Anwendung genauer angesehen und gefixt. Der Fix wirkt aber erst ab dieser Version, d.h. falls Inhalte nicht oder nicht richtig angezeigt werden, müßt Ihr noch einmal "Neu laden" (CTRL-F5) drücken.
- Bei meinen Tests fiel mir auf, dass es beim Speichern von Änderungen immer Fehler gab, falls im SAE S0-Zähler oder GPIO-Schalter konfiguriert sind. Der Fehler hat dazu geführt, dass der Zähler/Schalter nicht mehr funktioniert bis zum Neustart des SAE. Das wurde gefixt.
- Wenn der SHM den SAE abfragt, während er gerade startet (das ist auch der Fall nach dem Speichern einer Konfigurationsänderung in der Web-Oberfläche), kam es zu Fehlern. Jetzt wird dem SHM währenddessen signalisiert, dass der SAE noch nicht läuft (HTTP 503).
- Bei Wallboxen wird das gelbe Ampellicht nicht mehr im Kontext des Überschußladens verwendet

## [1.6.6](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.6) - 12.12.2020

### Neu
- Wallboxen: Ändern des SOC für Überschussladen mit nachfolgendem Laden auf den neuen SOC ist auch möglich, wenn zuvor bereits CHARGING_COMPLETED erreicht wurde.

### Geändert
- Wallboxen: diverse Fehler im Bereich zeitplangesteuertes Laden

## [1.6.5](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.5) - 23.11.2020

### Neu
- wenn für ein Fahrzeug einer Wallbox ein SOC-Script konfiguriert wurde, wird dieses automatisch erneut ausgeführt (siehe [#90](https://github.com/camueller/SmartApplianceEnabler/issues/90) ), wenn sich der SOC rechnerisch um 20% erhöht hat. Dieser Default kann umkonfiguriert werden bzw. es kann zusätzlich auch ein Mindest-Zeitabstand zwischen zwei SOC-Abfragen angegeben werden. Wichtig ist, dass bei einer Abfrage wirklilch der aktuelle SOC des Fahrzeugs geliefert wird, nicht irgendein alter SOC-Wert. Aus der Differenz zwischen berechnetem SOC-Wert und abgefragtem SOC-Wert werden die Ladeverluste ermittelt, wobei ein initial konfigurierter Wert für die Ladeverluste berücksichtigt wird.
- wenn für ein Fahrzeug einer Wallbox ein SOC-Script konfiguriert wurde, wird dessen Ausgabe nur interpretiert, wenn der Prozess mit Return-Code 0 beendet wird

### Geändert
- bei S0-Zählern funktioniert jetzt auch PULL-UP korrekt bzw. sollte sogar präferiert werden, um den Einfluss von Störungen zu minimieren. Dazu werden jetzt auch Impulse mit einer Pulsdauer < 20 ms ignoriert.
- Komma-Zahlen von HTTP-Antworten im JSON-Format werden jetzt korrekt interpretiert
- Laden mit Überschuß-Energie hat nur in den ersten 48 Stunden nach dem Verbinden des Fahrzeugs funktioniert. Der Fehler, der zu dieser Beschränkung geführt hat, wurde beseitigt.
- Die Konfiguration der Energiemenge bei Zählern ist nur noch für Wallboxen möglich
- bei Wallboxen wurde die Erkennung des Lade-Ende verbessert

## [1.6.4](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.4) - 16.10.2020

### Neu
- Wallboxen werden in der Status-Übersicht immer angezeigt (bisher nur bei verbundenem Fahrzeug)
- um für Wallboxen einen vom Standard-SOC für Überschußladen abweichenden Soll-SOC festlegen zu können (https://github.com/camueller/S…plianceEnabler/issues/104), wurde der Dialog beim Klick auf die grüne Ampel-Lampe umgebaut. Dort wird jetzt unterschieden zwischen Sofortladen, Optmiertem Laden und Überschußladen, wobei jeweils nur die relevanten Felder angezeigt werden.

### Geändert
- die Leistungsberechnung für Zähler mit anderen Impuls-Raten als 1000 imp/kWh war falsch - das wurde korrigiert

## [1.6.3](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.3) - 05.10.2020

### Neu
- Holding-Register können gelesen werden (Dank an DNATW für die Implementierung :-) )
- bei Wallboxen werden Einschaltbefehle vom SHM nur dann ausgeführt, wenn auch die Ladeleistung vorgegeben wird. Beim SHM 2.0 ist das offensichtlich öfters nicht der Fall, bei SHM 1.0 passiert es auch, aber sehr selten.

### Geändert
- bei S0-Zählern wurde die Leistung nicht richtig berechnet bei Geräten mit geringer Leistungsaufnahme
- in der Status-Anzeige bei Wallboxen entsprach der Wert für "Geplante Energeimenge" der noch zu ladenen Energeimenge. Jetzt wird die insgesamt "Geplante Energiemenge" angezeigt.

## [1.6.2](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.2) - 26.07.2020

### Geändert
- Konfiguration von HTTP-Schaltern wurde als falsch erkannt, obwohl gültig
- die Checkbox "Statusabfrage" bei HTTP-Schaltern hat nicht den richtigen Zustand angezeigt

## [1.6.1](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.1) - 25.07.2020

### Neu
- komplett neue Web-Oberfläche auf Basis von Angular Material
- neue Implementierung von S0-Zählern - siehe [#92](https://github.com/camueller/SmartApplianceEnabler/issues/92)

### Geändert
- diverse Verbesserungen für Wallboxen
