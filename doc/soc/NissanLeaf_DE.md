# Nissan Leaf
Nissan stellt ein API names **Carwings** bereit, für das es einige Implementierungen gibt:

* die **Python-Implementierung** ist am einfachsten zu installieren und sollte im Zweifel gewählt werden.
* die **Dart-Implementierung** stammt vom Entwickler der "My Leaf"-Android-App und wird auch dort verwendet. 

## Python-Implementierung
### Installation
Zunächst muss der Python-Package-Manager installiert werden:

```bash
$ sudo apt-get -y install python3-pip
```

Danach muss die Python-Implementierung des Carwings-API installiert werden. Dabei werden keine Dateien in das aktuelle Verzeichnis geschrieben!

```bash
$ pip3 install pycarwings2 --break-system-packages
Defaulting to user installation because normal site-packages is not writeable
Looking in indexes: https://pypi.org/simple, https://www.piwheels.org/simple
Collecting pycarwings2
  Downloading https://www.piwheels.org/simple/pycarwings2/pycarwings2-2.14-py3-none-any.whl (17 kB)
Collecting PyYAML
  Downloading https://www.piwheels.org/simple/pyyaml/PyYAML-6.0.1-cp311-cp311-linux_armv7l.whl (45 kB)
     ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 45.4/45.4 kB 619.5 kB/s eta 0:00:00
Collecting iso8601
  Downloading https://www.piwheels.org/simple/iso8601/iso8601-2.1.0-py3-none-any.whl (7.5 kB)
Requirement already satisfied: requests in /usr/lib/python3/dist-packages (from pycarwings2) (2.28.1)
Collecting pycryptodome
  Downloading https://www.piwheels.org/simple/pycryptodome/pycryptodome-3.20.0-cp35-abi3-linux_armv7l.whl (2.0 MB)
     ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 2.0/2.0 MB 2.6 MB/s eta 0:00:00
Installing collected packages: PyYAML, pycryptodome, iso8601, pycarwings2
Successfully installed PyYAML-6.0.1 iso8601-2.1.0 pycarwings2-2.14 pycryptodome-3.20.0
```

Jetzt kann das Verzeichnis für das SOC-Script und Konfigurationsdatei angelegt und dorthin gewechselt werden:

```bash
$ mkdir /opt/sae/soc
$ cd /opt/sae/soc
```

Die Konfigurationsdatei muss den Namen `/opt/sae/soc/config.ini` und folgenden Inhalt haben:

```console
[get-leaf-info]
username = IhrNissan+YOUUsername
password = IhrNissan+YOUPasswort
region = NE
```

Das eigentliche SOC-Python-Script sollte mit dem Namen `/opt/sae/soc/soc.py` und folgendem Inhalt angelegt werden:

```python
#!/usr/bin/env python
  
import pycarwings2
import time
from configparser import ConfigParser
import logging
import sys

logging.basicConfig(stream=sys.stdout, level=logging.ERROR)

parser = ConfigParser()
candidates = ['config.ini', 'my_config.ini']
found = parser.read(candidates)

username = parser.get('get-leaf-info', 'username')
password = parser.get('get-leaf-info', 'password')
region = parser.get('get-leaf-info', 'region')
sleepsecs = 30     # Time to wait before polling Nissan servers for update


def update_battery_status(leaf, wait_time=1):
    key = leaf.request_update()
    status = leaf.get_status_from_update(key)
    # Currently the nissan servers eventually return status 200 from get_status_from_update(), previously
    # they did not, and it was necessary to check the date returned within get_latest_battery_status().
    while status is None:
#        print("Waiting {0} seconds".format(sleepsecs))
        time.sleep(wait_time)
        status = leaf.get_status_from_update(key)
    return status


# Main program

logging.debug("login = %s, password = %s, region = %s" % (username, password, region))

print("Prepare Session")
s = pycarwings2.Session(username, password, region)
print("Login...")
leaf = s.get_leaf()

# Give the nissan servers a bit of a delay so that we don't get stale data
time.sleep(1)

print("get_latest_battery_status from servers")
leaf_info = leaf.get_latest_battery_status()

# Give the nissan servers a bit of a delay so that we don't get stale data
time.sleep(1)

print("request an update from the car itself")
update_status = update_battery_status(leaf, sleepsecs)

latest_leaf_info = leaf.get_latest_battery_status()
print(vars(latest_leaf_info))
```

Damit das SOC-Python-Script von überall aus aufgerufen werden kann und trotzdem die `config.ini` gefunden wird, hilft folgendes kleine Shell-Script `/opt/sae/soc/soc.sh`, das vom *Smart Appliance Enabler* aufgerufen wird:

```bash
#!/bin/sh
cd /opt/sae/soc
python3 ./soc.py
```

Das Script muss noch ausführbar gemacht werden:

```bash
$ chmod +x /opt/sae/soc/soc.sh
```

### Ausführung

