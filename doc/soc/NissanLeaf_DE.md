# Nissan Leaf
Nissan stellt ein API names **Carwings** bereit, für das einige Implementierungen:

* die **Python-Implementierung** ist am einfachsten zu installieren und sollte im Zweifel gewählt werden.
* die **Dart-Implementierung** stammt vom Entwickler der "My Leaf"-Android-App und wird auch dort verwendet. 

## Python-Implementierung
### Installation
Zunächst muss der Python-Package-Manager installiert werden:
```console
pi@raspberrypi ~ $ sudo apt install python-pip python-dev
```

Danach muss die Python-Implementierung des Carwings-API https://github.com/jdhorne/pycarwings2) installiert werden. Dabei werden keine Dateien in das aktuelle Verzeichnis geschrieben!
```console
pi@raspberrypi ~ $ pip install git+https://github.com/jdhorne/pycarwings2.git
Collecting git+https://github.com/jdhorne/pycarwings2.git
  Cloning https://github.com/jdhorne/pycarwings2.git to /tmp/pip-sjdnRw-build
Collecting PyYAML (from pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/9f/2c/9417b5c774792634834e730932745bc09a7d36754ca00acf1ccd1ac2594d/PyYAML-5.1.tar.gz (274kB)
    100% |████████████████████████████████| 276kB 495kB/s 
Collecting iso8601 (from pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/ef/57/7162609dab394d38bbc7077b7ba0a6f10fb09d8b7701ea56fa1edc0c4345/iso8601-0.1.12-py2.py3-none-any.whl
Collecting pycrypto (from pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/60/db/645aa9af249f059cc3a368b118de33889219e0362141e75d4eaf6f80f163/pycrypto-2.6.1.tar.gz (446kB)
    100% |████████████████████████████████| 450kB 335kB/s 
Collecting requests (from pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/51/bd/23c926cd341ea6b7dd0b2a00aba99ae0f828be89d72b2190f27c11d4b7fb/requests-2.22.0-py2.py3-none-any.whl (57kB)
    100% |████████████████████████████████| 61kB 1.0MB/s 
Collecting idna<2.9,>=2.5 (from requests->pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/14/2c/cd551d81dbe15200be1cf41cd03869a46fe7226e7450af7a6545bfc474c9/idna-2.8-py2.py3-none-any.whl (58kB)
    100% |████████████████████████████████| 61kB 1.1MB/s 
Collecting chardet<3.1.0,>=3.0.2 (from requests->pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/bc/a9/01ffebfb562e4274b6487b4bb1ddec7ca55ec7510b22e4c51f14098443b8/chardet-3.0.4-py2.py3-none-any.whl (133kB)
    100% |████████████████████████████████| 143kB 921kB/s 
Collecting certifi>=2017.4.17 (from requests->pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/60/75/f692a584e85b7eaba0e03827b3d51f45f571c2e793dd731e598828d380aa/certifi-2019.3.9-py2.py3-none-any.whl (158kB)
    100% |████████████████████████████████| 163kB 801kB/s 
Collecting urllib3!=1.25.0,!=1.25.1,<1.26,>=1.21.1 (from requests->pycarwings2==2.1)
  Downloading https://files.pythonhosted.org/packages/e6/60/247f23a7121ae632d62811ba7f273d0e58972d75e58a94d329d51550a47d/urllib3-1.25.3-py2.py3-none-any.whl (150kB)
    100% |████████████████████████████████| 153kB 846kB/s
Building wheels for collected packages: PyYAML, pycrypto
  Running setup.py bdist_wheel for PyYAML ... done
  Stored in directory: /home/pi/.cache/pip/wheels/ad/56/bc/1522f864feb2a358ea6f1a92b4798d69ac783a28e80567a18b
  Running setup.py bdist_wheel for pycrypto ... done
  Stored in directory: /home/pi/.cache/pip/wheels/27/02/5e/77a69d0c16bb63c6ed32f5386f33a2809c94bd5414a2f6c196
Successfully built PyYAML pycrypto
Installing collected packages: PyYAML, iso8601, pycrypto, idna, chardet, certifi, urllib3, requests, pycarwings2
  Running setup.py install for pycarwings2 ... done
Successfully installed PyYAML-5.1 certifi-2019.3.9 chardet-3.0.4 idna-2.8 iso8601-0.1.12 pycarwings2-2.1 pycrypto-2.6.1 requests-2.22.0 urllib3-1.25.3
```

In der Datei ```~/.local/lib/python2.7/site-packages/pycarwings2/pycarwings2.py``` muss ggf. die Carwings-URL angepasst werden, da diese sich von Zeit zu Zeit ändert. Diese kann man ggf. von der "My Leaf"-App abschauen: https://gitlab.com/tobiaswkjeldsen/carwingsflutter/blob/master/android/app/src/main/java/dk/kjeldsen/carwingsflutter/CarwingsSession.java 
Dazu nach einer Zeile mit ```BASE_URL``` am Zeilenanfang suchen und die URL auf folgenden Wert anpassen:
```console
BASE_URL = "https://gdcportalgw.its-mo.com/api_v181217_NE/gdc/"
```

Jetzt kann das Verzeichnis für das SOC-Script und Konfigurationsdatei angelegt werden:
```console
pi@raspberrypi ~ $ mkdir /app/soc
pi@raspberrypi ~ $ cd /app/soc
```

Die Konfigurationsdatei muss den Namen ```config.ini``` haben mit folgendem Inhalt:
```console
[soc]
username = IhrNissan+YOUUsername
password = IhrNissan+YOUPasswort
```

Das eigentliche SOC-Python-Script sollte mit dem Namen ```soc.py``` und folgendem Inhalt angelegt werden:
```console
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
Damit das SOC-Python-Script von überall aus aufgerufen werden kann und trotzdem die ```config.ini``` gefunden wird, hilft folgendes kleine Shell-Script ```/app/soc/soc.sh```, das vom *Smart Appliance Enabler* aufgerufen wird:
```console
#!/bin/sh
cd /app/soc
./soc.py
```

Beide Scripts müssen noch ausführbar gemacht werden:
```console
pi@raspberrypi:/app/soc $ chmod +x soc.*
```

### Ausführung
```console
pi@raspberrypi:/app/soc $ ./soc.sh
Prepare Session
Login...
get_latest_battery_status
leaf_info.state_of_charge 76
```

Im *Smart Appliance Enabler* wird als SOC-Script angegeben: ```/app/soc/soc.sh```.
Außerdem muss der nachfolgende *Reguläre Ausdruck* angegeben werden, um aus den Ausgaben den eigentlichen Zahlenwert zu extrahieren:
```
.*state_of_charge (\d+)
```

## Dart-Implementierung
### Installation
Zunächst muss Dart installiert werden, wie auf https://www.dartlang.org/tools/sdk#install beschrieben:
```console
$ sudo sh -c 'curl https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -'
$ sudo sh -c 'curl https://storage.googleapis.com/download.dartlang.org/linux/debian/dart_stable.list > /etc/apt/sources.list.d/dart_stable.list'
$ sudo apt-get update
$ sudo apt-get install dart
```

Danach muss die Dart-Implementierung des Carwings-API (https://gitlab.com/tobiaswkjeldsen/dartcarwings) installiert werden
```console
$ git clone https://gitlab.com/tobiaswkjeldsen/dartcarwings
```

Diese Implementierung benötigt verschiedene Bibliotheken, die zunächst installiert werden müssen:
```console
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
```console
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