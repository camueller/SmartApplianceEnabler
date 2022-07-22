# Changelog

All wesentlichen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
und das Projekt folgt den Leitlinien des [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Abkürzungen

| Abkürzung   | Erläuterung |
| ----------- | ----------- |
| SAE         | Smart Appliance Enabler |
| SHM         | Sunny Home Manager |

## [2.0.5](https://github.com/camueller/SmartApplianceEnabler/releases/tag/2.0.5) - 22.07.2022

### Gefixt
- pgpiod muss nur dann laufen, wenn GPIOs verwendet werden - siehe [#252](https://github.com/camueller/SmartApplianceEnabler/issues/252)
- Wallbox: kein endloses Wechseln zwischen CHARGING_COMPLETED und VEHICLE_CONNECTED
- bei Lesen von Float-Werten aus Modbus-Registern wird jetzt der Umrechnungsfaktor berücksichtigt
- durch Änderungen an der Datei `smartapplianceenabler.service` sollte sichergestellt sein, dass der SAE erst nach Mosquitto gestartet wird
- wenn zum Zeitpunkt der Anlaufstromerkennung die verbeleibende Zeit im Zeitplan nicht ausreichend war, wurde das nicht beim nächstmöglichen Zeitplan-Intervall berücksichtigt - siehe [#262](https://github.com/camueller/SmartApplianceEnabler/issues/262)
- die Initialisierung des Stufenschalters mit GPIO-Schaltern wurde gefixt
 
### Neu
- Wallbox: die Wallbox-Vorlagen werden beim Start des SAE von Github geladen - dadurch können Vorlagen hinzugefügt/gefixt werden ohne neues SAE-Release
- Wallbox: Identifikation des mit der Wallbox verbundenen Fahrzeugs über einen oder mehrere Parameter, sofern das SOC-Script diese liefern kann:
  - Verbindungsstatus des Ladekabels
  - Zeitpunkt des Einsteckens des Ladekabels
  - Geo. Breite/Länge des Fahrzeugs
- Wallbox: das Periodische Setzen der Ladestromstärke zur Vermeidungs des Rückfalls auf eine Standard-Stromstärke wird unterstützt (erforderlich z.B. für Alfen-Wallbox)
- Modbus unterstützt 64bit Float
- Modbus unterstützt 2 Zeichen pro 16bit-Wort
- Modbus unterstützt FunctionCode 16 zum Schreiben mehrerer Holding-Register

### Geändert
- Wallbox: Der Default-Wert für die Statuserkennung-Unterbrechnung wurde von 300s auf 30s verkürzt
- 

## [2.0.4](https://github.com/camueller/SmartApplianceEnabler/releases/tag/2.0.4) - 13.04.2022

### Gefixt
- Master-Zähler wurde nicht richtig initialisiert - siehe [#206](https://github.com/camueller/SmartApplianceEnabler/issues/206)
- Wallbox: Wenn beim Laden mit Überschuss-Energie das Ladeziel erreicht wird, bleibt dieser Zustand bestehen, solange das Fahrzeug nicht getrennt wird 

### Neu
- [neuer Schalter-Typ Stufenschalter](https://github.com/camueller/SmartApplianceEnabler/issues/223)
- [neuer Schalter-Typ PWM-Schalter](https://github.com/camueller/SmartApplianceEnabler/issues/222)
- Unterstützung für Authentifizierung am MQTT-Broker

## [2.0.3](https://github.com/camueller/SmartApplianceEnabler/releases/tag/2.0.3) - 20.03.2022

### Gefixt
- Wallbox: [Ladeleistung auf Minimum reduziert trotz deutlich höherem Überschuss](https://github.com/camueller/SmartApplianceEnabler/issues/219)
- verschiedene Fixes für Master/Slave-Zähler
- zu häufiger Verbindungsaufbau zum MQTT-Broker wird vermieden 

### Geändert
- Logging erweitert für Inhalte, die an der SEMP-Schnittstelle an den SHM gemeldet werden

## [2.0.2](https://github.com/camueller/SmartApplianceEnabler/releases/tag/2.0.2) - 30.01.2022

### Neu
- im Node-RED-Dashboard wird jetzt auch angezeigt, wann der SAE das letzte Mal vom SHM abgefragt wurde - siehe [#159](https://github.com/camueller/SmartApplianceEnabler/issues/159). Dazu müssen allerdings die Flows neu importiert werden. Zuvor müssen die alten Flows gelöscht werden - siehe [Löschen der vorhandenen Flows](doc/NodeRED_DE.md)

### Gefixt
- bei HTTP-Zählern mit Parameter "Zählerstand" konnte die Leistungsberechnung zu falschen Werten führen, wenn bei der vorangegangenen Abfrage ein HTTP-Fehler aufgetreten war
- beim Schalten via Ampel hat diese direkt nach dem Schalten den neuen Status zunächst nicht richtig angezeigt
- bei HTTP-Schaltern wurden keine Benachrichtigungen verschickt - siehe [#171](https://github.com/camueller/SmartApplianceEnabler/issues/171)

### Geändert
- es werden keine "last will" MQTT-Nachrichten mehr verschickt
- Abfrage der Zähler wird nicht mehr durch SEMP-Abfrage des SHM getriggert
- HTTP/Modbus-Zähler werden (unabhängig von Parameter "Zählerstand" oder "Leistung") alle 60s abfragt; falls der Zähler zur Anlaufstromerkennung verwendet wir, erfolgt die Abfrage alle 20s
- für die Zähler werden keine Durchschnitte berechnet: der letzte abfragte (Parameter "Leistung") oder berechnete (Parameter "Zählerstand") Leistungswert wird an den SHM übermittelt
- der `Forum`-Link zeigt jetzt auf Github-Discussions

## [2.0.1](https://github.com/camueller/SmartApplianceEnabler/releases/tag/2.0.1) - 21.01.2022

### Gefixt
- Verwendung von HTTP-Zählern mit Parameter "Leistung" hatte zu einem Fehler geführt
- Speichern der Benachrichtigungen ohne Angabe von Ereignisarten bei Zählern funktioniert wieder 
- Node-RED: Flow-Export korrigiert für bessere Berücksichtigung der Request-Art im Timeframe-Diagramm

## [2.0.0](https://github.com/camueller/SmartApplianceEnabler/releases/tag/2.0.0) - 09.01.2022

### Neu
- der wesentliche Teil der internen Kommunikation wurde auf MQTT umgestellt - siehe auch [#124](https://github.com/camueller/SmartApplianceEnabler/issues/124).  [Deshalb ist muss MQTT-Broker vorhanden sein und installiert werden](doc/ManualInstallation_DE.md).
- zusätzlich zur Status-Seite mit den Ampeln kann man Node-RED dazu verwenden [ein detailliertes Dashboard anzuzeigen](doc/NodeRED_DE.md) - siehe [#159](https://github.com/camueller/SmartApplianceEnabler/issues/159)
- für den Zugriff auf die GPIOs des Raspberry Pi wird nicht mehr Pi4J verwendet, sondern `pigpioj`.
  - [Dafür muss allerdings pigpiod installiert werden.](doc/ManualInstallation_DE.md).
  - Es ist ein neuer Parameter in `/etc/default/smartapplianceenabler` hinzugekommen (`-DPIGPIOD_HOST=localhost"`): die neue Datei installieren oder die neuen Zeilen hinzufügen
  - Durch den Wechsel wird `libwiringpi` nicht mehr benötigt, die ab Raspbian Bullseye nicht mehr im offiziellen Raspbian-Repository enthalten ist.
  - Durch den Wechsel ändert sich auch das [Nummern-Schema der GPIOs](doc/Raspberry_DE.md). Bereits konfigurierte GPIO-Nummer werden automatisch migriert.
- zwei Appliances können sich einen Zähler teilen - [Master-/Slave-Zähler](doc/MasterSlaveMeter_DE.md) 

## [1.6.18](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.18) - 20.12.2021

### Gefixt
- beim Abfragen von Zählern über HTTP konnte es zu Ungeauigkeiten kommen, wenn keine Regex angegeben war
- Wallbox: beim Steuern über die Ampel wird der aktuelle SOC wieder im entsprechenden Eingabefeld vorbelegt
- Wallbox: beim Ändern des Ziel-SOC der Überschussladung über die Ampel bleibt der Zeitpunkt des Beginn erhalten
- Wallbox: wenn das Fahrtzeug an einem Sonntag mit der Wallbox verbunden wurde, wurde der Wochentag nicht angezeigt
- Wallbox: beim Zeitplan-basierten Laden wurde der Energiemengenzähler nur beim ersten Mal korrekt initialisiert, danach nicht mehr

## [1.6.17](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.17) - 01.11.2021

### Gefixt
- nach dem erneuten Verbinden des Fahrzeugs mit der Wallbox wurde manchmal ein negativer SOC ermittelt
- diverse Fehler bei Modbus-Schaltern gefixt
- WEB: ein über die Ampelsteuerung geänderter SOC wurde nicht übernommen
- Bei HTTP-basierten Schaltern hat die Anlaufstromerkennung nicht funktioniert, wenn als Parameter "Zählerstand" konfiguriert ist

### Geändert
- beim Lesen von HTTP-Werten kann jetzt immer die HTTP-Methode konfiguriert werden 

## [1.6.16](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.16) - 28.08.2021

### Gefixt
- WEB: Beim Wechseln zwischen den Zählern verschiedener Geräte wird der Inhaltkorrekt aktualisiert
- WEB: Benachrichtigungen konnten nicht gespeichert werden, wenn bei Schaltern die Anlaufstromerkennung aktiviert war
- WEB: bei HTTP-Schaltern kam es zu einem Fehler nach Klick auf "Status via HTTP abfragen"
- WEB: beim Laden im Mode "Optimiert" wurde der eingegebene Ist-SOC nicht korrekt gespeichert, was eine fehlerhafte Energieberechnung nach sich zog
- WEB: die Steuerung zum Einblenden des Feldes "Abfrageintervall" bei HTTP-/Modbus-Zählern hat nicht immer funktioniert

### Geändert
- Wenn der SHM mit dem Einschaltbefehl für Wallboxen keine Leistungsvorgabe sendet, wird jetzt mit der als minimal konfigurierten Leistung eingeschaltet
- Wenn das Fahrzeug von der Wallbox getrennt wird, werden die Queue geleert; bisher blieben Anforderungen für die Folgetage in der Queue, wenn Zeitpläne konfiguriert waren.
- WEB: bei HTTP-Zählern wurde bisher keine HTTP-Methode als Standard angezeigt, obwohl implizit GET verwendet wurde. Jetzt kann nur zwischen vorhandenen HTTP-Methoden gewählt werden, d.h. eine Leer-Auswahl ist nicht mehr möglich.
- kleinere Änderungen an Texten und am Layout, die im Rahmen der Englisch-Übersetzung aufgefallen sind
- Dokumentation aktualisiert

### Neu
- WEB: alle Texte sind jetzt in Englisch verfügbar. Wenn die laut Web-Browser gewünschte Sprache Deutsch ist, werden deutsche Texte angezeigt, in allen anderen Fällen die englischen Texte.
- WEB: Unterstützung von "Zur Startseite hinzufügen", was besonders zum direkten Aufruf der SAE-Seite auf Handies hilfreich ist; dazu habe ich das Logo endlich mal als Vektorgrafik erstellt und dafür sowie für die Homepage in verschiedenen Auflösungen verwendet


## [1.6.15](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.15) - 06.06.2021

### Gefixt
- Bei Zeitplänen konnte der Wert für die minimale Laufzeit nicht gelöscht werden, was dazu geführt hat, dass dieser Zeitplan ungewollt die Verwendung von Überschussenergie anfordert hat
- Zeitpläne über REST-API wurden nicht eingeplant. Siehe [#149](https://github.com/camueller/SmartApplianceEnabler/issues/149) 

## [1.6.14](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.14) - 09.05.2021

### Gefixt
- Berechnung der Ladeverluste und Berücksichtigung bei der Berechnung des aktuellen SOC während des Ladens
- falls beim Berechnen der Dauer bis zur nächsten Zählerabfrage ein negativer Wert berechnet wird, erfolgt einmalig keine Synchronisation der Zählerabfrage mit der Abfrage durch den SHM. Bisher konnte dieser Fehler dazu führen, dass der Zähler nachfolgend nicht mehr abgefragt wurde 
- beim Starten eines Ladevorgangs via Ampel erfolgt keine initialisierung des Zählers mehr, wenn bereits Energie geladen wurde seit dem Verbinden des Fahrzeugs. Durch die bisher erfolgte Initialisierung wurde nachfolgend der SOC falsch berechnet.
- bei Wallboxen wurde der Status nach dem Einschalten nach Ablauf der konfigurierten `Statuserkennung-Unterbrechung` bis zur nächsten Abfrage des Wallbox-Status falsch angezeigt. Das wurde gefixt.

### Geändert
- wenn die Energiemenge bis zur Erreichung des Soll-SOC kleienr als 1 kWh ist, wird zusätzlich das SOC-Script ausgeführt, um sicherzustellen, dass der Soll-SOC erreicht wird
- wenn `Ladestatuserkennung abwarten` aktiv ist, wird vor dem kurzzeitigen Einschalten der Wallbox die Ladestromstärke auf den Wert gesetzt, welcher der konfigurierten `minimalen Leistungsaufnahme` entspricht

## [1.6.13](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.13) - 24.04.2021

### Geändert
- bei S0-Zählern wurde der Standart-Wert für die minimale Pulsdauer wurde von 15 ms auf 20 ms erhöht, um Streuungen bei der Genauigkeit der Pluselänge zu eliminieren
- zur Vermeidung von Problemen durch das Monitoren aktueller Geräteverbräuche im Sunny Portal wertet der SAE die Abfrage durch das Portal nur dann als Trigger für das rechtzeitige Auslesen vor der nächsten Abfrage durch das Portal, wenn seit der letzten Abfrage mindestens 50 s vergangen sind 

### Gefixt
- die Änderung der Ladeverluste wurde als negativer Wert berechnet, wenn das (Überschuss)-Laden bereits begonnen hat bevor das SOC-Script einen Wert geliefert hat
- es konnte passieren, dass zwei Timeframe-Intervalle für Überschussenergie erzeugt wurde (der SAE geht davon aus, dass es immer nur einen solchen gibt)
- bei der Erzeugung der SEMP-Timeframes ist jetzt sichergestellt, dass nur solche mit einem max-Wert > 0 an den SHM übermittel werden
- das Ändern des SOC für Überschussenergie via Ampel-Steuerung war aufgrund eines Fehler nicht möglich, wenn der Zustand der Wallbox bereits CHARGING_COMPLETED war und keine Zeitpläne konfiguriert waren

### Neu
- für HTTP-basierte Geräte wird neben GET und POST jetzt auch PUT, PATCH und DELETE unterstützt
- die Wallboxen [WARP Charger Smart / Pro werden unterstützt inkl. einer Vorlage für Konfiguration mit allen relevanten Einstellungen](https://github.com/camueller/SmartApplianceEnabler/blob/master/doc/WarpCharger_DE.md)
- bei Wallboxen wird beim Nicht-Überschuss-Laden das Ende des Timeframe-Intervals während des Ladens auf Basis der verbleibenden Energiemenge und der aktuellen Ladeleistung ständig neu berechnet (bisher nur einmal zu Ladebeginn). Dadurch sollte auch die Ladekurve bei fast vollem Akku besser abgebildet sein und das zu zeitige Beenden des Ladevorgangs verhindert werden. 

## [1.6.12](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.12) - 13.03.2021

### Geändert
- bei der Konfiguration von **Modbus**-Geräten war bisher der Register-Typ und das Format der Modbus-Antwort nicht klar getrennt (z.B. `InputFloat`). Das hat es den Anwendern unnötig erschwert, die korrekten Modbus-Einstellungen zu festzulegen. Deshalb wurde in der `Appliances.xml` für das Element `ModbusRead` das Attribut `type` um das Attribut `valueType` ergänzt, sodass in `type` nur noch der Typ laut Modbus-Spezifikation (Coil, Discrete, Holding, Input) steht und in `valueType` das Format der Modbus-Antwort (Float, Integer, Integer2Float, String). Beim Laden der `Appliance.xml` erfolgt automatisch ein Konvertierung in das neue Format, wobei die Änderungen erst bei der nächsten Änderung der Konfiguration tatsächlich in die `Appliance.xml` geschrieben werden. Trotzdem ist keine manuelle Anpassung erforderlich. In der Web-Oberfläche werden die Register-Typen mit ihrem Function-Code angezeigt, um Anwendern dabei zu helfen, die korrekten Modbus-Einstellungen zu festzulegen.

### Gefixt
- die Genauigkeit der Leistungsberechung aus Zählerstandsdifferenzen wurde verbessert durch die Berechnung der Zeitdifferenz in Millisekunden anstatt Sekunden
- die bisherige Verwendung des **Modbus-Protokolls** war fehlerhaft, weil nicht klar zwischen der Anzahl der angeforderten Datenwörter und der tatsächlichen Anzahl der Datenwörter in der Modbus-Antwort unterschieden wurde. In diesem Zusammenhang wurde in der Web-Oberfläche das Feld `Bytes` in `Datenwörter` umbenannt. Dementsprechend wurde auch in der `Appliance.xml` das Attribut `bytes` in `words` umbenannt. Beim Laden der `Appliance.xml` erfolgt automatisch ein Konvertierung in das neue Format, wobei die Änderungen erst bei der nächsten Änderung der Konfiguration tatsächlich in die `Appliance.xml` geschrieben werden. Trotzdem ist keine manuelle Anpassung erforderlich.
- Beim Konfigurieren von HTTP/Modbus-Schaltern und Wallboxen über die Web-Oberfläche konnte eine ungültige Konfiguration gespeichert werden, die erst beim nachfolgenden Start des SAE als ungültig erkannt wurde. Jetzt ist das Speichern nur möglich, wenn die Konfiguration auch gültig ist. Siehe [#136](https://github.com/camueller/SmartApplianceEnabler/issues/136)
- wenn bei Wallboxen der Ladevorgang extern (z.B durch das Auto oder die Web-Oberfläche des Ladecontrollers/Wallbox) gestartet oder gestoppt wird, erkennt der SAE diese Statusänderung jetzt und stellt sicher, das auch der SHM mit korrekten Informationen versorgt wird
- wenn bei Wallboxen das Ladeziel eines Fahrzeugs erreicht war, konnte nachfolgend kein manueller Ladevorgang mit höherem SOC als Ladeziel gestartet werden - das wurde jetzt korrigiert.

### Neu
- die Wallboxen [KeContact P30 c-series / x-series des Herstellers Keba werden voll unterstützt inkl. einer Vorlage für Konfiguration mit allen relevanten Einstellungen](https://github.com/camueller/SmartApplianceEnabler/blob/master/doc/Keba_DE.md)
- der für den Stream-Server verwendete Port kann mittels der Variable `semp.streamserver.port` festgelegt werden - normalerweise wird ein freier Port dynamisch bestimmt 

## [1.6.11](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.11) - 13.02.2021

### Geändert
- die Messgrösse `Energie` bei HTTP-/Modbus-basierten Zählern wurde in `Zählerstand` umbenannt
- für HTTP-/Modbus-basierte Zähler kann nur noch eine Messgrösse (entweder `Zählerstand` oder `Leistung`) angegeben werden. Wird diese auf `Zählerstand` gesetzt (bevorzugte Einstellung), wird der Zählerstand nur noch alle 60 Sekunden abgefragt und die an den SHM gemeldete Leisung aus Zählerstandsdifferenz und Zeitdifferenz berechnet. Wird die Messgrösse `Leistung` konfiguriert, erfolgt die Abfrage des Zählers weiterhin entsprechend des konfigurierten Abfrageintervals (Standardwert: alle 20 Sekunden), wobei die an den SHM gemeldete Leistung als Durchschnitt der Werte in den letzten 60 Sekunden berechnet wird. Falls für einen Zähler bisher `Energie` und `Leistung` konfiguriert waren, wird die Konfiguration für `Leistung` automatisch ignoriert und verschwindet beim nächsten Speichern der Konfiguration aus der `Appliances.xml`. Bei Verwendung von Geräten mit [Tasmota-Firmware bitte die Hinweise bzgl. des Auslesens der Felder und der Einstellung der Zählergenauigkeit](doc/Tasmota_DE.md) beachten.
- das `Messinterval` bei Zählern (müsste eigentlich `Durchschnittsberechnungsinterval` heissen) ist grundsätzlich nicht mehr konfiguriertbar und intern auf das Abfrageinterval des SHM (60 Sekunden) gesetzt.
- einige Log-Meldungen wurden zusammenfasst
- einige Log-Meldungen wurden umklassifiziert für Level `TRACE` und sollten daher im Normalfall (d.h. also Level `DEBUG`) nicht im Log erscheinen

### Neu
-  bei der Eingabe der `ID` eines Gerätes wird geprüft, ob diese bereits von einem anderen Gerät in diesem *Smart Appliance Enabler** verwendet wird. Ist das der Fall, wird ein Fehlermeldung angezeigt und das Speichern der Konfiguration ist nicht möglich. Siehe [#127](https://github.com/camueller/SmartApplianceEnabler/issues/127)

## [1.6.10](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.10) - 26.01.2021

### Geändert
- in Anlehnung an die grün blinkende Ampel bei Verwendung von Überschussenergie gibt es einen neuen Status "gelb blinkend", der klarmacht, dass eine Anforderung (Laufzeit oder Energie) besteht, aber nur für Überschussenergie
- Dokumentation und Konfigurationsvorlage für den go-eCharger verwende jetzt den Parameter `amx` anstatt `amp` zur Steuerung der Ladestromstärke
- beim Erstellen der Timeframe-Intervalle nach dem der SAE umkonfiguriert/restartet wurde, wird eine ggf. konfigurierte min. Laufzeit berücksichtigt, wodurch der nächste Timeframe-Interval zeitnaher sein sollte, als bisher
- beim Lademodus *Schnell* werden Unterbrechungen durch den SHM nicht akzeptiert

### Gefixt
- in der Dokumentation zum go-eCharger war der Wert `$.dws` fälschlicherweise unter *Regex für Extraktion* unter nicht unter *Pfad* eingetragen
- beim Lademodus *Optimiert* kam es zu einem Fehler, was dazu geführt hat, dass die Ladeanzeige dauerhaft angezeigt und das Laden nicht gestartet wurde
- beim Lademodus *Optimiert* wurde das Feld zur Wochentag-Asuwahl nicht als Pflichtfeld markiert
- bei Zeitplänen wird die minimale Laufzeit 0 entsprechend der Validierungsvorgaben als `00:00` angezeigt
- wenn der SAE umkonfiguriert/restartet wird, während eine Wallbox gerade lädt, sollte er das Laden zwar stoppen, aber nicht in den Status `CHARGING_COMPLETED` gehen
- beim Ausführen einer Tasmota-Regel wird das Gerät abgeschaltet und eine Laufzeit-Anforderung entsprechend der übergebenen Werte für maxRuntime und latestEnd gestellt

## [1.6.9](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.9) - 17.01.2021

### Neu
- im Menü finden sich Links zur Fragen&Antworen, zum Forum und zur Projekt-Homepage
- an Eingabefeldern für Shellscripts (derzeit Script für Benachrichtigungen und SOC-Script) wird angezeigt, ob das Shell-Script existiert und ausführbar ist. Andernfalls wird eine Fehlermeldung angezeigt und das Speichern ist nicht möglich
- in der Web-Oberfläche wird (neben dem Titel) auf das Vorhandensein neuer Versionen hingewiesen mit direktem Link zur neuen Version
- stark vereinfachter Installationsprozess

### Geändert
- Nach dem Abbrechen einer Sofortladung durch Klick auf die rote Ampel war es nicht möglich, den SOC für Überschussladen wirksam zu ändern. Das ist jetzt möglich.
- Projekt-Homepage inkl. Screenshots und Video wurde komplett aktualisert

## [1.6.8](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.8) - 04.01.2021

### Neu
- Geräte mit Tasmota-Firmware können via Tastendruck eine Laufzeitanforderung stellen - siehe [#111](https://github.com/camueller/SmartApplianceEnabler/issues/111)
- Auf der Projekt-Homepage findet sich jetzt auch ein Video der Web-Oberfläche "in Action", dass regelmässig im Rahmen der automatsierten Tests entsteht. Ich habe es dort verlinkt, damit sich Neu-User einen besseren Eindruck von der Web-Oberfläche verschaffen können.

### Geändert
- das grüne Ampellicht blinkt nicht nur, wenn Wallboxen Überschussenergie laden, sodern auch bei allen anderen Geräte, sobald diese Überschussenergie verwenden - siehe [#107](https://github.com/camueller/SmartApplianceEnabler/issues/107)
- vor der Ausführung eines SOC-Scripts wird geprüft, ob dieses vorhanden und ausführbar ist; falls nicht, werden hilfereiche Meldungen geloggt
- auf allen Seiten des SAE findet sich mindestens ein rotes Fragezeichen-Symbol, das beim Anklicken direkt auf die entsprechende Seite der SAE-Dokumentation führt. Dazu habe ich die gesamte Dokumentation umstrukturiert, konsolidiert und aktualisiert. Auch die Screenshots sind jetzt aktuell!
- an das Shell-Script, dass für Benachrichtigungen aufgerufen wird, werden jetzt neben der ID auch Name, Hersteller, Typ und Seriennummer übergeben. Falls jemand bereits die Benachrichtigungen per Telegram verwendet muss das aktualisierte Script verwendet werden!

### Gefixt
- ein regulärer Ausdruck für ein SOC-Script kann wieder gelöscht werden, ohne dass das als leere Regex interpretiert wird
- bei mehrmaligem Verbinden des Fahrzeugs mit der Wallbox wurde der Zeitpunkt des erstmaligen Verbindens angezeigt; jetzt immer der Zeitpunkt des letzten Verbindens
- für Walboxen konnten die URLs des zugehörigen Zählers nicht gelöscht werden, sobald beide URLs eingegeben waren
- die Eingabe von Zeichen wie "<> in die Eingabefelder der Web-Oberfläche konnte dazu führen, dass die `Appliances.xml` ungültig wird. Ab sofort werden solche Zeichen automatisch "escaped", d.h. der Anwender muss sich darüber keine Gedanken machen (entsprechende Hinweise wurden aus der Dokumentation entfernt)

## [1.6.7](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.7) - 26.12.2020

### Neu
- Für Ereignisse ("Einschalten", "Ausschalten", "Kommunikationsstörung", ...) können Benachrichtigungen versendet werden (siehe doc/Configuration_DE.md#benachrichtigungen )
- Falls einem Gerät kein Schalter zugewiesen ist, wird automatisch der neue Schaltertyp "Zählerbasierter Zustandsmelder") zugewiesen. Dieser meldet "eingeschaltet", wenn die Leistungsaufnahme oberhalb des Schwellwertes (Standard: 10W) liegt, ansonsten "ausgeschaltet". Für taktende Geräte (typischerweise solche, bei denen eine Temperatur gehalten werden muss), kann eine Ausschaltverzögerung konfuguriert werden, damit das Takten nicht erfasst wird (siehe doc/MeterReportingSwitch_DE.md )
- Es gibt eine neue Schema-Datei für die `Appliances.xml`, welche alle Neuerungen der bisherigen 1.6er-Versionen reflektiert. Die Migration der aktuellen Appliances.xml erfolgt automatisch. Allerdings ist die migrierte Datei nicht mehr mit bisherigen Versionen verwendbar - also ggf. vorher sichern.

### Geändert
- Nachdem kürzlich meine Änderungen an der Web-Oberfläche erst nach "Neu laden" sichtbar wurden, habe ich mir das Caching-Verhalten der Web-Anwendung genauer angesehen und gefixt. Der Fix wirkt aber erst ab dieser Version, d.h. falls Inhalte nicht oder nicht richtig angezeigt werden, müßt Ihr noch einmal "Neu laden" (CTRL-F5) drücken.
- Wenn der SHM den SAE abfragt, während er gerade startet (das ist auch der Fall nach dem Speichern einer Konfigurationsänderung in der Web-Oberfläche), kam es zu Fehlern. Jetzt wird dem SHM währenddessen signalisiert, dass der SAE noch nicht läuft (HTTP 503).
- Bei Wallboxen wird das gelbe Ampellicht nicht mehr im Kontext des Überschußladens verwendet

### Gefixt
- Bei meinen Tests fiel mir auf, dass es beim Speichern von Änderungen immer Fehler gab, falls im SAE S0-Zähler oder GPIO-Schalter konfiguriert sind. Der Fehler hat dazu geführt, dass der Zähler/Schalter nicht mehr funktioniert bis zum Neustart des SAE. Das wurde gefixt.


## [1.6.6](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.6) - 12.12.2020

### Neu
- Wallboxen: Ändern des SOC für Überschussladen mit nachfolgendem Laden auf den neuen SOC ist auch möglich, wenn zuvor bereits CHARGING_COMPLETED erreicht wurde.

### Gefixt
- Wallboxen: diverse Fehler im Bereich zeitplangesteuertes Laden

## [1.6.5](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.5) - 23.11.2020

### Neu
- wenn für ein Fahrzeug einer Wallbox ein SOC-Script konfiguriert wurde, wird dieses automatisch erneut ausgeführt (siehe [#90](https://github.com/camueller/SmartApplianceEnabler/issues/90) ), wenn sich der SOC rechnerisch um 20% erhöht hat. Dieser Default kann umkonfiguriert werden bzw. es kann zusätzlich auch ein Mindest-Zeitabstand zwischen zwei SOC-Abfragen angegeben werden. Wichtig ist, dass bei einer Abfrage wirklilch der aktuelle SOC des Fahrzeugs geliefert wird, nicht irgendein alter SOC-Wert. Aus der Differenz zwischen berechnetem SOC-Wert und abgefragtem SOC-Wert werden die Ladeverluste ermittelt, wobei ein initial konfigurierter Wert für die Ladeverluste berücksichtigt wird.
- wenn für ein Fahrzeug einer Wallbox ein SOC-Script konfiguriert wurde, wird dessen Ausgabe nur interpretiert, wenn der Prozess mit Return-Code 0 beendet wird

### Geändert
- bei S0-Zählern funktioniert jetzt auch PULL-UP korrekt bzw. sollte sogar präferiert werden, um den Einfluss von Störungen zu minimieren. Dazu werden jetzt auch Impulse mit einer Pulsdauer < 20 ms ignoriert.
- Die Konfiguration der Energiemenge bei Zählern ist nur noch für Wallboxen möglich
- bei Wallboxen wurde die Erkennung des Lade-Ende verbessert

### Gefixt
- Komma-Zahlen von HTTP-Antworten im JSON-Format werden jetzt korrekt interpretiert
- Laden mit Überschuß-Energie hat nur in den ersten 48 Stunden nach dem Verbinden des Fahrzeugs funktioniert. Der Fehler, der zu dieser Beschränkung geführt hat, wurde beseitigt.

## [1.6.4](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.4) - 16.10.2020

### Neu
- Wallboxen werden in der Status-Übersicht immer angezeigt (bisher nur bei verbundenem Fahrzeug)
- um für Wallboxen einen vom Standard-SOC für Überschußladen abweichenden Soll-SOC festlegen zu können (siehe [#104](https://github.com/camueller/SmartApplianceEnabler/issues/104) ), wurde der Dialog beim Klick auf die grüne Ampel-Lampe umgebaut. Dort wird jetzt unterschieden zwischen Sofortladen, Optmiertem Laden und Überschußladen, wobei jeweils nur die relevanten Felder angezeigt werden.

### Gefixt
- die Leistungsberechnung für Zähler mit anderen Impuls-Raten als 1000 imp/kWh war falsch - das wurde korrigiert

## [1.6.3](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.3) - 05.10.2020

### Neu
- Holding-Register können gelesen werden (Dank an DNATW für die Implementierung :-) )

### Geändert
- bei Wallboxen werden Einschaltbefehle vom SHM nur dann ausgeführt, wenn auch die Ladeleistung vorgegeben wird. Beim SHM 2.0 ist das offensichtlich öfters nicht der Fall, bei SHM 1.0 passiert es auch, aber sehr selten.
- in der Status-Anzeige bei Wallboxen entsprach der Wert für "Geplante Energeimenge" der noch zu ladenen Energeimenge. Jetzt wird die insgesamt "Geplante Energiemenge" angezeigt.

### Gefixt
- bei S0-Zählern wurde die Leistung nicht richtig berechnet bei Geräten mit geringer Leistungsaufnahme

## [1.6.2](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.2) - 26.07.2020

### Gefixt
- Konfiguration von HTTP-Schaltern wurde als falsch erkannt, obwohl gültig
- die Checkbox "Statusabfrage" bei HTTP-Schaltern hat nicht den richtigen Zustand angezeigt

## [1.6.1](https://github.com/camueller/SmartApplianceEnabler/releases/tag/1.6.1) - 25.07.2020

### Neu
- komplett neue Web-Oberfläche auf Basis von Angular Material
- neue Implementierung von S0-Zählern - siehe [#92](https://github.com/camueller/SmartApplianceEnabler/issues/92)

### Geändert
- diverse Verbesserungen für Wallboxen
