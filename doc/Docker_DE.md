# Installation als Docker-Container

Die nachfolgenden Kapitel beschreiben die Installation des *Smart Appliance Enabler* als __Docker Container__ und sollten in der angegebenen Reihenfolge umgesetzt werden. Dabei sollte beachtet werden, dass dabei ausreichend Platz auf der SD-Karte ist - 4 GB haben sich als nicht ausreichend erwiesen.

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
```
docker pull avanux/smartapplianceenabler-arm32
```

Für x86 muss der Befehl wie folgt aussehen:
```
docker pull avanux/smartapplianceenabler-x86
```

## Docker-Konfiguration
### SAE-Volume
Der *Smart Appliance Enabler* benötigt ein schreibbares Verzeichnis, in dem er seine Dateien ablegen kann. Dazu wird in Docker das Volume *sae* erzeugt.
```
root@resin:/# docker volume create sae
sae
```
Danach sollte das neue Volume in der Liste der vorhanden Volumes enthalten sein:
```
root@resin:/# docker volume ls
DRIVER              VOLUME NAME
local               sae
```
### Init-System (********************* Kapitel ist veraltet und muss überarbeitet werden **************************)
Damit der SAE-Container nach jedem Boot des Raspberry Pi automatisch gestartet wird, muss eine Unit-Datei für SAE in systemd installiert werden.
Weil das root-Filesystem von ResinOS read-only ist, muss dieses zunächst read-write gemountet werden:
```
root@resin:/# mount -o remount,rw /dev/mmcblk0p2 /
```
Jetzt kann die Unit-Datei für SAE in systemd installiert werden:
TODO: **************** Ersetzen durch wget-Aufruf: *******************
```
scp -P22222 run/etc/systemd/system/sae.service root@resin:/lib/systemd/system
```
Jetzt muss systemd dazu gebracht werden, die Unit-Dateien neu einzulesen:
```
root@resin:/# systemctl daemon-reload
```
Abschliessend muss der SAE-Service noch aktiviert werden, damit der Container mit dem *Smart Appliance Enabler* beim Boot automatisch gestartet wird:
```
root@resin:/# systemctl enable sae.service
Created symlink /etc/systemd/system/multi-user.target.wants/sae.service → /lib/systemd/system/sae.service.
```

## Betrieb
### Manueller Start
Zum direkten Staren des *Smart Appliance Enabler* eignet sich folgender Befehl:
```console
docker run -v sae:/app -p 8080:8080 sae
```
Dabei wird das SAE-Volume gemounted und der Por 8080 des SAE auf dem localhost ebenfalls als Port 8080 verfügbar gemacht.
Dabei können über die Docker-Variable _JAVA_OPTS_ auch Properties gesetzt werden:
```console
docker run -v sae:/app -e "JAVA_OPTS=-Dsemp.gateway.address=192.168.178.33" --net=host avanux/smartapplianceenabler-arm32
```

### Manueller Start über Init-System (********************* Kapitel ist veraltet und muss überarbeitet werden **************************)
Zum manuellen Starten des Container mit dem *Smart Appliance Enabler* via Init-System genügt folgender Befehl:
```
systemctl start sae
```
Alternativ kann auch der in der Unit-Datei ```/lib/systemd/system/sae.service``` enthaltende docker-Befehl verwendet werden.

### Konsole-Log anzeigen
Folgender Befehl zeigt die Ausgaben des *Smart Appliance Enabler* auf der Konsole an:
```
docker logs $(docker ps -q)
```

### SAE-Logdatei anzeigen
Zusätzlich zum Konsole-Log erzeugt der *Smart Appliance Enabler* für jeden Tag eine Log-Datei im ```/tmp```-Verzeichnis.
Mit dem nachfolgenden Befehl kann dieses angezeigt werden, wobei das Datum entsprechend angepasst werden muss:
```
docker container exec $(docker ps -q) tail -f /tmp/rolling-2017-10-30.log
```

### Befehl im SAE-Container ausführen
Falls man einen Befehl im laufenden Container des *Smart Appliance Enabler* ausführen möchte, kann man mit nachfolgendem Befehl eine entsprechend Shell erzeugen:
```
docker exec -i -t $(docker ps -q) /bin/bash
```