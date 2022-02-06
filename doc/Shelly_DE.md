# Shelly Plug (S)

Mit dem [Shelly plug](https://shelly.cloud/shelly-plug/) oder dem [Shelly Plug S](https://shelly.cloud/knowledge-base/devices/shelly-plug-s/), der mit dem WLAN verbunden ist, lässt sich ein Gerät schalten auch dessen aktueller Stromverbrauch messen.

## Verwendung als Stromzähler

Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                          | Wert                       |
| ----------------------------- |----------------------------|
| Format                        | JSON                       |
| URL                           | http://192.168.1.1/meter/0 |
| Pfad (für Zustand `Leistung`) | $.power                    |

## Verwendung als Schalter

Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):


| Feld        | Wert                                   |
|-------------|----------------------------------------|
| Einschalten | http://192.168.1.xxx/relay/0?turn=on   |
| Ausschalten | http://192.168.1.xxx/relay/0?turn=off  |

# Shelly 3EM

## Verwendung als Stromzähler

Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (IP-Adresse bzw. Hostname ist anzupassen):

| Feld                          | Wert                      |
| ----------------------------- |---------------------------|
| Format                        | JSON                      |
| URL                           | http://192.168.1.1/status |
| Pfad (für Zustand `Leistung`) | $.total_power             |

# Shelly 4PM

Mit dem [Shelly 4 Pro](https://shelly.cloud/shelly-4-pro/), der mit dem WLAN verbunden ist, lassen sich 4 Geräte schalten auch deren aktueller Stromverbrauch messen.

## Verwendung als Stromzähler

Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (x ist durch die die Nummer des Kanals [0,1,2,3] zu ersetzen; IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                            | Wert                                         |
| ----------------------------------------------- |----------------------------------------------|
| Format                                          | JSON                                         |
| URL                                             | http://192.168.1.1/rpc/Switch.GetStatus?id=x |
| Pfad (für Zustand `Leistung`)                   | $.apower                                     |

## Verwendung als Schalter

Für die Verwendung als Stromzähler sind folgende Einstellungen notwendig (x ist durch die die Nummer des Kanals [0,1,2,3] zu ersetzen;IP-Adresse bzw. Hostname ist anzupassen):

| Feld                                            | Wert                               |
| ----------------------------------------------- |------------------------------------|
| Format                                          | JSON                               |
| URL                                             | http://192.168.1.1/relay/x?turn=on |
