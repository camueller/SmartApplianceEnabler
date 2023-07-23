# Konfigurationsdateien

## Device2EM.xml und Appliances.xml
Die Konfiguration des *Smart Appliance Enabler* befindet sich in zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien:
* die Datei `Device2EM.xml` enthält die Gerätebeschreibungen für den Sunny Home Manager
* die Datei `Appliances.xml` enthält die Gerätekonfigurationen für den *Smart Appliance Enabler*

Die Groß-/Kleinschreibung der Dateinamen muss genau so sein, wie hier angegeben!

Die Dateien müssen sich in dem Verzeichnis befinden, auf das die Variable `SAE_HOME` verweist (normalerweise `/opt/sae`)

### <a name="speichern"></a> Speichern-Schaltfäche
Beim Klick auf die Speichern-Schaltfäche der Web-Oberfläche werden diese Dateien geschrieben. Dadurch wird der *Smart Appliance Enabler* intern neu gestartet, damit die geänderte Konfiguration wirksam wird. Aktuell eingeschaltete Geräte werden in diesem Moment ausgeschaltet um einen definierten Zustand zu erhalten. Auch werden bereits in Anspruch genommene Energie/Laufzeiten von Geräten zurück gesetzt und neu eingeplant. Um das zu vermeiden sollte man die Konfiguration möglichst dann ändern, wenn die zu steuernden Geräte gerade nicht laufen.

### Manuelle Änderungen in den Konfigurationsdateien
Achtung! Manuelle, direkte Änderungen an den Konfigurationsdateien kann dazu führen, dass der *Smart Appliance Enabler* diese Dateien nicht mehr verwenden kann und/oder nicht mehr startet! Bei manuellen Änderungen vorher also unbedingt die Dateien sichern! 

Die XML-Dateien kann man entweder auf dem Raspberry Pi bearbeiten oder man [überträgt sie dazu auf den PC](#scp).

Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden. Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:

Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert.

In das Fenster *XSD Input* muss der Inhalt (nicht die URL selbst!) der nachfolgenden URL kopiert werden:
* beim Prüfen von `Device2EM.xml`: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.3.xsd
* beim Prüfen von `Appliances.xml`: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-2.0.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is valid.* Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

### <a name="scp"></a> Kopieren der Konfigurationsdateien zwischen Raspberry Pi und PC

Zum Transferieren der Dateien zwischen Raspberry Pi und PC kann man unter Linux `scp` bzw. unter Windows [WinSCP](https://winscp.net/eng/download.php) ([Video mit WinSCP Anleitung auf Deutsch](https://www.youtube.com/watch?v=z6yJDMjTdMg)) verwenden.

*Webmin*: Mit dem [File-Manager von Webmin](Webmin_DE.md) kann der Transfer zwischen Raspberry Pi und PC im Browser erfolgen.

## <a name="etc-default-smartapplianceenabler"></a> Server-Konfiguration


In der Datei `/etc/default/smartapplianceenabler` finden sich die Konfigurationseinstellungen für den *Smart Appliance Enabler*. Die darin befindlichen Parameter (z.B. Netzwerk-Adresse, Port, Java-Einstellungen, ...) sind in der Datei selbst dokumentiert. Normalerweise sollte man die Datei unverändert lassen können.

## <a name="log-konfiguration"></a> Log-Konfiguration


Die Konfiguration des Loggings erfolgt in der Datei `/opt/sae/logback-spring.xml`.
