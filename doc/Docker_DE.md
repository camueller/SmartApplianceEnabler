# Installation als Docker-Container

Die nachfolgenden Kapitel beschreiben die Installation des *Smart Appliance Enabler* als __Docker Container__ und sollten in der angegebenen Reihenfolge umgesetzt werden.

## Docker-Installation
Bevor der *Smart Appliance Enabler* als Docker-Container betrieben werden kann, muss zunächt Docker selbst installiert sein.

Die Docker-Installation ist denkbar einfach, muss aber in einer Root-Shell erfolgen:

```console
pi@raspberrypi:~ $ sudo bash
root@raspberrypi:/home/pi# curl -sSL https://get.docker.com | sh
# Executing docker install script, commit: 6bf300318ebaab958c4adc341a8c7bb9f3a54a1a
+ sh -c apt-get update -qq >/dev/null
+ sh -c apt-get install -y -qq apt-transport-https ca-certificates curl >/dev/null
+ sh -c curl -fsSL "https://download.docker.com/linux/raspbian/gpg" | apt-key add -qq - >/dev/null
Warning: apt-key output should not be parsed (stdout is not a terminal)
+ sh -c echo "deb [arch=armhf] https://download.docker.com/linux/raspbian stretch stable" > /etc/apt/sources.list.d/docker.list
+ sh -c apt-get update -qq >/dev/null
+ [ -n  ]
+ sh -c apt-get install -y -qq --no-install-recommends docker-ce >/dev/null
+ sh -c docker version
Client: Docker Engine - Community
 Version:           19.03.2
 API version:       1.40
 Go version:        go1.12.8
 Git commit:        6a30dfc
 Built:             Thu Aug 29 06:18:10 2019
 OS/Arch:           linux/arm
 Experimental:      false

Server: Docker Engine - Community
 Engine:
  Version:          19.03.2
  Version:          19.03.2
  API version:      1.40 (minimum version 1.12)
  Go version:       go1.12.8
  Git commit:       6a30dfc
  Built:            Thu Aug 29 06:12:07 2019
  OS/Arch:          linux/arm
  Experimental:     false
 containerd:
  Version:          1.2.6
  GitCommit:        894b81a4b802e4eb2a91d1ce216b8817763c29fb
 runc:
  Version:          1.0.0-rc8
  GitCommit:        425e105d5a03fabd737a126ad93d62a9eeede87f
 docker-init:
  Version:          0.18.0
  GitCommit:        fec3683
If you would like to use Docker as a non-root user, you should now consider
adding your user to the "docker" group with something like:

  sudo usermod -aG docker your-user

Remember that you will have to log out and back in for this to take effect!

WARNING: Adding a user to the "docker" group will grant the ability to run
         containers which can be used to obtain root privileges on the
         docker host.
         Refer to https://docs.docker.com/engine/security/security/#docker-daemon-attack-surface
         for more information.
```

Nachdem man entsprechend des Vorschlags der Docker-Installation dem User die Rolle `docker` gegeben hat, muss man sich aus- und einloggen, bevor man die Dokcer-Installation überprüfen kan: 

```console
$ docker info
Client:
 Debug Mode: false

Server:
 Containers: 0
  Running: 0
  Paused: 0
  Stopped: 0
 Images: 0
 Server Version: 19.03.2
 Storage Driver: overlay2
  Backing Filesystem: extfs
  Supports d_type: true
  Native Overlay Diff: true
 Logging Driver: json-file
 Cgroup Driver: cgroupfs
 Plugins:
  Volume: local
  Network: bridge host ipvlan macvlan null overlay
  Log: awslogs fluentd gcplogs gelf journald json-file local logentries splunk syslog
 Swarm: inactive
 Runtimes: runc
 Default Runtime: runc
 Init Binary: docker-init
 containerd version: 894b81a4b802e4eb2a91d1ce216b8817763c29fb
 runc version: 425e105d5a03fabd737a126ad93d62a9eeede87f
 init version: fec3683
 Security Options:
  seccomp
   Profile: default
 Kernel Version: 4.9.80-v7+
 Operating System: Raspbian GNU/Linux 9 (stretch)
 OSType: linux
 Architecture: armv7l
 CPUs: 4
 Total Memory: 927.3MiB
 Name: raspberrypi
 ID: N46Q:WC5N:PZRB:UHCB:XKJQ:6QH3:XUAJ:6Q77:WDUA:NIVX:37UD:UNHW
 Docker Root Dir: /var/lib/docker
 Debug Mode: false
 Registry: https://index.docker.io/v1/
 Labels:
 Experimental: false
 Insecure Registries:
  127.0.0.0/8
 Live Restore Enabled: false

WARNING: No memory limit support
WARNING: No swap limit support
WARNING: No kernel memory limit support
WARNING: No kernel memory TCP limit support
WARNING: No oom kill disable support
WARNING: No cpu cfs quota support
WARNING: No cpu cfs period support
```

