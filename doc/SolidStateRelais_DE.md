# Solid-State-Relais
Zum Schalten von 240V-Geräten durch elektronische Schaltungen eignen sich [**Solid-State-Relais**](https://de.wikipedia.org/wiki/Relais#Halbleiterrelais). Diese gibt es ab ca. 5 Euro zu kaufen.

#### Schaltbeispiel 1: Schaltung eines 240V-Gerätes mittels Solid-State-Relais
Der Aufbau zum Schalten eines 240V-Gerätes (z.B. Pumpe) mittels Solid-State-Relais könnte wie folgt aussehen:

![Schaltbeispiel](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SchaltungSSR.jpg)

Falls ein 400V-Gerät geschaltet werden soll, lässt sich dieses über ein Schütz schalten.

Die Konfiguration für dieses Schaltbeispiel würde wie folgt aussehen:
```
<?xml version="1.0" encoding="UTF-8"?>
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <Switch gpio="4" reverseStates="true" />
    </Appliance>
</Appliances>
```

Allgemeine Hinweise zu diesem Thema finden sich im Kapitel [Konfiguration](Configuration_DE.md).

Wird ein Gerät über GPIO geschaltet via Solid State Relais, findet sich in der Log-Datei ```/var/log/smartapplianceenabler.log``` für jeden Schaltvorgang folgende Zeile:

```
2017-02-25 10:45:03,400 INFO [http-nio-8080-exec-2] d.a.s.a.Switch [Switch.java:56] F-00000001-000000000001-00: Switching on GPIO 4
´´`

Zur Montage des Solid-State-Relais verwende ich [DIN-Schienenhalter](DINSchienenhalter.md), wobei ich auf der PVC-Montageplatte Gummi-Durchführungsringe als Abstandshalter mit Heißkleber befestigt habe. Die Muttern der Nylon-Schrauben zur Befestigung des Solid-State-Relais habe ich (anders als auf dem Foto zu sehen) nicht mehr vorn auf der Kunststoffplatte mit Heißkleber angeklebt, sondern auf der Rückseite. Der Grund dafür ist die Erwärmung der Metallseite des Solid-State-Relais, wodurch sich die mit Heißkleber auf der PVC-Platte befestigte Mutter lösen kann.

![DINSchienenhalterSSR](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/DINSchienenhalterSSR.jpg)
![DINSchienenhalterMitSSR](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/DINSchienenhalterMitSSR.jpg)
