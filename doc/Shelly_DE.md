# Shelly Plug (S)
Mit dem [Shelly plug](https://shelly.cloud/shelly-plug/) oder dem [Shelly Plug S](https://shelly.cloud/knowledge-base/devices/shelly-plug-s/), der mit dem WLAN verbunden ist, lässt sich ein Gerät schalten sowie dessen aktueller Stromverbrauch messen.

## Verwendung als Stromzähler
Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                           | Wert                       |
|------------------------------------------------|----------------------------|
| Format                                         | JSON                       |
| URL                                            | http://192.168.1.1/meter/0 |
| Pfad für Extraktion (bei Parameter `Leistung`) | $.power                    |

## Verwendung als Schalter
Für die Verwendung als Schalter sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                  | Wert                                   |
|-----------------------|----------------------------------------|
| Aktion "Einschalten"  | http://192.168.1.xxx/relay/0?turn=on   |
| Aktion "Ausschalten"  | http://192.168.1.xxx/relay/0?turn=off  |

# Shelly 3EM
## Verwendung als Stromzähler
Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                           | Wert                      |
|------------------------------------------------|---------------------------|
| Format                                         | JSON                      |
| URL                                            | http://192.168.1.1/status |
| Pfad für Extraktion (bei Parameter `Leistung`) | $.total_power             |

# Shelly 4PM
Mit dem [Shelly 4 Pro](https://shelly.cloud/shelly-4-pro/), der mit dem WLAN verbunden ist, lassen sich 4 Geräte schalten sowie deren aktueller Stromverbrauch messen.

## Verwendung als Stromzähler
Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (# ist durch die Nummer des Kanals [0,1,2,3] zu ersetzen; IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                           | Wert                                         |
|------------------------------------------------|----------------------------------------------|
| Format                                         | JSON                                         |
| URL                                            | http://192.168.1.1/rpc/Switch.GetStatus?id=# |
| Pfad für Extraktion (bei Parameter `Leistung`) | $.apower                                     |

## Verwendung als Schalter
Für die Verwendung als Schalter sind folgende Einstellungen notwendig (# ist durch die die Nummer des Kanals [0,1,2,3] zu ersetzen; IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                            | Wert                               |
| ----------------------------------------------- |------------------------------------|
| Format                                          | JSON                               |
| URL                                             | http://192.168.1.1/relay/#?turn=on |

# Shelly Plus 1PM
Mit dem [Shelly Plus 1PM](https://shelly.cloud/shelly-plus-1pm/), der mit dem WLAN verbunden ist, lässt sich ein Gerät schalten sowie dessen aktueller Stromverbrauch messen.

## Verwendung als Stromzähler
Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                           | Wert                                    |
|------------------------------------------------|-----------------------------------------|
| Format                                         | JSON                                    |
| URL                                            | http://192.168.1.1/rpc/Shelly.GetStatus |
| Pfad für Extraktion (bei Parameter `Leistung`) | $.switch:0.apower                       |

## Verwendung als Schalter
Für die Verwendung als Schalter sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                            | Wert                               |
| ----------------------------------------------- |------------------------------------|
| Format                                          | JSON                               |
| URL                                             | http://192.168.1.1/relay/0?turn=on |
