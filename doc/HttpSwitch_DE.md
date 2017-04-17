# HTTP-basierte Schalter

Richtige Schalter, die man per HTTP schalten kann, gibt es meines Wissens nicht. Wenn man allerdings andere Hausautomatisierungen (z.B. FHEM) verwendet, kann man dort eingebundene Schalter via HTTP setzen.
```
<Appliances ...>
    <Appliance ...>
        <HttpSwitch onUrl="http://192.168.1.19:8083/fhem?cmd=set%20Aquarium%20on" offUrl="http://192.168.1.19:8083/fhem?cmd=set%20Aquarium%20off" />
    </Appliance>
</Appliances>
```
Zu beachten ist, dass in der URL anstatt des "&"-Zeichens der Ausruck ```"&amp;"``` (ohne Anführungszeichen) verwendet werden muss!

Allgemeine Hinweise zu diesem Thema finden sich im Kapitel [Konfiguration](Configuration_DE.md).
