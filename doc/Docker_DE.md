- [Images](#images)
- [Docker-Installation](#docker-installation)
  - [Allgemeine Anleitung](#allgemeine-anleitung)
  - [Raspberrypi](#raspberrypi)
- [Docker-Konfiguration](#docker-konfiguration)
  - [Via docker-compose](#via-docker-compose)
    - [Starten des Containers](#starten-des-containers)
    - [Stoppen des Containers](#stoppen-des-containers)
    - [Anzeige der Consolen-Ausgabe (Logs)](#anzeige-der-consolen-ausgabe-logs)
  - [Via docker](#via-docker)
    - [SAE-Volume](#sae-volume)
    - [Starten des Containers](#starten-des-containers-1)
    - [Stoppen des Containers](#stoppen-des-containers-1)
    - [Status des Containers](#status-des-containers)
    - [Automatisches Starten des Containers durch Systemd](#automatisches-starten-des-containers-durch-systemd)
- [Hilfreiche Befehle](#hilfreiche-befehle)
  - [Shell im laufenden Smart Appliance Enabler-Container ausführen](#shell-im-laufenden-smart-appliance-enabler-container-ausführen)
  - [Konsole-Log anzeigen](#konsole-log-anzeigen)
  - [Smart Appliance Enabler-Logdatei anzeigen](#smart-appliance-enabler-logdatei-anzeigen)
- [Bekannte Probleme](#bekannte-probleme)
  - [Sunny Home Managaer findet Smart Appliance Enabler nicht ohne --net=host](#sunny-home-managaer-findet-smart-appliance-enabler-nicht-ohne---nethost)


# Images 

Für den *Smart Appliance Enabler* gibt es Images für Raspberry Pi und amd64, die jeweils die passende Java-Version beinhalten (deshalb Plaform-spezifische Images).

[avanux/smartapplianceenabler-arm32](https://hub.docker.com/r/avanux/smartapplianceenabler-arm32)
[avanux/smartapplianceenabler-amd64](https://hub.docker.com/r/avanux/smartapplianceenabler-amd64)


# Docker-Installation
Bevor der *Smart Appliance Enabler* als Docker-Container betrieben werden kann, muss Docker installiert sein.

## Allgemeine Anleitung
Eine allgemeine Anleitung für alle offiziell unterstützen Plattformen findet sich bei Docker unter https://docs.docker.com/get-docker/

## Raspberrypi
Die Docker-Installation ist denkbar einfach, muss aber in einer Root-Shell erfolgen:

```console
pi@raspberrypi:~ $ sudo bash
root@raspberrypi:/home/pi# curl -sSL https://get.docker.com | sh
# Executing docker install script, commit: 6bf300318ebaab958c4adc341a8c7bb9f3a54a1a
[...]
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

Nachdem man entsprechend des Vorschlags der Docker-Installation dem User die Rolle `docker` gegeben hat, muss man sich aus- und einloggen, bevor man die Docker-Installation überprüfen kan: 

```console
$ docker version
Client: Docker Engine - Community
 Version:           19.03.13
[...]

Server: Docker Engine - Community
 Engine:
  Version:          19.03.13
[...]

```

# Docker-Konfiguration

Im Image befinden sich der *Smart Appliance Enabler* im Verzeichnis `/opt/sae`.
Für die Konfiguration als Container gibt es zwei Möglichkeiten:
 * Via docker-compose (Konfiguration über YAML-Datei)
 * Via docker (Konfiguration über CLI-Parameter)
 
## Via docker-compose

docker-compose ermöglicht eine komfortable Konfiguration des Containers über eine YAML-Datei.

Im folgenden wird die Definition eines Containers beschrieben, welcher ähnlich wie ein Systemdienst nach Neustart des Hosts automatisch startet und auf Port 9000 erreichbar ist. Der Container läuft mit der Zeitzone des Hosts. Die Konfiguration befindet sich auf dem Host im Unterverzeichnis data.

Auf dem Host wird in einem Ordner für SAE die Datei ```docker-compose.yaml``` mit folgendem Inhalt angelegt:

```yaml
version: '3'
services:
  sae:
     image: avanux/smartapplianceenabler-amd64
     container_name: sae
     restart: unless-stopped
     network_mode: host
     environment:
       JAVA_OPTS: '-Dserver.port=9000'
     volumes:
        - ./data:/opt/sae/data
        - /etc/localtime:/etc/localtime
```

Anschließend wird ein Unterverzeichnis ```data``` erstellt, in dem die Dateien ```Appliances.xml``` und ```Device2EM.xml`` abgelegt werden.

### Starten des Containers
```console
$ docker-compose up -d
Creating sae ... done
```

### Stoppen des Containers
```console
$ docker-compose down
Stopping sae ... done
Removing sae ... done
```
### Anzeige der Consolen-Ausgabe (Logs)
```console
$ docker-compose logs
[...]
sae    | 09:59:45.898 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port(s): 9000 (http)
sae    | 09:59:45.961 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-9000"]
sae    | 09:59:45.964 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
sae    | 09:59:45.964 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/9.0.29]
sae    | 09:59:47.339 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
sae    | 09:59:47.339 [main] INFO  o.s.web.context.ContextLoader - Root WebApplicationContext: initialization completed in 4403 ms
sae    | 09:59:48.377 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-9000"]
sae    | 09:59:48.425 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 9000 (http) with context path ''
[...]
```

## Via docker

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

### Starten des Containers
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

# Hilfreiche Befehle

## Shell im laufenden Smart Appliance Enabler-Container ausführen
Falls man einen Befehl im laufenden Container des *Smart Appliance Enabler* ausführen möchte, kann man mit nachfolgendem Befehl eine entsprechend Shell erzeugen:
```console
pi@raspberrypi:~ $ docker exec -it sae bash
```

## Konsole-Log anzeigen
Folgender Befehl zeigt die Ausgaben des *Smart Appliance Enabler* auf der Konsole an:
```console
pi@raspberrypi:~ $ docker logs sae
```

## Smart Appliance Enabler-Logdatei anzeigen
Zusätzlich zum Konsole-Log erzeugt der *Smart Appliance Enabler* für jeden Tag eine Log-Datei im ```/tmp```-Verzeichnis.
Mit dem nachfolgenden Befehl kann dieses angezeigt werden, wobei das Datum entsprechend angepasst werden muss:
```console
pi@raspberrypi:~ $ docker container exec sae tail -f /tmp/rolling-2019-12-25.log
```

# Bekannte Probleme
## Sunny Home Managaer findet Smart Appliance Enabler nicht ohne --net=host
Der *Smart Appliance Enabler* implementiert das SEMP-Protokoll von SMA. Dieses Protokoll basiert auf UPnP, welches wiederum IP Multicast benötigt.
Aktuell unterstützt Docker nicht die Weiterleitung der Multicast-Pakete vom Host in die Dokker-Container.
Siehe auch https://forums.docker.com/t/multicast-forward-from-host-to-container-for-dlna-discovery/33723
