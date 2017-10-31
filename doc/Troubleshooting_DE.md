# Fragen / Probleme

Wenn der *SMA Home Manager* die vom *Smart Appliance Enabler* verwalteten Geräte nicht finden kann, sollen folgende Punkte geprüft werden:

### Erhöhung des Log-Levels
Standardmäßig ist der Log-Level auf INFO gesetzt. Zur Fehlersuche sollte in der Datei `/etc/default/smartapplianceenabler` die alternative Logging-Konfiguration aktiviert werden, indem die Zeile
```
JAVA_OPTS="${JAVA_OPTS} -Dlogging.config=/app/logback-spring.xml"
```
nicht mehr auskommentiert ist. In dieser Datei ist der Log-Level bereits auf DEBUG gesetzt. Nach einem Neustart des *Smart Appliance Enabler* findet sich die Log-Datei im Verzeichnis ```/tmp``` als ```rolling-<datum>.log```, also z.B. ```rolling-2017-10-30.log```.

### Verbindung zwischen Home Manager und Smart Appliance Enabler
Home Manager auf den *Smart Appliance Enabler* müssen sich im gleichen Netz befinden!
Wenn der Log-Level mindestens auf DEBUG gesetzt wurde, kann man in der Log-Datei sehen, wenn der Home Manager auf den *Smart Appliance Enabler* zugreift:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```
### Analyse der Log Dateien des SEMP Moduls im Sunny Home Manager
Siehe http://www.photovoltaikforum.com/geraete-mit-home-manager-koppeln-via-semp-ethernet-p1396300.html#p1396300

### Anwender-Forum
Fragen zur Verwendung des *Smart Appliance Enabler* sollten im SEMP-Thread des *photovoltaik-forums* im SMA Herstellerbereich gestellt werden: https://www.photovoltaikforum.com/geraete-mit-home-manager-koppeln-via-semp-ethernet-t104060.html.

Bitte keine Fragen direkt per Email an mich stellen! Wenn die Fragen im Forum gestellt werden, haben alle was davon und die Chance auf Antworten ist deutlich größer.

### Fehler melden
Bei Fehlern im *Smart Appliance Enabler* sollte ein [Issue](https://github.com/camueller/SmartApplianceEnabler/issues) erstellt werden.