```bash
$ /opt/sae/soc/soc.sh
Prepare Session
Login...
{'answer': {'status': 200, 'VoltLabel': {'HighVolt': '240', 'LowVolt': '120'}, 'BatteryStatusRecords': {'OperationResult': 'START', 'OperationDateAndTime': '09.Jun 2022 16:05', 'BatteryStatus': {'BatteryChargingStatus': 'NOT_CHARGING', 'BatteryCapacity': '240', 'BatteryRemainingAmount': '168', 'BatteryRemainingAmountWH': '25680', 'BatteryRemainingAmountkWH': '', 'SOC': {'Value': '70'}}, 'PluginState': 'CONNECTED', 'CruisingRangeAcOn': '168000', 'CruisingRangeAcOff': '171000', 'TimeRequiredToFull': {'HourRequiredToFull': '13', 'MinutesRequiredToFull': '30'}, 'TimeRequiredToFull200': {'HourRequiredToFull': '9', 'MinutesRequiredToFull': '30'}, 'TimeRequiredToFull200_6kW': {'HourRequiredToFull': '4', 'MinutesRequiredToFull': '0'}, 'NotificationDateAndTime': '2022/06/09 14:05', 'TargetDate': '2022/06/09 14:05'}}, 'battery_capacity': '240', 'battery_remaining_amount': '168', 'charging_status': 'NOT_CHARGING', 'is_charging': False, 'is_quick_charging': False, 'plugin_state': 'CONNECTED', 'is_connected': True, 'is_connected_to_quick_charger': False, 'cruising_range_ac_off_km': 171.0, 'cruising_range_ac_on_km': 168.0, 'time_to_full_trickle': datetime.timedelta(seconds=48600), 'time_to_full_l2': datetime.timedelta(seconds=34200), 'time_to_full_l2_6kw': datetime.timedelta(seconds=14400), 'battery_percent': 70.0, 'state_of_charge': '70'}
```

Im *Smart Appliance Enabler* wird das SOC-Script wie folgt konfiguriert:

| Feld                                | Wert                             |
|-------------------------------------|----------------------------------|
| Dateiname mit Pfad                  | `/opt/sae/soc/soc.sh`            |
| Regex für SOC-Extraktion            | `.*state_of_charge': '(\d+).*`   |
| Regex für Verbindungsstatus-Prüfung | `.*PluginState': '(CONNECTED).*` |

### Hinweis
In der Datei `~/.local/lib/python3.5/site-packages/pycarwings2/pycarwings2.py` muss ggf. die Carwings-URL angepasst werden, da diese sich von Zeit zu Zeit ändert. Diese kann man ggf. von der ["My Leaf"-App abschauen](https://gitlab.com/tobiaswkjeldsen/carwingsflutter/blob/master/android/app/src/main/java/dk/kjeldsen/carwingsflutter/CarwingsSession.java). 
Dazu nach einer Zeile mit `BASE_URL` am Zeilenanfang suchen und die URL auf folgenden Wert anpassen:

```bash
BASE_URL = "https://gdcportalgw.its-mo.com/api_v181217_NE/gdc/"
```

## Dart-Implementierung
### Installation
Zunächst muss Dart wie auf https://www.dartlang.org/tools/sdk#install beschrieben installiert werden:

```bash
$ sudo sh -c 'curl https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -'
$ sudo sh -c 'curl https://storage.googleapis.com/download.dartlang.org/linux/debian/dart_stable.list > /etc/apt/sources.list.d/dart_stable.list'
$ sudo apt-get update
$ sudo apt-get install dart
```

Danach muss die Dart-Implementierung des Carwings-API (https://gitlab.com/tobiaswkjeldsen/dartcarwings) installiert werden

```bash
$ git clone https://gitlab.com/tobiaswkjeldsen/dartcarwings
```

Diese Implementierung benötigt verschiedene Bibliotheken, die zunächst installiert werden müssen:

```bash
$ cd dartcarwings
$ /usr/lib/dart/bin/pub get
Resolving dependencies... (8.2s)
+ analyzer 0.34.2
+ args 1.5.1
+ async 2.0.8
[...]
Changed 53 dependencies!
Precompiling executables... (6.8s)
Precompiled test:test.
```

Im Verzeichnis `dartcarwings` muss jetzt noch das SOC-Script `soc.dart` mit folgendem Inhalt angelegt werden, wobei username/password zu ersetzen sind:

```
import 'package:http/http.dart' as http;
import 'package:dartcarwings/dartcarwings.dart';

main() {
  CarwingsSession session = new CarwingsSession(debug: true);

  session
      .login(
          username: "HereComesMyUsername",
          password: "HereComesMyPassword",
          blowfishEncryptCallback: (String key, String password) async {
            // No native support for Blowfish encryption with Dart
            // Use external service
            http.Response response = await http.get(
                "https://wkjeldsen.dk/nissan/blowfish.php?password=$password&key=$key");
            return response.body;
          })
      .then((vehicle) {
    vehicle.requestBatteryStatusLatest().then((battery) {
      print(battery.batteryPercentage);
    });
  });
}
```
