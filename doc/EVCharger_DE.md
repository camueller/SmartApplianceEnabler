# Ampel-Steuerung
Der Ist-SOC wird bei Konfiguration eines SOC-Scripts vorbelegt mit dem aktuellen Wert zu diesem Zeitpunkt, ansonsten kann er im Auto angelesen und eingegeben werden, wenn man dem SHM eine gute Planung ermöglichen will (ansonsten wird 0 angenommen).

Wird kein Soll-SOC eingegeben, wird 100% angenommen.

Wird bei "bis" ein Wochentag/Zeit eingegeben, wird dem SHM der Bedarf gemeldet aber der SAE schaltet den Lader nicht selbst an, d.h. das Laden beginnt erst, wenn der SHM einen Einschaltbefehl schickt, wobei ja auch die Ladeleistung von ihm vorgegeben wird.

Wird bei "bis" kein Wochentag/Zeit eingegeben, wird dem SHM der Bedarf gemeldet, aber der SAE schaltet den Lader sofort ein. Weil der SAE keine Ladeleistung setzt, lädt der Lader mit voller Leistung.