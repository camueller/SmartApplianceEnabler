# Alternative Firmware Tasmota
Adapter, mit denen via WLAN beliebige Geräte ein- und ausgeschaltet werden können und die teilweise auch einen Stromzähler integriert haben, basieren oft auf dem Mikrokontroller [ESP8266](https://de.wikipedia.org/wiki/ESP8266).

Meist können diese Geräte nur mit den Cloud-Diensten des Adapter-Herstellers verwendet werden. Glücklicherweise existiert die alternative Firmware [Tasmota](https://github.com/arendst/Sonoff-Tasmota), die ursprünglich für diverse Sonoff-Adapter entwickelt wurde, inzwischen aber für eine Vielzahl von Adaptern verwendet werden kann.

Dazu muss die Tasmota-Firmware allerdings in den Flash-Speicher des Mikrokontrollers geschrieben ("geflasht") werden. Um den Mikrocontroller zum Flashen mit einem PC oder Raspberry Pi zu verbinden ist ein FT232RL-Adapters (kostet zwischen 2 und 5 Euro ) erforderlich.

Zum eigentlichen Flashen benötigt man ein Programm wie [ESPEasy](https://www.heise.de/ct/artikel/ESPEasy-installieren-4076214.html).

Vor dem Flashen löscht man zunächst die alte Firmware:
```console
pi@raspberrypi:~ $ esptool.py --port /dev/ttyUSB0 erase_flash
esptool.py v2.7
Serial port /dev/ttyUSB0
Connecting....
Detecting chip type... ESP8266
Chip is ESP8266EX
Features: WiFi
Crystal is 26MHz
MAC: bc:dd:c2:23:23:84
Uploading stub...
Running stub...
Stub running...
Erasing flash (this may take a while)...
Chip erase completed successfully in 4.0s
Hard resetting via RTS pin...
```

Danach kann man die Tastmota-Firmaware flashen:
```console
pi@raspberrypi:~ $ esptool.py --port /dev/ttyUSB0 write_flash -fs 1MB -fm dout 0x00000 sonoff-DE.bin
esptool.py v2.7
Serial port /dev/ttyUSB0
Connecting....
Detecting chip type... ESP8266
Chip is ESP8266EX
Features: WiFi
Crystal is 26MHz
MAC: bc:dd:c2:23:23:84
Uploading stub...
Running stub...
Stub running...
Configuring flash size...
Compressed 517008 bytes to 356736...
Wrote 517008 bytes (356736 compressed) at 0x00000000 in 33.1 seconds (effective 124.9 kbit/s)...
Hash of data verified.

Leaving...
Hard resetting via RTS pin...
```
