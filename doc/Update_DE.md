# Update
Das Update der installierten Version des *Smart Appliance Enabler* besteht im Austausch der `SmartApplianceEnabler-*.war`-Datei im Verzeichnis `/opt/sae`. **Dabei ist zu beachten, das sich im Verzeichnis immer nur eine Datei mit der Erweiterung `.war` befindet!** Damit man ggf. auf die alte Version zurückwechseln kann, kann man diese umbenennen beispielsweise in `SmartApplianceEnabler-1.6.19.war.old` anstatt sie zu löschen.

Vor einem Update sollten unbedingt Kopien der [Konfigurationsdateien](ConfigurationFiles_DE.md) erstellt werden, weil diese beim Start der neuen Version automatisch aktualisiert werden und mit diesen ein Zurückwechseln auf die bisher genutzte Version voraussichtlich nicht möglich ist.  

## Download der neuen Version
Die zum Download bereitstehenden Versionen des *Smart Appliance Enabler* finden sich auf der [Projekt-Seite im Bereich Releases](https://github.com/camueller/SmartApplianceEnabler/releases).

## Durchführung des Updates
Die neue `SmartApplianceEnabler-*.war`-Datei kopiert man mit `scp` auf den Raspberry Pi in das Verzeichnis `/opt/sae`. Der Login sollte dabei mit dem User `sae` erfolgen, damit die Datei als Owner und Group gleich `sae` erhält.

*Webmin*: Mit dem [Dateiverwaltung von webmin](Webmin_DE.md) kann man ebenfalls die Datei auf den Raspberry Pi kopieren und auch eine bereits vorhanden Datei umbenennen oder löschen. Das Erstellen von Kopien von Dateien erfolgt mit Menüpunkten "Copy" und "Paste". 

## Starten der aktualisierten Version
Nachdem die zu verwendende `SmartApplianceEnabler-*.war`-Datei platziert worden ist, muss der *Smart Appliance Enabler*-Prozess neu gestartet werden. Das passiert mit folgendem Befehl:

```console
sudo systemctl restart smartapplianceenabler.service
```

*Webmin*: Mit der [Dienstverwaltung von webmin](Webmin_DE.md) kann der *Smart Appliance Enabler* ebenfalls restarted werden. 
