# Nissan Leaf
Nissan stellt ein API names Carwings bereit. Für dieses existieren Implementierungen u.a. für Python und Dart. Letzteres stammt vom Entwickler der "My Leaf"-Android-App und wird auch dort verwendet. Die Python-Implementierung ist einfacher zu installieren und sollte im Zweifel gewählt werden.

## Python-Implementierung
### Installation
Zunächst muss der Python-Package-Manager installiert werden:
```
sudo apt install python-pip
```

Danach muss die Python-Implementierung des Carwings-API https://github.com/jdhorne/pycarwings2) installiert werden:
```
pip install git+https://github.com/jdhorne/pycarwings2.git
```

In der Datei ```~/.local/lib/python2.7/site-packages/pycarwings2/pycarwings2.py``` muss ggf. die Carwings-URL angepasst werden, da diese sich von Zeit zu Zeit ändert.
Dazu nach einer Zeile mit ```BASE_URL``` am Zeilenanfang suchen und die URL auf folgenden Wert anpassen:
```
BASE_URL = "https://gdcportalgw.its-mo.com/api_v181217_NE/gdc/"
```

Jetzt kann das Verzeichnis für das SOC-Script und Konfigurationsdatei angelegt werden:
```
mkdir /app
```

Die Konfigurationsdatei muss den Namen ```config.ini``` haben mit folgendem Inhalt:
```
[soc]
username = IhrNissan+YOUUsername
password = IhrNissan+YOUPasswort
```

Das eigentliche SOC-Script sollte mit dem Namen ```soc.py``` und folgendem Inhalt angelegt werden:
```
#!/usr/bin/python

import pycarwings2
import time
from ConfigParser import SafeConfigParser
import logging
import sys
import pprint

logging.basicConfig(stream=sys.stdout, level=logging.ERROR)


parser = SafeConfigParser()
candidates = [ 'config.ini', 'my_config.ini' ]
found = parser.read(candidates)

username = parser.get('soc', 'username')
password = parser.get('soc', 'password')

logging.debug("login = %s , password = %s" % ( username , password)  )

print "Prepare Session"
s = pycarwings2.Session(username, password , "NE")
print "Login..."
l = s.get_leaf()

print "get_latest_battery_status"
leaf_info = l.get_latest_battery_status()
print "leaf_info.state_of_charge %s" % leaf_info.state_of_charge
```

### Ausführung
```
cd /app/soc
$ ./soc.py
Prepare Session
Login...
get_latest_battery_status
leaf_info.state_of_charge 76
$
```

## Dart-Implementierung
### Installation
Zunächst muss Dart installiert werden, wie auf https://www.dartlang.org/tools/sdk#install beschrieben:
```
$ sudo sh -c 'curl https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -'
$ sudo sh -c 'curl https://storage.googleapis.com/download.dartlang.org/linux/debian/dart_stable.list > /etc/apt/sources.list.d/dart_stable.list'
$ sudo apt-get update
$ sudo apt-get install dart
```

Danach muss die Dart-Implementierung des Carwings-API (https://gitlab.com/tobiaswkjeldsen/dartcarwings) installiert werden
```
$ git clone https://gitlab.com/tobiaswkjeldsen/dartcarwings
```

Diese Implementierung benötigt verschiedene Bibliotheken, die zunächst installiert werden müssen:
```
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
$
```

Im Verzeichnis ```dartcarwings``` muss jetzt noch das SOC-Script ```soc.dart``` mit folgendem Inhalt angelegt werden, wobei username/password zu ersetzen sind:
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