# HTTP-basierte Stromzähler

Richtige Stromzähler, deren Werte man per HTTP abfragen kann, gibt es meines Wissens nicht. Wenn man allerdings andere Hausautomatisierungen (z.B. FHEM) verwendet, kann man dort eingebundene Stromzähler via HTTP abfragen:
```
<Appliances ...>
    <Appliance ...>
        <HttpElectricityMeter url="http://192.168.1.1/control?device=123&amp;cmd=getpower" factorToWatt="1000" />
    </Appliance>
</Appliances>
```
Zu beachten ist, dass in der URL anstatt des "&"-Zeichens der Ausruck ```"&amp;"``` (ohne Anführungszeichen) verwendet werden muss!
Falls der über HTTP geliefert Verbrauchswert nicht in Watt geliefert wird, muss über den Parameter ```factorToWatt``` der Faktor angegeben werden, mit dem der gelieferte Wert multipliziert werden muss, um den Verbrauch in Watt zu erhalten. Wird beispielsweise der Verbrauch in mW geliefert, muss ```factorToWatt="1000"``` angegeben werden.
Optional können folgende Parameter gesetzt werden:
- measurementInterval in Sekunden (default=60) : Zeitraum, für den der durchschnittliche Verbrauch berechnet wird
- pollInterval in Sekunden (default=10) : die Zeit zwischen zwei Verbrauchsabfragen beim Zähler

Allgemeine Hinweise zu diesem Thema finden sich im Kapitel [Konfiguration](Configuration_DE.md).

## Beispiel Stromverbrauchsmessung über Fritz!Box Home Automation
Die Fritz!Box erlaubt die Abfrage der über eine Steckdose entnommenen Leistung via HTTP. Die Konfiguration dafür würde wie folgt aussehen, wobei die Session ID (ain) über ein zwischengeschaltetes Script gesetzt werden müsste:
```
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <HttpElectricityMeter url="http://192.168.2.1/webservices/homeautoswitch.lua?ain=xxx&amp;switchcmd=getswitchpower" factorToWatt="1000" />
    </Appliance>
</Appliances>
```
