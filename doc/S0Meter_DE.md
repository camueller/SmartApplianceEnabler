# Digitale Stromzähler mit S0-Ausgang

Für die Genauigkeit des Zählers ist die Anzahl der Impulse pro kWh wichtig. Die meisten aktuellen Zähler bieten hier 1000imp. Persönlich bin ich sehr zufrieden mit den Zählern von [B+G E-Tech](http://www.bg-etech.de/), die zudem recht günstig sind.

In den Schaltbeispielen ist der für den Stromzähler notwendige **Pull-Down-Widerstand** nicht eingezeichnet, weil dafür die auf dem Raspberry Pi vorhandenen Pull-Down-Widerstände per Software-Konfiguration aktiviert werden.

## Schaltbeispiel 1: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Messen des Stromverbrauchs eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen, wobei diese Schaltung natürlich um einen [Schalter](https://github.com/camueller/SmartApplianceEnabler/blob/master/README.md#schalter) erweitert werden kann, wenn neben dem Messen auch geschaltet werden soll.

![Schaltbeispiel](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SchaltungS0Zaehler.jpg)

Die Konfiguration für dieses Schaltbeispiel würde wie folgt aussehen, wobei der Wert für die Anzahl der Impulse je kWh vom verwendeten Zähler abhängt:
```
<?xml version="1.0" encoding="UTF-8"?>
<Appliances xmlns="http://github.com/camueller/SmartApplianceEnabler/v1.1">
    <Appliance id="F-00000001-000000000001-00">
        <S0ElectricityMeter gpio="0" pinPullResistance="PULL_DOWN" impulsesPerKwh="1000" />
    </Appliance>
</Appliances>
```

Allgemeine Hinweise zu diesem Thema finden sich im Kapitel [Konfiguration](Configuration_DE.md).
