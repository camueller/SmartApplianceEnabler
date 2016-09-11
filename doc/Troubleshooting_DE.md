# Fragen / Probleme

Wenn der *SMA Home Manager* die vom *Smart Appliance Enabler* verwalteten Geräte nicht finden kann, sollen folgende Punkte geprüft werden:

### Erhöhung des Log-Levels
Standardmäßig ist der Log-Level auf INFO gesetzt. Zur Fehlersuche sollte dieser in der Datei `/etc/default/smartapplianceenabler` auf ALL gesetzt werden, damit in der Log-Datei alle verfügbaren Informationen in der Log-Datei `/var/log/smartapplianceenabler.log` protokolliert werden.

### Verbindung zwischen Home Manager und Smart Appliance Enabler
Home Manager auf den *Smart Appliance Enabler* müssen sich im gleichen Netz befinden!
Wenn der Log-Level mindestens auf DEBUGgesetzt wurde, kann man in der Log-Datei sehen, wenn der Home Manager auf den *Smart Appliance Enabler* zugreift:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```
### Analyse der Log Dateien des SEMP Moduls im Sunny Home Manager
Siehe http://www.photovoltaikforum.com/geraete-mit-home-manager-koppeln-via-semp-ethernet-p1396300.html#p1396300