## Images installieren 

Für den *Smart Appliance Enabler* gibt es Images für Raspberry Pi und x86, die jeweils die passende Java-Version beinhalten (deshalb Plaform-spezifische Images).
Zum Installieren des Images für den Raspberry Pi folgender Befehl:
```console
pi@raspberrypi:~ $ docker pull avanux/smartapplianceenabler-arm32
```

Für x86 muss der Befehl wie folgt aussehen:
```console
pi@raspberrypi:~ $ docker pull avanux/smartapplianceenabler-x86
```

Im Image befinden sich der *Smart Appliance Enabler* im Verzeichnis `/opt/sae`.

## Docker-Konfiguration
### SAE-Volume
Der *Smart Appliance Enabler* benötigt ein schreibbares Verzeichnis, in dem er seine Dateien ablegen kann. Dazu wird in Docker das Volume *sae* erzeugt.
```console
pi@raspberrypi:~ $ docker volume create sae
sae
```
Danach sollte das neue Volume in der Liste der vorhanden Volumes enthalten sein:
```console
pi@raspberrypi:~ $ docker volume ls
DRIVER              VOLUME NAME
local               sae
```

Zur Laufzeit wird das Volume unter `/opt/sae/data` gemounted.

## Betrieb
### Manueller Start
Zum direkten Starten des *Smart Appliance Enabler* eignet sich folgender Befehl:
```console
pi@raspberrypi:~ $ docker run -it --rm -v sae:/opt/sae/data --net=host avanux/smartapplianceenabler-arm32
```

Dabei können über die Docker-Variable _JAVA_OPTS_ auch Properties gesetzt werden:
```console
pi@raspberrypi:~ $ docker run -it --rm -v sae:/opt/sae/data --net=host -e "JAVA_OPTS=-Dsae.discovery.disable=true" avanux/smartapplianceenabler-arm32
```

## Hilfreiche Befehle
### Befehl im laufenden SAE-Container ausführen

```console
pi@raspberrypi:~ $ docker container exec $(docker ps -q) ls -al /opt/sae
total 22780
drwxr-xr-x    1 root     root          4096 Oct 27 16:32 .
drwxr-xr-x    1 root     root          4096 Oct 27 13:49 ..
-rw-r--r--    1 root     root      23308408 Oct 27 13:50 SmartApplianceEnabler.war
drwxr-xr-x    2 root     root          4096 Oct 27 12:17 data
-rw-r--r--    1 root     root          2103 Oct 27 13:49 logback-spring.xml
```

### Shell im laufenden SAE-Container ausführen
Falls man einen Befehl im laufenden Container des *Smart Appliance Enabler* ausführen möchte, kann man mit nachfolgendem Befehl eine entsprechend Shell erzeugen:
```console
pi@raspberrypi:~ $ docker run -it --rm -v sae:/opt/sae/data --entrypoint=/bin/sh avanux/smartapplianceenabler-arm32
```

### Konsole-Log anzeigen
Folgender Befehl zeigt die Ausgaben des *Smart Appliance Enabler* auf der Konsole an:
```console
pi@raspberrypi:~ $ docker logs $(docker ps -q)
```

### SAE-Logdatei anzeigen
Zusätzlich zum Konsole-Log erzeugt der *Smart Appliance Enabler* für jeden Tag eine Log-Datei im ```/tmp```-Verzeichnis.
Mit dem nachfolgenden Befehl kann dieses angezeigt werden, wobei das Datum entsprechend angepasst werden muss:
```console
pi@raspberrypi:~ $ docker container exec $(docker ps -q) tail -f /tmp/rolling-2017-10-30.log
```

## Bekannte Probleme
### SHE findet SAE nicht ohne --net=host
Der *Smart Appliance Enabler* implementiert das SEMP-Protokoll von SMA. Dieses Protokoll basiert auf UPnP, welches wiederum IP Multicast benötigt.
Aktuell unterstützt Docker nicht die Weiterleitung der Multicast-Pakete vom Host in die Dokker-Container.
Siehe auch https://forums.docker.com/t/multicast-forward-from-host-to-container-for-dlna-discovery/33723

