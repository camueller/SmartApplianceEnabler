# Modbus-Unterstützung

*Smart Appliance Enabler* unterstützt das [Modbus](https://de.wikipedia.org/wiki/Modbus)-Protokoll lediglich in der Ausprägung  Modbus/TCP. Allerdings können Modbus/RTU-Geräte verwendet werden mittels eines Modbus/TCP zu Modbus/RTU Gateway wie z.B. des frei verfügbaren [mbusd](https://sourceforge.net/projects/mbus).

Da ich selbst nur einen einzigen Modbus-Zähler habe und diesen ausschliesslich zum Testen verwende, findet sich nachfolgend lediglich eine Befehlssammlung zur Installation/Starten von mbusd, aber keine Runlevel-Startscripts etc..
```
wget https://sourceforge.net/projects/mbus/files/mbus/0.2.0/mbus-0.2.0.tar.gz/download -O mbus-0.2.0.tar.gz
tar xvfz mbus-0.2.0.tar.gz
cd mbus-0.2.0
./configure
make
sudo make install
sudo mbusd -d -p /dev/ttyUSB0 -s 9600 -v 9
```
