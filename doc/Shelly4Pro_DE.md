# Shelly 4 Pro

Mit dem [Shelly 4 Pro](https://shelly.cloud/shelly-4-pro/), der mit dem WLAN verbunden ist, lassen sich 4 Geräte schalten auch deren aktueller Stromverbrauch messen.

## Shelly 4 Pro als Stromzähler

Damit der *Smart Appliance Enabler* in der JSON-Antwort den eigentlichen Wert für die Leistungsaufnahme findet, müssen die folgenden Regulären Ausdrücke angegeben werden:

### Ausgang 1

```"power":([\.\d]+).+"power":.+"power":.+"power":.+```

### Ausgang 2

```"power":.+"power":([\.\d]+).+"power":.+"power":.+```

### Ausgang 3

```"power":.+"power":.+"power":([\.\d]+).+"power":.+```

### Ausgang 4

```"power":.+"power":.+"power":.+"power":([\.\d]+).+```

## Shelly 4 Pro als Schalter

Zum Schalten der angeschlossenen Geräte müssen folgende URLs verwendet werden:

### Ausgang 1

_Einschalten_
```http://192.168.1.xxx/relay/0?turn=on```

_Ausschalten_
```http://192.168.1.xxx/relay/0?turn=off```

### Ausgang 2

_Einschalten_
```http://192.168.1.xxx/relay/1?turn=on```

_Ausschalten_
```http://192.168.1.xxx/relay/1?turn=off```

### Ausgang 3

_Einschalten_
```http://192.168.1.xxx/relay/2?turn=on```

_Ausschalten_
```http://192.168.1.xxx/relay/2?turn=off```

### Ausgang 4

_Einschalten_
```http://192.168.1.xxx/relay/3?turn=on```

_Ausschalten_
```http://192.168.1.xxx/relay/3?turn=off```