### GPIO-Zugriff
Wenn im *Smart Appliance Enabler* ein Gerät konfiguriert wird, das auf die GPIO-Port zugreift, erscheint folgender Fehler:
```
16:33:05.837 [Thread-5] ERROR com.pi4j.util.NativeLibraryLoader - Unable to load [libpi4j.so] using path: [/lib/raspberrypi/dynamic/libpi4j.so]
java.lang.UnsatisfiedLinkError: /tmp/libpi4j7547460481529582810.so: libwiringPi.so: cannot open shared object file: No such file or directory
        at java.lang.ClassLoader$NativeLibrary.load(Native Method) ~[na:1.8.0_65]
        at java.lang.ClassLoader.loadLibrary0(ClassLoader.java:1938) ~[na:1.8.0_65]
        at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1821) ~[na:1.8.0_65]
        at java.lang.Runtime.load0(Runtime.java:809) ~[na:1.8.0_65]
        at java.lang.System.load(System.java:1086) ~[na:1.8.0_65]
        at com.pi4j.util.NativeLibraryLoader.loadLibraryFromClasspath(NativeLibraryLoader.java:159) ~[pi4j-core-1.2.jar!/:na]
        at com.pi4j.util.NativeLibraryLoader.load(NativeLibraryLoader.java:105) ~[pi4j-core-1.2.jar!/:na]
        at com.pi4j.wiringpi.Gpio.<clinit>(Gpio.java:189) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.io.gpio.RaspiGpioProvider.<init>(RaspiGpioProvider.java:69) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.io.gpio.RaspiGpioProvider.<init>(RaspiGpioProvider.java:51) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.platform.Platform.getGpioProvider(Platform.java:125) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.platform.Platform.getGpioProvider(Platform.java:118) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.io.gpio.GpioFactory.getDefaultProvider(GpioFactory.java:109) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.io.gpio.impl.GpioControllerImpl.<init>(GpioControllerImpl.java:53) [pi4j-core-1.2.jar!/:na]
        at com.pi4j.io.gpio.GpioFactory.getInstance(GpioFactory.java:91) [pi4j-core-1.2.jar!/:na]
        at de.avanux.smartapplianceenabler.appliance.ApplianceManager.getGpioController(ApplianceManager.java:74) [classes!/:na]
        at de.avanux.smartapplianceenabler.appliance.ApplianceManager.init(ApplianceManager.java:168) [classes!/:na]
        at de.avanux.smartapplianceenabler.appliance.ApplianceManager.startAppliances(ApplianceManager.java:118) [classes!/:na]
        at de.avanux.smartapplianceenabler.appliance.ApplianceManager.run(ApplianceManager.java:88) [classes!/:na]
        at java.lang.Thread.run(Thread.java:745) [na:1.8.0_65]
```
Der *Smart Appliance Enabler* verwendet für den Zugriff auf die PIO-Ports pi4j, welches intern libwiringpi.so verwendet.
Die obige Fehlermeldung besagt, dass libpi4j gefunden wurde, aber nicht geladen werden kann, weil `libwiringPi.so` nicht gefunden wird.
Wenn man dass mit `ldd` überprüft, wird aber `libwiringPi.so` sehr wohl gefunden:
```console
pi@raspberrypi:~ $$ docker container exec $(docker ps -q) ldd /usr/lib/libpi4j.so
                          /lib/ld-musl-armhf.so.1 (0x76f15000)
                          libwiringPi.so => /usr/lib/libwiringPi.so (0x76edc000)
                          libwiringPiDev.so => /usr/lib/libwiringPiDev.so (0x76ec6000)
                          libc.so.6 => /lib/ld-musl-armhf.so.1 (0x76f15000)
```

Wenn man sich den LD_LIBRARY_PATH von Java ausgeben läßt, erhält man:
```
java.library.path=/usr/java/packages/lib/arm:/lib:/usr/lib
```
Da `/usr/lib` enthalten ist, müßten `libwiringPi.so` und `libwiringPiDev.so` vom Java-Prozess geladen werden können.
Dieses Problem werde ich erneut untersuchen, wenn die Umstellung auf eine aktuellere Java-Version abgeschlossen ist (siehe #41). 