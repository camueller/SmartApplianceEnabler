# Administration von Raspberry Pi und Smart Appliance Enabler mit `webmin`
Standardmässig wird [webmin](https://www.webmin.com) installiert, wodurch der Raspberry Pi mittels Web-Browser administriert werden kann. Das ist sehr hilfreich, wenn man sich nicht mit Linux auskennt.

`webmin` ist erreichbar unter der URL: `https://raspi:10000`, wobei "raspi" durch Hostname oder IP-Adresse des Raspberry PI zu ersetzen ist. Aktuell scheint das Zertifikat nicht gültig zu sein, weshalb man im Web-Browser bestätigen muss, dass man trotzdem *webmin* aufrufen möchte.

Danach erscheint die Anmeldeseite von *webmin*, wo man sich mit dem Benutzer `sae` und dem während der Installation vergebenen Passwort anmelden kann.

![Login](../pics/webmin/login.png)

## Dashboard
Nach der erfolgreichen Anmeldung gelangt man zum **Dashboard**, das einen guten Überblick über den aktuellen Systemzustand bietet:

![Dashboard](../pics/webmin/dashboard.png)

## Dateiverwaltung
Der File Manager ermöglicht das Verwalten der Dateien, wobei hier insbesondere das Verzeichnis `/opt/sae` interessant ist. Nachdem man dieses Verzeichnis links in der Baumansicht angeklickt hat, werden rechts die in dem Verzeichnis befindlichen Dateien angezeigt. 

Durch Klick auf eine Datei mit der rechten Maustaste werden die möglichen Datei-Operationen angezeigt:

![File Manager](../pics/webmin/file_manager.png)

Durch Klick auf das File-Menü oben werden auch Verzeichnis-Operationen angezeigt, wie beispielsweise `Upload to current directory`:

![File Menu](../pics/webmin/file_menu.png)

## Dienstverwaltung
Der *Smart Appliance Enabler* ist ein Dienst, der durch die Dienstverwaltung des Raspberry Pi (systemd) gestartet oder gestoppt wird.

In die Dienstverwaltung gelangt man im Seitenmenü über den Punkt `System` und den Unterpunkt `Bootup and Shutdown`.

Um den *Smart Appliance Enabler* zu starten/stoppen/restarten, muss in der Liste mit den Diensten die Checkbox vor `smartapplianceenabler.service` aktiviert und dann die entsprechende Schaltfläche unterhalb der Liste mit den Diensten angeklickt werden.

![Bootup and Shutdown](../pics/webmin/bootup_and_shutdown.png)

## Command Shell
Die Ausführung von Befehlen ist möglich, indem man im Seitenmenü auf den Punkt `Tools` und den Unterpunkt `Command Shell` klickt.

![Command Shell](../pics/webmin/command_shell.png)

## Log Dateien anzeigen
Die Anzeige von Log-Datei ist möglich, indem man im Seitenmenü auf den Punkt `System` und den Unterpunkt `System Logs` klickt.
Hinter `View log file` kann auch der Pfad zu beliebigen Log-Datei angezeigt werden, wobei man danach noch auf `View` klicken muss.

![System Logs](../pics/webmin/system_logs.png)

Sehr hilfreich ist die `Refresh`-Schaltfläche, die via Drop-Down die Möglichkeit bietet festzulegen, alle wieviel Sekunden die Anzeige aktualisiert werden soll. Zuvor sollte man den Wert für `Last ... lines of` reduzieren (z.B. auf 50), weil sonst die letzten Zeilen möglicherweise nicht sichtbar sind. 

![View Log File](../pics/webmin/system_logs2.png)

## Ausschalten (Shutdown) / Neustart (Reboot)
Um das Risiko einer beschädigten SD-Karte zu minimieren, sollte der Raspberry Pi zum Ausschalten/Neustart nicht einfach vom Strom getrennt werden. Stattdessen sollte im Seitenmenü über den Punkt `System` und den Unterpunkt `Bootup and Shutdown` auf die Seite navigiert werden, auf der sich ganz unten die Schaltflächen `Reboot System` und `Shutdown System` befinden. Durch Klick auf eine dieser beiden Schaltfläche wird die entsprechende Aktion so ausgeführt, dass das Dateisystem auf der SD-Karte geschont wird.

![Reboot and Shutdown](../pics/webmin/reboot_and_shutdown.png)
