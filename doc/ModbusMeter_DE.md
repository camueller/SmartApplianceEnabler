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


