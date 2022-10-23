# Einschaltoption

Die Einschaltoption kann dazu verwendet werden, die [SG-Ready-Funktion von Wärmepumpen](https://www.waermepumpe.de/normen-technik/sg-ready/) optimial zu unterstützen.

Das Schalten des SG-Ready-Eingangs entspricht einem Angebot an die Wärmepumpe, das diese nicht annehmen muss. Damit eine Nicht-Annahme der Einschaltoption nicht dazu führt, dass andere Geräte unter Kontrolle des *Sunny Home Manager* unnötig ausgebremst werden, füht die Nicht-Annahme der Einschaltoption innerhalb einer bestimmten Zeit dazu, dass das aktuelle Zeitfenster freigegeben wird.

![Einschaltoption](../pics/fe/Einschaltoption.png)

Dazu müssen **Wärmepumpe und SG-Ready jeweils als eigenständiges Gerät** im *Smart Appliance Enabler* angelegt werden.

Beide Geräte teilen sich einen physischen Zähler teilen, der im *Smart Appliance Enabler* als [Master/Slave-Zähler](MasterSlaveMeter_DE.md) beiden Geräten zugeordnet ist. Der Master-Zähler muss so konfiguriert sein, dass die Leistungsaufnahme dem Slave zugeordnet wird, wenn der Slave-Schalter eingeschaltet ist:

![Master-Zähler bei Einschaltoption](../pics/fe/EinschaltoptionMasterMeter.png)

Der Schalter des Gerätes _Wärmepumpe_  schaltet und/oder signalisiert den Schaltzustand der Wärmepumpe während der Schalter des Gerätes _SG-Ready-Eingang_ den SG-Ready-Eingang schaltet.

Für die SG-Ready-Funktion wird ein [Zeitplan mit Überschussenergie](Schedules_DE.md) verwendet, welcher im *Smart Appliance Enabler* dem Gerät _SG-Ready-Eingang_ zugewiesen sein muss.

Sobald ein Einschaltbefehl für das Gerät _SG-Ready-Eingang_ vom *Sunny Home Manager* empfangen wird, wird der SG-Ready-Eingang geschaltet. Wenn innerhalb der **Dauer der Einschalterkennung** die Leistungsaufname des Gerätes _Wärmepumpe_ den in der **Leistungsschaltgrenze** angegebenen Wert übersteigt, wird dies als Einschalten des Gerätes erkannt und das Zeitfenster, welches zum Einsschalten des SG-Ready-Eingangs geführt hat, bleibt aktiv. Andernfalls wird nach Ablauf der **Dauer der Einschalterkennung** das aktuelle Zeitfenster freigegeben.

Nachdem das Einschalten des Gerätes _Wärmepumpe_ erkannt wurde, ist die Ausschalterkennung aktiv. Das bedeutet, dass das Ausschalten des des Gerätes _Wärmepumpe_ erkannt wird, sobald die Leistungsaufnahme für die **Dauer der Ausschalterkennung** unterhalb der **Leistungsschaltgrenze** bleibt. Wenn das Ausschalten erkannt wurde, wird das aktuelle Zeitfenster freigegeben.


## Log
... wird noch ergänzt
