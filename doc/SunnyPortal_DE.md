# Sunny Portal
## Hinzugfügen neuer Geräte im Sunny Portal
Bevor der *Sunny Home Manager* ein Gerät steuern oder messen kann, muss dieses im [Sunny Portal](https://www.sunnyportal.com/) hinzugefügt werden.

Dazu muss dort auf der Seite

```
Konfiguration -> Geräteübersicht -> Tab: Übersicht Neugeräte
```

der Button `Geräte aktualisieren` gedrückt werden.

Danach sollte das neue Gerät angezeigt werden:

![Neues Geraet erkannt](../pics/shm/NeuesGeraetErkannt.png)

Durch Drücken des `[+]`-Buttons wird das Gerät hinzugefügt.

Im ersten Schritt kann der Gerätename festgelegt werden - die Vorgabe kommt aus der Konfiguration des Gerätes im *Smart Appliance Enabler*:

![Neues Geraet Geraetename](../pics/shm/NeuesGeraet_Geraetename.png)

Im zweiten Schritt wird lediglich eine Zusammenfassung angezeigt:

![Neues Geraet Zusammenfassung](../pics/shm/NeuesGeraet_Zusammenfassung.png)

Nach Drücken von `Fertigstellen` wird noch eine Bestätigung angezeigt, dass das Gerät hinzugefügt wurde.

Wenn stattdessen die Fehlermeldung angezeigt wird

> Das von Ihrem Sunny Home Manager gefundene Gerät mit der Seriennummer ... ist bereits in einer anderen Anlage registriert und kann deshalb Ihrer Anlage nicht hinzugefügt werden.

... ist die von Ihnen für das Gerät gewählte ID bereits vergeben und es muß eine andere [ID konfiguriert](Appliance_DE.md#id) werden. Danach muss man das Gerät im *Sunny Portal*  erneut hinzufügen.

### <a name="max-devices"></a> Maximale Anzahl der Geräte im Sunny Portal

Aktuell unterstützt der Sunny Home Manager *maximal 12 steuerbare Geräte*. Da der *Smart Appliance Enabler* via SEMP-Protokoll mit dem Sunny Home Manager kommuniziert, sind aus Sicht des Sunny Home Manager alle Geräte im *Smart Appliance Enabler* steuerbare Geräte. Zur Gesamtzahl zählen neben den Geräten, die mit dem SEMP Protokoll angesprochen werden, bspw. auch SMA Bluetooth Funksteckdosen - also alle Geräte, die im Sunny Portal in der Verbraucherübersicht angezeigt werden.

## Vebraucherbilanz
In der *Verbraucherbilanz* sollte ab jetzt das neue Gerät mit seinem Verbrauch aufgeführt werden:

![Verbraucherbilanz](../pics/shm/Verbraucherbilanz.png)

Falls der *Smart Appliance Enabler* für Geräte Timeframes übermittelt, werden diese (ca. 10-15 Minuten später) unter *Prognose und Handlungsempfehlung* angezeigt:

![Prognose](../pics/shm/PrognoseMitEingeplantenGeraeten.png)

Das mögliche Zeitfenster wird dabei durchsichtig angezeigt (im Bild der lilane Balken 8:00 bis 17:00), während die darin geplante Laufzeit nicht durchsichtig ist (im Bild der lila-farbene Balken ca. 13:30 bis 16:30). Der komplette Balken ist transparent, wenn das Gerät ausschließlich Überschussenergie konsumieren möchte (im Bild der orange Balken für eine Wallbox).
