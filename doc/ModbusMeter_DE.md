### Modbus/TCP zu Modbus/RTU Gateway bauen

Dieses Kapitel ist nur relevant falls ein Gerät mit Modbus/RTU verwendet werden soll und kann ansonsten übersprungen werden!

```
wget https://sourceforge.net/projects/mbus/files/mbus/0.2.0/mbus-0.2.0.tar.gz/download -O mbus-0.2.0.tar.gz
tar xvfz mbus-0.2.0.tar.gz
cd mbus-0.2.0
./configure
make
sudo make install
sudo mbusd -d -p /dev/ttyUSB0 -s 9600 -v 9
```
In der Log-Datei ```/var/log/smartapplianceenabler.log``` sollten sich für jede Abfrage der aktuellen Leistungsaufnahme folgende Zeilen finden:
```
2016-09-19 19:39:25,010 DEBUG [Timer-0] d.a.s.m.ModbusSlave [ModbusSlave.java:77] F-00000001-000000000001-00: Connecting to modbus modbus//127.0.0.1:502
2016-09-19 19:39:25,051 DEBUG [Timer-0] d.a.s.m.ReadInputRegisterExecutor [ReadInputRegisterExecutor.java:63] F-00000001-000000000001-00: Input register 0C: value=61.766785
2016-09-19 19:39:25,052 DEBUG [Timer-0] d.a.s.a.PollElectricityMeter [PollElectricityMeter.java:41] F-00000001-000000000001-00: timestamps added/removed/total: 1/0/2
```
