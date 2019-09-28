# Shelly Plug

Mit dem [Shelly plug](https://shelly.cloud/shelly-plug/), der mit dem WLAN verbunden ist, lässt sich ein Gerät schalten auch dessen aktueller Stromverbrauch messen.

## Shelly Plug als Stromzähler

Damit der *Smart Appliance Enabler* in der JSON-Antwort den eigentlichen Wert für die Leistungsaufnahme findet, muss folgender Regulärer Ausdruck angegeben werden:

```.*power&quot;:(\d+).*```

## Shelly Plug als Schalter

Zum Schalten des angeschlossenen Gerätes müssen folgende URLs verwendet werden:

_Einschalten_
```
http://192.168.1.xxx/relay/0?turn=on
```

_Ausschalten_
```
http://192.168.1.xxx/relay/0?turn=off
```

