# Modbus
Vor der Einbindung von Modbus-Geräten in den *Smart Appliance Enabler* sollte geprüft werden, ob
- die Kommunikation mit dem Gerät via Modbus/TCP funktioniert
- die relevanten Register die erwarteten Daten liefern

Dazu eignet sich das Windows-Programm [Simply Modbus TCP Client](https://www.simplymodbus.ca/TCPclient.htm), dessen Demo-Version zwar funtional nicht eingeschränkt ist, aber nach sechs Modbus-Nachrichten einen Neustart des Programms erfordert.

Folgende Angaben müssen dabei der Modbus-Beschreibung des jeweiligen Geätes entnommen werden:
- Slave ID bzw. Slave-Adresse
- Register-Adresse
- Anzahl der Register bzw. Datenwörter
- Function code
- Register-Grösse
- Byte-Reihenfolge
- Datenwort-Reihenfolge

Verbindung herstellen: 
- Mode sollte auf `TCP` stehen
- IP-Adresse des Modbus-Gerätes eingeben
- Port sollte immer auf 502 gesetzt bleiben
- nach Klick auf `CONNECT` sollte der Status mit `CONNECTED` angezeigt werden

Als nächstes die `Slave ID` eingeben - sie ist spezifisch für das Modbus-Gerät und bleibt für alle Anfragen an dieses Gerät gleich. 

Register auslesen:
- `First Register` ist die Register-Adresse als Dezimalwert (ggf. [umrechnen](https://www.rapidtables.com/convert/number/hex-to-decimal.html), falls in der Geräte-Beschreibung die Hexadezimalwerte angegeben sind)
- `No. of Regs` ist die Anzahl der Register bzw. Datenwörter und meist 1, max. 4
- `2 byte ID` nicht selektieren
- `Function code` auswählen
- `minus offset` muss immer auf 0 stehen!
- `register size` auswählen
- nach Klick auf `Send` sollte keine Fehlermeldung sondern eine Response angezeigt werden

Durch Variation von
- `High byte first`
- `High word first`
- Response data type (erste Spalte in der Tabelle rechts) 

muss erreicht werden, dass in Spalte `results` der korrekte Wert angezeigt wird.

![SimplyModbusTCPClient](../pics/SimplyModbusTCPClient.png)

# Modbus im Smart Appliance Enabler
## Allgemein
Für jeden Modbus-basierten Zähler/Schalter/Wallbox muss ein konfigurierter [Modbus/TCP](Settings_DE.md#modbus) ausgewählt werden.

Ausserdem muss die **Slave-Adresse** des Modbus-Gerätes angegeben werden.

Grundsätzlich ist die Angabe jeder Slave-Adresse oder Register-Adresse als Hexadezimalzahl (mit "0x" am Anfang) oder als Dezimalzahl (ohne "0x" am Anfang) möglich.

Für jedes Modbus-Register sind folgende Angaben erforderlich:
- `Register-Adresse`
- `Register-Typ` bzw. Function-Code
- `Wert-Typ`: legt fest, welches Format der Wert im Register hat
Zahlenwerte mit hoher Genauigkeit benötigen manchmal 2 oder 4 Datenwörter. In diesen Fällen kann auch die Byte-Reihenfolge (Big Endian / Little Endian) konfiguriert werden.

Beim Finden der richtigen Konfiguration hilft Ausprobieren: Eine zu testende Kombination aus `Register-Adresse`, `Register-Typ` und `Wert-Typ` einstellen und im Log prüfen, welcher Wert aus dem Registerinhalt ermittelt wurde. Ggf. muss auch die Anzahl der `Datenwörter` und die `Byte-Reihefolge` variiert werden. Ziel ist, zumindest die richtige Ziffernfolge zu ermitteln, bei der nur noch das Komma an der falschen Stelle steht. Dies kann man abschliessend durch [Setzen eines Umrechnungsfaktors](ModbusMeter_DE.md) korrigieren. 

## Modbus-Protokoll
### Modbus/TCP
Die Konfiguration von Modbus/TCP erfolgt in den [Einstellungen](Settings_DE.md#modbus).

### Modbus/RTU
*Smart Appliance Enabler* unterstützt das [Modbus](https://de.wikipedia.org/wiki/Modbus)-Protokoll lediglich in der Ausprägung Modbus/TCP. Allerdings können Modbus/RTU-Geräte angeschlossen werden, wenn man einen **USB-Modbus-Adapter** (manchmal auch als USB-RS485-Adapter bezeichnet) verwendet. In diesem Fall benötigt man allerdings zusätzlich ein Modbus/TCP zu Modbus/RTU Gateway wie z.B. das frei verfügbare [mbusd](https://github.com/3cky/mbusd), dessen Installation nachfolgend beschrieben ist.

#### Aktivieren/Starten des `mbusd` nach Automatischer Installation
Bei der automatischen Installation wird der `mbusd` bereits gebaut und installiert, aber zur Vermeidung unnötiger Einträge im Syslog nicht gestartet und im Boot-System aktiviert.
Das lässt sich bei Bedarf mit den beiden folgenden Befehlen nachholen, nachdem ein **USB-Modbus-Adapter** angeschlossen wurde:

```bash
$ sudo systemctl start mbusd@ttyUSB0.service
$ sudo systemctl enable mbusd@ttyUSB0.service
```

#### Manuelle Installation
Falls noch nicht installiert, muss `git` und `cmake` installiert werden:

```bash
$ sudo apt update
$ sudo apt install git cmake
```

Für den Build wird in das Verzeichnis `/tmp` gewechselt:

```bash
$ cd /tmp
```

Damit können jetzt die Sourcen aus dem Git-Repository geholt werden:

```bash
$ git clone https://github.com/camueller/mbusd.git
Cloning into 'mbusd'...
remote: Enumerating objects: 22, done.
remote: Counting objects: 100% (22/22), done.
remote: Compressing objects: 100% (19/19), done.
remote: Total 775 (delta 7), reused 12 (delta 3), pack-reused 753
Receiving objects: 100% (775/775), 986.62 KiB | 540.00 KiB/s, done.
Resolving deltas: 100% (480/480), done.
```

Als nächstes wird im Verzeichnis des Git-Klone das Build-Verzeichnis erstellt, dorthin gewechselt und der Build konfiguriert:

```bash
$ mkdir mbusd/build
$ cd mbusd/build
$ cmake -DCMAKE_INSTALL_PREFIX=/usr ..
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

```bash
$ make
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

```bash
$ sudo make install
[100%] Built target mbusd
Install the project...
-- Install configuration: ""
-- Installing: /usr/bin/mbusd
-- Installing: /usr/share/man/man8/mbusd.8
-- Installing: /etc/mbusd/mbusd.conf.example
-- Installing: /lib/systemd/system/mbusd@.service
```

Der `systemd` muss reinitialisiert werden, damit er die gerade installierte Service-Definition des `mbusd` finden kann:

```bash
$ sudo systemctl daemon-reload
```

Jetzt kann die Konfiguration für den mbusd erstellt werden, ausgehend von der installierten Beispiel-Datei:

```bash
$ cd /etc/mbusd/
$ sudo cp mbusd.conf.example mbusd-ttyUSB0.conf
```

Jetzt steht einem Start des `mbusd` nichts mehr im Wege:

```bash
$ sudo systemctl start mbusd@ttyUSB0.service
```

Nachfolgend ist zu sehen, wie überprüft werden kann, ob der `mbusd` läuft:

```bash
$ sudo systemctl status mbusd@ttyUSB0.service
● mbusd@ttyUSB0.service - Modbus TCP to Modbus RTU (RS-232/485) gateway.
   Loaded: loaded (/lib/systemd/system/mbusd@.service; disabled; vendor preset: enabled)
   Active: active (running) since Sun 2019-03-24 18:33:53 CET; 2s ago
 Main PID: 2807 (mbusd)
   CGroup: /system.slice/system-mbusd.slice/mbusd@ttyUSB0.service
           └─2807 /usr/bin/mbusd -d -v2 -L - -c /etc/mbusd/mbusd-ttyUSB0.conf -p /dev/ttyUSB0

Mar 24 18:33:53 raspberrypi systemd[1]: Started Modbus TCP to Modbus RTU (RS-232/485) gateway..
Mar 24 18:33:53 raspberrypi mbusd[2807]: 24 Mar 2019 18:33:53 mbusd-0.3.1 started...
```

Damit der `mbusd` direkt beim Booten gestartet wird, muss der Service noch aktiviert werden:

```bash
$ sudo systemctl enable mbusd@ttyUSB0.service
Created symlink /etc/systemd/system/multi-user.target.wants/mbusd@ttyUSB0.service → /lib/systemd/system/mbusd@.service.
```

Falls es Probleme mit `mbusd` gibt, kann man den Log-Level erhöhen und die Ausgaben in eine Datei leiten, in dem man in der Datei `/lib/systemd/system/mbusd@.service` die `ExecStart`-Zeile wie folgt ändert:

```
ExecStart=/usr/bin/mbusd -d -v9 -c /etc/mbusd/mbusd-%i.conf -p /dev/%i
```

Danach finden sich sehr detaillierte Log-Ausgaben in der Datei `/var/log/mbus.log`.

### Überprüfung der Modbus-Installation und der mbusd-Installation
Bei Modbus-Problemen sollten zunächst folgende Punkte geprüft werden:
- Wurde bei der Modbus-Verkabelung Plus und Minus vertauscht?
- Wurde der Bus mindestens auf einer Seite mit einem 120-Ohm-Widerstand sauber terminiert?

Nach der Hardware-Überprüfung sollte zunächst die Modbus/RTU-Funktion überprüft werden, bevor Modbus/TCP geprüft wird.

#### Installation von mbpoll
Für die Überprüfung von Modbus/RTU und Modbus/TCP eignet sich das Command-Line-Tool [mbpoll](https://github.com/epsilonrt/mbpoll), das wie folgt installiert wird:

```bash
$ wget -O- http://www.piduino.org/piduino-key.asc | sudo apt-key add -
$ echo 'deb http://raspbian.piduino.org stretch piduino' | sudo tee /etc/apt/sources.list.d/piduino.list
$ sudo apt update
$ sudo apt install mbpoll
```

`mbpoll -h` liefert Hinweise zur Verwendung. Alternativ finden sich diese auch auf der [Projekt-Homepage](https://github.com/epsilonrt/mbpoll#help).

#### Überprüfung Modbus/RTU
*Vor der Überprüfung von Modbus/RTU sollte unbedingt der `mbusd` gestoppt werden, damit `mbpoll` auf den USB-Modbus-Adapter zugreifen kann!*

Danach sollte es möglich sein ein Register auszulesen. Beispielsweise sieht der Aufruf für den `SDM220-Modbus`-Zähler zum Lesen des Zählerstandes wie folgt aus:
- Slave-Adresse (`-a`), hier im Beispiel: 1
- Register (`-r`), hier im Beispiel: 342 (Dezimal! - entspricht 156 hex)
- Register-Typ (`-t`), hier im Beispiel: 32bit Input-Register mit Float-Wert
- Byte-Reihenfolge "Big endian" (`-B`)
- erste Adresse is 0 anstatt 1 (`-0`)
- Register nur einmal auslesen (`-1`)

Der Aufruf sorgt dafür, dass das Register einmal ausgelesen und der Register-Wert (hier im Beispiel: 2952.21) angezeigt wird.

```bash
$ mbpoll -b 9600 -P none -a 1 -r 342 -t 3:float -B -0 -1 /dev/ttyUSB0
mbpoll 1.4-12 - FieldTalk(tm) Modbus(R) Master Simulator
Copyright © 2015-2019 Pascal JEAN, https://github.com/epsilonrt/mbpoll
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under certain conditions; type 'mbpoll -w' for details.

Protocol configuration: Modbus RTU
Slave configuration...: address = [1]
                        start reference = 342, count = 1
Communication.........: /dev/ttyUSB0,       9600-8N1 
                        t/o 1.00 s, poll rate 1000 ms
Data type.............: 32-bit float (big endian), input register table

-- Polling slave 1...
[342]:  2952.21
```

#### Überprüfung Modbus/TCP
*Vor der Überprüfung von Modbus/TCP muss sichergestellt sein, dass der `mbusd` läuft!*

Danach sollte es möglich sein ein Register auszulesen. Beispielsweise sieht der Aufruf für den `SDM220-Modbus`-Zähler zum Lesen des Zählerstandes wie folgt aus:
- Slave-Adresse (`-a`), hier im Beispiel: 1
- Register (`-r`), hier im Beispiel: 342 (Dezimal! - entspricht 156 hex)
- Register-Typ (`-t`), hier im Beispiel: 32bit Input-Register mit Float-Wert
- Byte-Reihenfolge "Big endian" (`-B`) 
- erste Adresse is 0 anstatt 1 (`-0`)
- Register nur einmal auslesen (`-1`)

Der Aufruf sorgt dafür, dass das Register einmal ausgelesen wird und der Register-Wert (hier im Beispiel: 2952.21) angezeigt wird.

```bash
$ mbpoll -a 1 -r 342 -t 3:float -B -0 -1 localhost
mbpoll 1.4-12 - FieldTalk(tm) Modbus(R) Master Simulator
Copyright © 2015-2019 Pascal JEAN, https://github.com/epsilonrt/mbpoll
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under certain conditions; type 'mbpoll -w' for details.

Protocol configuration: Modbus TCP
Slave configuration...: address = [1]
                        start reference = 342, count = 1
Communication.........: localhost, port 502, t/o 1.00 s, poll rate 1000 ms
Data type.............: 32-bit float (big endian), input register table

-- Polling slave 1...
[342]:  2952.21
```
