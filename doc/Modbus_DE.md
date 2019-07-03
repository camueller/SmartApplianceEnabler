# Modbus-Unterstützung

## Modbus/TCP
Die Konfiguration von Modbus/TCP erfolgt in den [Einstellungen](Settings_DE.md#Modbus).

## Modbus/RTU
*Smart Appliance Enabler* unterstützt das [Modbus](https://de.wikipedia.org/wiki/Modbus)-Protokoll lediglich in der Ausprägung Modbus/TCP. Allerdings können Modbus/RTU-Geräte verwendet werden mittels eines Modbus/TCP zu Modbus/RTU Gateway wie z.B. des frei verfügbaren [mbusd](https://sourceforge.net/projects/mbus), dessen Installation nachfolgend beschrieben ist.

Falls noch nicht installiert, muss als Git und cmake installiert werden:
```console
pi@raspberrypi:/tmp $ sudo apt update
pi@raspberrypi:/tmp $ sudo apt install git cmake
```

Für den Build wird in das tmp-Verzeichnis gewechselt:
```console
pi@raspberrypi ~ $ cd /tmp
```

Damit können jetzt die Sourcen aus dem Git-Repository gehole werden:
```console
pi@raspberrypi:/tmp $ git clone https://github.com/camueller/mbusd.git
Cloning into 'mbusd'...
remote: Enumerating objects: 22, done.
remote: Counting objects: 100% (22/22), done.
remote: Compressing objects: 100% (19/19), done.
remote: Total 775 (delta 7), reused 12 (delta 3), pack-reused 753
Receiving objects: 100% (775/775), 986.62 KiB | 540.00 KiB/s, done.
Resolving deltas: 100% (480/480), done.
```

Als nächstes in das Verzeichnis mit den Sourcen wechseln, dort ein build-Verzeichnis erstellen und dortin wechseln:
```console
pi@raspberrypi:/tmp $ cd mbusd && mkdir build && cd build
```

Jetzt kann die Build-Konfiguration erstellt werden:
```console
pi@raspberrypi:/tmp/mbusd/build $ cmake -DCMAKE_INSTALL_PREFIX=/usr ..
-- The C compiler identification is GNU 6.3.0
-- The CXX compiler identification is GNU 6.3.0
-- Check for working C compiler: /usr/bin/cc
-- Check for working C compiler: /usr/bin/cc -- works
-- Detecting C compiler ABI info
-- Detecting C compiler ABI info - done
-- Detecting C compile features
-- Detecting C compile features - done
-- Check for working CXX compiler: /usr/bin/c++
-- Check for working CXX compiler: /usr/bin/c++ -- works
-- Detecting CXX compiler ABI info
-- Detecting CXX compiler ABI info - done
-- Detecting CXX compile features
-- Detecting CXX compile features - done
-- Found UnixCommands: /bin/bash
-- Checking for module 'systemd'
--   Found systemd, version 232
-- systemd services install dir: /lib/systemd/system
-- Looking for cfmakeraw
-- Looking for cfmakeraw - found
-- Looking for cfsetspeed
-- Looking for cfsetspeed - found
-- Looking for cfsetispeed
-- Looking for cfsetispeed - found
-- Looking for time
-- Looking for time - found
-- Looking for localtime
-- Looking for localtime - found
-- Passing HRDATE to compiler space
-- Looking for tty_get_name in util
-- Looking for tty_get_name in util - not found
-- Looking for uu_lock in util
-- Looking for uu_lock in util - not found
-- Systemd service file will be installed to /lib/systemd/system
-- Install prefix      /usr
-- Install bindir:     /usr/bin
-- Install sysconfdir: /etc
-- Install datadir:    /usr/share
-- Configuring done
-- Generating done
-- Build files have been written to: /tmp/mbusd/build
```

Nun kann endlich der eigentliche Build gestartet werden:
```console
pi@raspberrypi:/tmp/mbusd/build $ make
Scanning dependencies of target mbusd
[  8%] Building C object CMakeFiles/mbusd.dir/src/main.c.o
[ 16%] Building C object CMakeFiles/mbusd.dir/src/tty.c.o
[ 25%] Building C object CMakeFiles/mbusd.dir/src/log.c.o
[ 33%] Building C object CMakeFiles/mbusd.dir/src/cfg.c.o
[ 41%] Building C object CMakeFiles/mbusd.dir/src/conn.c.o
[ 50%] Building C object CMakeFiles/mbusd.dir/src/queue.c.o
[ 58%] Building C object CMakeFiles/mbusd.dir/src/modbus.c.o
[ 66%] Building C object CMakeFiles/mbusd.dir/src/crc16.c.o
[ 75%] Building C object CMakeFiles/mbusd.dir/src/state.c.o
[ 83%] Building C object CMakeFiles/mbusd.dir/src/sig.c.o
[ 91%] Building C object CMakeFiles/mbusd.dir/src/sock.c.o
[100%] Linking C executable mbusd
[100%] Built target mbusd
```

Die Installation der Build-Artefakte kommt als Nächstes:
```console
pi@raspberrypi:/tmp/mbusd/build $ sudo make install
[100%] Built target mbusd
Install the project...
-- Install configuration: ""
-- Installing: /usr/bin/mbusd
-- Installing: /usr/share/man/man8/mbusd.8
-- Installing: /etc/mbusd/mbusd.conf.example
-- Installing: /lib/systemd/system/mbusd@.service
```

Der ```systemd``` muss reinitialisiert werden, damit er die gerade installierte Service-Definition des ```mbusd``` finden kann:
```console
pi@raspberrypi:/tmp/mbusd/build $ sudo systemctl daemon-reload
```

Jetzt kann die Konfiguration für den mbusd erstellt werden, ausgehend von der installierten Beispiel-Datei:
```console
cd /etc/mbusd/
sudo cp mbusd.conf.example mbusd-ttyUSB0.conf
```

Jetzt steht einem Start des ```mbusd``` nichts mehr im Wege:
```console
pi@raspberrypi:/etc/mbusd $ sudo systemctl start mbusd@ttyUSB0.service
```

Nachfolgend ist zu sehen, wie überprüft werden kann, ob der ```mbusd``` läuft:
```console
pi@raspberrypi:/etc/mbusd $ sudo systemctl status mbusd@ttyUSB0.service
● mbusd@ttyUSB0.service - Modbus TCP to Modbus RTU (RS-232/485) gateway.
   Loaded: loaded (/lib/systemd/system/mbusd@.service; disabled; vendor preset: enabled)
   Active: active (running) since Sun 2019-03-24 18:33:53 CET; 2s ago
 Main PID: 2807 (mbusd)
   CGroup: /system.slice/system-mbusd.slice/mbusd@ttyUSB0.service
           └─2807 /usr/bin/mbusd -d -v2 -L - -c /etc/mbusd/mbusd-ttyUSB0.conf -p /dev/ttyUSB0

Mar 24 18:33:53 raspberrypi systemd[1]: Started Modbus TCP to Modbus RTU (RS-232/485) gateway..
Mar 24 18:33:53 raspberrypi mbusd[2807]: 24 Mar 2019 18:33:53 mbusd-0.3.1 started...
```

Damit der ```mbusd``` direkt beim Booten gestartet wird, muss der Service noch aktiviert werden:
```console
pi@raspberrypi:/etc/mbusd $ sudo systemctl enable mbusd@ttyUSB0.service
Created symlink /etc/systemd/system/multi-user.target.wants/mbusd@ttyUSB0.service → /lib/systemd/system/mbusd@.service.
```

Falls es Probleme mit ```mbusd``` gibt, kann man den Log-Level erhöhen und die Ausgaben in eine Datei leiten, in dem man in der Datei ```/lib/systemd/system/mbusd@.service``` die ```ExecStart```-Zeile wie folgt ändert:
```
ExecStart=/usr/bin/mbusd -d -v9 -c /etc/mbusd/mbusd-%i.conf -p /dev/%i
```

Danach finden sich sehr detaillierte Log-Ausgaben in der Datei ```/var/log/mbus.log```.
