# Anlaufstromerkennung

Viele Geräte können nicht einfach eingeschaltet werden um direkt danach mit ihrer Arbeit zu beginnen, sondern müssen nach dem Einschalten zunächst programmiert werden. Dazu ist es erforderlich, dass das Gerät auch während dieser Zeit mit Strom versorgt wird. Bei Verwendung der Anlaufstromerkennung des *Smart Appliance Enabler* wird deshalb die Stromversorgung des Gerätes nur unterbrochen in der Zeit zwischen Anlaufstromerkennung und dem Empfang der Einschaltempfehlung des *Sunny Home Managers*.

Die **Anlaufstromerkennung** besteht darin, daß unterschieden wird zwischen dem eingeschaltetem Gerät im Ruhezustand und dem eingeschalteten Gerät, das gerade seine Arbeit verrichtet. Der Übergang vom erstgenannten Zustand in den letztgenannten Zustand wird erkannt, wenn der Stromverbrauch für eine konfigurierbare Zeit (Standard: 30 Sekunden, Parameter: *startingCurrentDetectionDuration*) oberhalb einer konfigurierbaren Grenze (Standard: 15W, Parameter: *powerThreshold*) bleibt. Genauso wird der **Abschaltstrom** als Übergang zurück erkannt, wenn der Stromverbrauch für eine konfigurierbare Zeit (Standard: 5 Minuten, Parameter: *finishedCurrentDetectionDuration*) unterhalb dieser konfigurierbaren Grenze bleibt. Damit der Abschaltstrom nicht fälschlicherweise kurz nach der Erkennung des Anlaufstromes erkannt wird, beginnt die Abschaltstromerkennung mit einer Verzögerung (Standard: 10 Minuten, Parameter: *minRunningTime*).

Nach Erkennung des Abschaltstromes wird direkt wieder die Anlaufstromerkennung aktiviert. Dadurch ist es möglich das Gerät innerhalb eines [Schedule](https://github.com/camueller/SmartApplianceEnabler/blob/master/doc/Configuration_DE.md#planung-der-gerätelaufzeiten) mehrmals hintereinander laufen zu lassen.

Die Anlaufstromerkennung des SAE besteht aus einem *Softwere-Schalter*, in den der eigentliche Schalter eingebettet ist. Dadurch läßt sich die Anlaufstromerkennung mit allen physischen Schaltern nutzen, die der *Smart Appliance Enabler* unterstützt. Der *Software-Schalter* unterscheidet dazu zwischen dem Schaltzustand des Gerätes und dem Schaltzustand, wie er sich dem *Sunny Home Manager* darstellt.

## Beispiel mit Solid-State-Relais und S0-Zähler
```
    <Appliance id="F-00000001-000000000001-00">
        <StartingCurrentSwitch startingCurrentDetectionDuration="15">
            <Switch gpio="1" reverseStates="true" />
        </StartingCurrentSwitch>
        <S0ElectricityMeter gpio="2" pinPullResistance="PULL_DOWN" impulsesPerKwh="1000" measurementInterval="300" />
        <Schedule minRunningTime="10800" maxRunningTime="10800">
            <DayTimeframe>
                <Start hour="6" minute="0" second="0"/>
                <End hour="16" minute="0" second="0"/>
            </DayTimeframe>
        </Schedule>
    </Appliance>
```
