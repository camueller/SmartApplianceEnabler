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

Um evtuell vorhandene Konfigurationsdateien des *Smart Appliance Enabler* auf dieses Volume zu kopieren, eignet sich dieser Befehl (die erste Zeile erzeugt einen Dummy-Container mit dem Namen _sae_, falls noch keiner läuft): 
```console
docker run -v sae:/opt/sae/data --name sae busybox true
docker cp Appliances.xml sae:/opt/sae/data/
docker cp Device2EM.xml sae:/opt/sae/data/
```

## Betrieb
### Erzeugen eines Containers und Start
Zum direkten Starten des *Smart Appliance Enabler* in einem neuen Container mit dem Namen _sae_ eignet sich folgender Befehl:
```console
pi@raspberrypi:~ $ docker run -v sae:/opt/sae/data --net=host --device /dev/mem:/dev/mem --privileged --name=sae avanux/smartapplianceenabler-arm32
```

Dabei können über die Docker-Variable _JAVA_OPTS_ auch Properties gesetzt werden:
```console
pi@raspberrypi:~ $ docker run -v sae:/opt/sae/data --net=host --device /dev/mem:/dev/mem --privileged --name=sae -e JAVA_OPTS="-Dserver.port=9000" avanux/smartapplianceenabler-arm32
```

### Stoppen des Containers
Zum Stoppen Starten des Containers mit dem *Smart Appliance Enabler* eignet sich folgender Befehl:
```console
pi@raspberrypi:~ $ docker stop sae
sae
```

### Status des Containers
Zum Anzeigen des Status des Containers mit dem *Smart Appliance Enabler* eignet sich folgender Befehl:
```console
sae@raspberrypi:~/docker $ sudo systemctl status smartapplianceenabler-docker.service
● smartapplianceenabler-docker.service - Smart Appliance Enabler Container
   Loaded: loaded (/lib/systemd/system/smartapplianceenabler-docker.service; enabled; vendor preset: enabled)
   Active: active (running) since Wed 2019-12-25 17:18:25 CET; 2min 38s ago
  Process: 9566 ExecStartPre=/bin/sleep 1 (code=exited, status=0/SUCCESS)
 Main PID: 9567 (docker)
    Tasks: 11 (limit: 2200)
   Memory: 23.0M
   CGroup: /system.slice/smartapplianceenabler-docker.service
           └─9567 /usr/bin/docker run -v sae:/opt/sae/data --net=host --device /dev/mem:/dev/mem --privileged --name=sae avanux/smartapplianceenabler-arm32

Dec 25 17:19:13 raspberrypi docker[9567]: 16:19:13.925 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-8080"]
Dec 25 17:19:13 raspberrypi docker[9567]: 16:19:13.930 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
Dec 25 17:19:13 raspberrypi docker[9567]: 16:19:13.933 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/9.0.29]
Dec 25 17:19:20 raspberrypi docker[9567]: 16:19:20.991 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
Dec 25 17:19:20 raspberrypi docker[9567]: 16:19:20.992 [main] INFO  o.s.web.context.ContextLoader - Root WebApplicationContext: initialization completed in 20208 ms
Dec 25 17:19:25 raspberrypi docker[9567]: 16:19:25.964 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-8080"]
Dec 25 17:19:26 raspberrypi docker[9567]: 16:19:26.183 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) with context path ''
Dec 25 17:19:38 raspberrypi docker[9567]: 16:19:38.878 [http-nio-8080-exec-1] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring DispatcherServlet 'dispatcherServlet'
Dec 25 17:19:38 raspberrypi docker[9567]: 16:19:38.879 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Initializing Servlet 'dispatcherServlet'
Dec 25 17:19:38 raspberrypi docker[9567]: 16:19:38.952 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Completed initialization in 69 ms
```

### Automatisches Starten des Containers durch Systemd
Auch wenn der *Smart Appliance Enabler* als Docker-Container betrieben wird, bietet es sich an, den Container als Service des [Systemd](https://de.wikipedia.org/wiki/Systemd) zu verwalten. Dazu dient die Datei ```/lib/systemd/system/smartapplianceenabler-docker.service```, die nachfolgend heruntergeladen und konfiguriert wird:
```console
pi@raspberrypi ~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/smartapplianceenabler-docker.service -P /lib/systemd/system
pi@raspberrypi ~ $ sudo chown root.root /lib/systemd/system/smartapplianceenabler-docker.service
pi@raspberrypi ~ $ sudo chmod 755 /lib/systemd/system/smartapplianceenabler-docker.service
```
 
Damit der *Smart Appliance Enabler* beim Systemstart ebenfalls gestartet wird (via Systemd), muss folgender Befehl ausgeführt werden:
```console
pi@raspberrypi ~ $ sudo systemctl enable smartapplianceenabler-docker.service
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler.service → /lib/systemd/system/smartapplianceenabler.service.
```
Nach diesen Änderungen muss der Systemd dazu gebracht werden, die Service-Konfigurationen neu zu lesen:
```console
pi@raspberrypi ~ $ sudo systemctl daemon-reload
```

## Hilfreiche Befehle

### Shell im laufenden Smart Appliance Enabler-Container ausführen
Falls man einen Befehl im laufenden Container des *Smart Appliance Enabler* ausführen möchte, kann man mit nachfolgendem Befehl eine entsprechend Shell erzeugen:
```console
pi@raspberrypi:~ $ docker exec -it sae bash
```

### Konsole-Log anzeigen
Folgender Befehl zeigt die Ausgaben des *Smart Appliance Enabler* auf der Konsole an:
```console
pi@raspberrypi:~ $ docker logs $(docker ps -q)
```

### Smart Appliance Enabler-Logdatei anzeigen
Zusätzlich zum Konsole-Log erzeugt der *Smart Appliance Enabler* für jeden Tag eine Log-Datei im ```/tmp```-Verzeichnis.
Mit dem nachfolgenden Befehl kann dieses angezeigt werden, wobei das Datum entsprechend angepasst werden muss:
```console
pi@raspberrypi:~ $ docker container exec $(docker ps -q) tail -f /tmp/rolling-2019-12-25.log
```

## Bekannte Probleme
### Sunny Home Managaer findet Smart Appliance Enabler nicht ohne --net=host
Der *Smart Appliance Enabler* implementiert das SEMP-Protokoll von SMA. Dieses Protokoll basiert auf UPnP, welches wiederum IP Multicast benötigt.
Aktuell unterstützt Docker nicht die Weiterleitung der Multicast-Pakete vom Host in die Dokker-Container.
Siehe auch https://forums.docker.com/t/multicast-forward-from-host-to-container-for-dlna-discovery/33723
