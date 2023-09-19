# Modbus
Before integrating Modbus devices into the *Smart Appliance Enabler*, it should be checked whether
- communication with the device works via Modbus/TCP
- the relevant registers provide the expected data

The Windows program [Simply Modbus TCP Client](https://www.simplymodbus.ca/TCPclient.htm) is suitable for this, the demo version of which is functionally not restricted but requires a restart of the program after 6 Modbus messages.

The following information must be taken from the Modbus description of the respective device:
- Slave ID or slave address
- Register address
- Number of registers or data words
- Function code
- Register size
- Byte order
- data word order

Establish connection:
- Mode should be set to `TCP`
- Enter the IP address of the Modbus device
- Port should always be set to 502
- after clicking on `CONNECT` the status should be displayed with `CONNECTED`

Next enter the `Slave ID` - it is specific to the Modbus device and will remain the same for all requests to that device.

Read register:
- `First Register` is the register address as a decimal value (if necessary [convert](https://www.rapidtables.com/convert/number/hex-to-decimal.html), if the hexadecimal values are in the device description are specified)
- `No. of Regs` is the number of registers or data words and usually 1, max. 4
- Do not select `2 byte ID`
- Select `Function code`
- `minus offset` must always be set to 0!
- Select `register size`
- After clicking on `Send` there should be no error message, but a response should be displayed

By varying
- `High byte first`
- `High word first`
- Response data type (first column in the table on the right)

it must be accomplished that the correct value is displayed in the `results` column.

![SimplyModbusTCPClient](../pics/SimplyModbusTCPClient.png)

# Modbus in Smart Appliance Enabler
## General
A configured [Modbus/TCP](Settings_EN.md#modbus) must be selected for each Modbus-based meter/switch/wallbox.

In addition, the **slave address** of the Modbus device must be specified.

In principle, each slave address or register address can be specified as a hexadecimal number (with "0x" at the beginning) or as a decimal number (without "0x" at the beginning).

The following information is required for each Modbus register:
- `Register address`
- `Register type` or function code
- `Value type`: defines the format of the value in the register
  Numerical values with high precision sometimes require 2 or 4 data words. In these cases, the byte order (Big Endian / Little Endian) can also be configured.

Trying out helps to find the right configuration: set a combination of `register address`, `register type` and `value type` to be tested and check in the log which value was determined from the register content. It may also be necessary to vary the number of `data words` and the `byte order`. The aim is to at least determine the correct sequence of digits, in which only the comma is in the wrong place. This can then be corrected by [setting a factor to value](ModbusMeter_EN.md).

## Modbus procokol
### Modbus/TCP
The configuration of Modbus/TCP takes place in the [Settings](Settings_EN.md#modbus).

### Modbus/RTU
*Smart Appliance Enabler* only supports the [Modbus](https://de.wikipedia.org/wiki/Modbus) protocol in the Modbus/TCP version. However, Modbus/RTU devices can be connected using a **USB-Modbus Adapter** (sometimes referred to as USB-RS485 Adapter). In this case, however, you also need a Modbus/TCP to Modbus/RTU gateway such as the freely available [mbusd](https://github.com/3cky/mbusd), the installation of which is described below.

#### Enable/start of `mbusd` after automatic installation
With the automatic installation, the `mbusd` is already built and installed, but not started and activated in the boot system to avoid unnecessary entries in the syslog.
If necessary, this can be done with the following two commands after a **USB-Modbus-Adapter** has been plugged in:
```console
sae@raspberrypi:/~ $ sudo systemctl start mbusd@ttyUSB0.service
sae@raspberrypi:/~ $ sudo systemctl enable mbusd@ttyUSB0.service
```

#### Manual installation
If not already installed, `git` and `cmake` must be installed:
```console
pi@raspberrypi:/tmp $ sudo apt update
pi@raspberrypi:/tmp $ sudo apt install git cmake
```

Change to the `/tmp` directory for the build:
```console
pi@raspberrypi ~ $ cd /tmp
```

The sources can now be fetched from the Git repository:
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

Next change to the directory with the sources, create a build directory there and change to it:
```console
pi@raspberrypi:/tmp $ cd mbusd && mkdir build && cd build
```

Now the build configuration can be created:
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

Now the actual build can finally be started:
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

Installing the build artifacts comes next:
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

The `systemd` needs to be reinitialized so that it can find the just installed service definition of the `mbusd`:
```console
pi@raspberrypi:/tmp/mbusd/build $ sudo systemctl daemon-reload
```

Now the configuration for the `mbusd` can be created, starting from the installed example file:
```console
cd /etc/mbusd/
sudo cp mbusd.conf.example mbusd-ttyUSB0.conf
```

Now nothing stands in the way of starting `mbusd`:
```console
pi@raspberrypi:/etc/mbusd $ sudo systemctl start mbusd@ttyUSB0.service
```

Below is how to check if `mbusd` is running:
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

In order for `mbusd` to be started directly during boot, the service must still be enabled:
```console
pi@raspberrypi:/etc/mbusd $ sudo systemctl enable mbusd@ttyUSB0.service
Created symlink /etc/systemd/system/multi-user.target.wants/mbusd@ttyUSB0.service → /lib/systemd/system/mbusd@.service.
```

If there are problems with `mbusd`, you can increase the log level and direct the output to a file by replacing the `ExecStart` line in the file `/lib/systemd/system/mbusd@.service` like follows changes:
```
ExecStart=/usr/bin/mbusd -d -v9 -c /etc/mbusd/mbusd-%i.conf -p /dev/%i
```

After that there are very detailed log outputs in the file `/var/log/mbus.log`.

### Verification of the Modbus installation and the mbusd installation
In the event of Modbus problems, the following points should be checked first:
- Haven't the plus and minus been mixed up in the Modbus wiring?
- Does the modbus have a 100 ohm resistor at least on one end?

After the hardware check, the Modbus/RTU function should be checked first before checking Modbus/TCP.

#### Install mbpoll
The command line tool [mbpoll](https://github.com/epsilonrt/mbpoll) is suitable for checking Modbus/RTU and Modbus/TCP, which is installed as follows:
```
wget -O- http://www.piduino.org/piduino-key.asc | sudo apt-key add -
echo 'deb http://raspbian.piduino.org stretch piduino' | sudo tee /etc/apt/sources.list.d/piduino.list
sudo apt update
sudo apt install mbpoll
```

`mbpoll -h` provides instructions for use. Alternatively, these can also be found on the [project homepage](https://github.com/epsilonrt/mbpoll#help).

#### Verification of Modbus/RTU
*Before checking Modbus/RTU, `mbusd` should be stopped so that `mbpoll` can access the USB-Modbus adapter!*

After that it should be possible to read a register. For example, the call for the `SDM220-Modbus` meter to get the meter reading looks like this:
- Slave address (`-a`), here in the example: 1
- Register (`-r`), here in the example: 342 (decimal! - corresponds to 156 hex)
- Register type (`-t`), here in the example: 32-bit input register with float value
- Byte order "Big endian" (`-B`)
- first address is 0 instead of 1 (`-0`)
- Read register only once (`-1`)

The call ensures that the register is read once and the register value (here in the example: 2952.21) is displayed.
```
pi@raspberrypi:~ $ mbpoll -b 9600 -P none -a 1 -r 342 -t 3:float -B -0 -1 /dev/ttyUSB0
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

#### Verification of Modbus/TCP
*Before checking Modbus/TCP, make sure `mbusd` is running!*

After that it should be possible to read a register. For example, the call for the `SDM220-Modbus` meter to get the meter reading looks like this:
- Slave address (`-a`), here in the example: 1
- Register (`-r`), here in the example: 342 (decimal! - corresponds to 156 hex)
- Register type (`-t`), here in the example: 32-bit input register with float value
- Byte order "Big endian" (`-B`)
- first address is 0 instead of 1 (`-0`)
- Read register only once (`-1`)

The call ensures that the register is read once and the register value (here in the example: 2952.21) is displayed.
```
pi@raspberrypi:~ $ mbpoll -a 1 -r 342 -t 3:float -B -0 -1 localhost
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
