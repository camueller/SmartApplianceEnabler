# State of charge (SOC) automatisiert auslesen
Leider können die meisten heimischen Wallboxen noch kein ISO 15118 (umfangreiche Kommunikation über Powerline Communication (PLC) zum Fahrzeug), weshalb die Wallbox nicht den SOC des Fahrzeugs kennt.

Zum automatisierten Auslesen des SOC durch den *Smart Appliance Enabler* existieren zwei Möglichkeiten:
- SOC-Script
- ODB2-Adapter

## SOC-Script
Für einige Fahrzeuge existieren sogenannte *SOC-Scripts*, welche meist einen Zugriff der Hersteller-Homepage emulieren, um an den SOC zu kommen. Die Scripts sind meist **nicht dauerhaft stabil**, weil jede Änderung der Hersteller-Homepage eine Anpassung der SOC-Scripts bzw. der verwendeten Bibliotheken nach sich zieht. Manche Hersteller bieten den Zugriff auf Fahrzeugdaten auch über **kostenpflichtige Dienste** an.

Das Verwenden dieser fahrzeug-spezifischen Scripts ist in den nachfolgenden Kapiteln beschrieben, wobei es sich um Inhalte von Usern handelt und ich die beschriebenen Verfahren nicht verifizieren konnte.

* [Kia](kia_DE.md)
* [Nissan](NissanLeaf_DE.md)
* [Skoda](Skoda_DE.md)
* [Volkswagen](VW_DE.md)

## ODB2-Adapter
Die Probleme der SOC-Scripts lassen sich umgehen, indem der SOC im Fahrzeug über die in jedem Fahrzeug vorhandene **ODB2-Schnittstelle** ausgelesen wird.

* [WiCAN ODB2-Adapter](WiCAN_DE.md)
