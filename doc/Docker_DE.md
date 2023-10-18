# Docker-Images 
Für den *Smart Appliance Enabler* werden Docker-Images für `arm` (inklusive Raspberry Pi) und `amd64` bereitgestellt, welche jeweils die passende Java-Version beinhalten (Platform-spezifische Images).

Das Repository für dieses Images ist [avanux/smartapplianceenabler](https://hub.docker.com/r/avanux/smartapplianceenabler)

# Docker-Installation
Bevor der *Smart Appliance Enabler* als Docker-Container betrieben werden kann, muss Docker installiert sein.

## Raspberry Pi
Die Docker-Installation ist denkbar einfach:

```bash
curl -sSL https://get.docker.com | sudo sh
```

Am Ende schlägt die Installation vor, einem User-Account die Verwendung von Docker zu ermöglichen:

```bash
...
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

Nachdem entsprechend des Vorschlags der Docker-Installation der User der Gruppe `docker` hinzugefügt wurde, muss man sich aus- und wieder einloggen. Danach kann man die Docker-Installation überprüfen:

```bash
$ docker version
Client: Docker Engine - Community
 Version:           19.03.13
[...]

Server: Docker Engine - Community
 Engine:
  Version:          19.03.13
[...]

```

## Sonstige Platformen
Eine allgemeine Anleitung für die Installation auf allen offiziell unterstützen Plattformen findet sich unter https://docs.docker.com/get-docker/

# Docker-Konfiguration
Im Image befindet sich der *Smart Appliance Enabler* im Verzeichnis `/opt/sae`.

Die Konfigurationsdateien des *Smart Appliance Enabler* (`Appliances.xml` und `Device2EM.xml`) werden im Docker-Volume `sae` abgelegt. Zur Laufzeit wird dieses Volume unter `/opt/sae/data` gemountet. 
 
Für die Konfiguration als Container gibt es zwei Möglichkeiten:
 * Konfiguration mit `docker-compose` auf Basis einer [YAML](https://de.wikipedia.org/wiki/YAML)-Datei (empfohlen!)
 * Konfiguration mit diversen `docker`-Aufrufen
 
Der *Smart Appliance Enabler* implementiert das [SEMP](https://my.sma-service.com/s/article/Description-of-SEMP-Protocol?language=en_US)-Protokoll von SMA. Dieses Protokoll basiert auf UPnP, welches wiederum IP Multicast benötigt. Die nachfolgend beschriebenen Konfigurationen verwenden deshalb ein [`macvlan`-Netz](https://docs.docker.com/network/macvlan/), mit dessen Hilfe der Docker-Container des *Smart Appliance Enabler* eine eigene MAC- und IP-Adresse erhält. Falls das nicht möglich oder gewünscht ist, muss der Docker-Container des *Smart Appliance Enabler* mit `--net=host` gestartet werden.
 
## Konfiguration mit `docker-compose`-Befehlen
`docker-compose` ermöglicht eine komfortable Konfiguration des Containers über eine YAML-Datei.

### Installation von `docker-compose`
 `docker-compose` muss zusätzlich zu `docker` installiert werden.

#### Raspberry Pi
Vor der Installation von `docker-compose` auf dem Raspberry Pi muss der Python-Package-Manager installiert werden:

```bash
$ sudo apt-get -y install python3-pip
```
  
Die eigentliche Installation von `docker-compose` erfolgt danach mit:

```bash
$ sudo pip3 -v install docker-compose
```

#### Sonstige Platformen
Die Beschreibung der Installation `docker-compose` findet sich unter https://docs.docker.com/compose/install .

### YAML-Datei
Für den *Smart Appliance Enabler* existiert eine vorkonfigurierte YAML-Datei, für die ein Verzeichnis angelegt werden muss, um sie danach herunterzuladen:

```bash
$ sudo mkdir -p /etc/docker/compose/smartapplianceenabler
$ sudo wget https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/run/etc/docker/compose/docker-compose.yml \
    -P /etc/docker/compose/smartapplianceenabler
```

Hinweise zu den notwendigen Anpassungen finden sich als Kommentare in der Datei selbst. Das Docker-Volume `sae` wird automatisch beim Start erstellt, falls noch nicht vorhanden.

### `systemd` mit `docker-compose`
Auch wenn der *Smart Appliance Enabler* als Docker-Container betrieben wird, bietet es sich an, den Container als Service des [Systemd](https://de.wikipedia.org/wiki/Systemd) zu verwalten. Dazu dient die Datei ```/etc/systemd/system/smartapplianceenabler-docker-compose.service```, die nachfolgend heruntergeladen und konfiguriert wird:

```bash
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/blob/master/run/lib/systemd/system/smartapplianceenabler-docker-compose.service \
    -P /etc/systemd/system
$ sudo chown root.root /etc/systemd/system/smartapplianceenabler-docker-compose.service
$ sudo chmod 644 /etc/systemd/system/smartapplianceenabler-docker-compose.service
```
 
Damit der *Smart Appliance Enabler* beim Systemstart ebenfalls gestartet wird (via Systemd), muss folgender Befehl ausgeführt werden:

```bash
$ sudo systemctl enable smartapplianceenabler-docker-compose.service
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler-docker-compose.service → /etc/systemd/system/smartapplianceenabler-docker-compose.service.
```

Nach diesen Änderungen muss der Systemd dazu gebracht werden, die Service-Konfigurationen neu zu lesen:

```bash
$ sudo systemctl daemon-reload
```

#### Starten des Containers
```bash
$ sudo systemctl start smartapplianceenabler-docker-compose
```

#### Stoppen des Containers
```bash
$ sudo systemctl stop smartapplianceenabler-docker-compose
```

#### <a name="container-status"></a> Status des Containers


Wenn der Container mit dem *Smart Appliance Enabler* läuft, sollte der Status `active (running)` sein:
```bash
$ sudo systemctl status smartapplianceenabler-docker-compose.service
● smartapplianceenabler-docker-compose.service - Smart Appliance Enabler Container
Loaded: loaded (/etc/systemd/system/smartapplianceenabler-docker-compose.service; enabled; vendor preset: enabled)
Active: active (running) since Sat 2020-12-26 13:04:45 CET; 5 days ago
Main PID: 30810 (docker-compose)
Tasks: 3 (limit: 2063)
CGroup: /system.slice/smartapplianceenabler-docker-compose.service
└─30810 /usr/bin/python3 /usr/local/bin/docker-compose up

Dec 26 13:05:40 raspi docker-compose[30810]: sae    | 13:05:40.622 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Initializing Servlet 'dispatcherServlet'
Dec 26 13:05:40 raspi docker-compose[30810]: sae    | 13:05:40.696 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Completed initialization in 73 ms
```

### Befehle mit `docker-compose`
Für alle nachfolgenden Befehle muss man sich im Verzeichnis mit der zum *Smart Appliance Enabler* gehörenden `docker-compose.yml`-Datei befinden (normalerweise `/etc/docker/compose/smartapplianceenabler`)!

#### Starten der Container
```bash
$ docker-compose up -d
Creating network "macvlan0" with driver "macvlan"
Creating mosquitto ... done
Creating pigpiod   ... done
Creating sae       ... done
```

#### Stoppen der Container
```bash
$ docker-compose down
Stopping sae       ... done
Stopping mosquitto ... done
Stopping pigpiod   ... done
Removing sae       ... done
Removing mosquitto ... done
Removing pigpiod   ... done
Removing network macvlan0
```

#### Anzeige der Consolen-Ausgabe
```bash
$ docker-compose logs
[...]
sae    | 17:06:20.733 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) with context path ''
sae    | 17:06:24.615 [http-nio-8080-exec-1] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring DispatcherServlet 'dispatcherServlet'
sae    | 17:06:24.616 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Initializing Servlet 'dispatcherServlet'
sae    | 17:06:24.708 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Completed initialization in 91 ms[...]
```

Wird zusätzlich die Option `-f` ("follow") angegeben, wird das Log continuierlich ausgegeben. Damit kann also Live verfolgt werden, was der Container gerade macht. Die Log-Ausgabe kann mit `Ctrl-C` abgebrochen werden.


## Konfiguration mit diversen `docker`-Aufrufen
### SAE-Volume
Der *Smart Appliance Enabler* benötigt ein schreibbares Verzeichnis, in dem er seine Dateien ablegen kann. Dazu wird in Docker das Volume *sae* erzeugt.

```bash
$ docker volume create sae
```

Danach sollte das neue Volume in der Liste der vorhanden Volumes enthalten sein:

```bash
$ docker volume ls
DRIVER              VOLUME NAME
local               sae
```

### `macvlan`-Netz einrichten
Die nachfolgenden Befehle (ausser `docker create network ...`) sind nur bis zum nächsten Reboot aktiv und müssen ggf. in einem Init-Script platziert werden.

Zunächst wird ein `macvlan`-Interface mit der Bezeichnung `macvlan0` als Link auf das physische Interface (hier eth0) erstellt:

```bash
$ sudo ip link add macvlan0 link eth0 type macvlan mode bridge
```

Für die IP-Adressen der Docker-Container muss ein Overlay-Netz von IP-Adressen definiert werden, welches den Adressbereich des physischen Interfaces überlagert. Dieser Adressbereich muss vom DHCP-Server ignoriert werden, d.h. er darf diese Adressen niemandem zuteilen!!

Zur Bestimmung möglicher Netze eignet sich der [IP Calculator](http://jodies.de/ipcalc). Nachfolgend wird das `macvlan`-Interface auf eine IP-Adresse aus dem Adressbereich des Overlay-Netzes konfiguriert.

```bash
$ sudo ifconfig macvlan0 192.168.0.223 netmask 255.255.255.0 up
```

Abschließend muss ein Routing-Eintrag für das Overlay-Netz hinzugefügt werden:

```bash
$ sudo ip route add 192.168.0.192/27 dev macvlan0
```

Jetzt kann das Docker-Netzwerk `macvlan0` erstellt werden. Der Parameter `subnet` entspricht dem Netz des physischen Interfaces. Beim Parameter `gateway` handelt sich es um das Ziel der Default-Route (meist die interne IP-Adresses des Internet-Routers). Als Parameter `ip-range` wird das Overlay-Netz angegegeben, wobei im Parameter `aux-address` die IP-Adresse des `macvlan`-Interfaces angegeben wird, damit sie keinem Docker-Container zugewiesen wird.  Beim Parameter `parent` wird das physische Interface angegeben. Damit sieht der Befehl wie folgt aus:

```bash
$ docker network create \
    -d macvlan \
    --subnet=192.168.0.0/24 \
    --gateway=192.168.0.1 \
    --ip-range 192.168.0.192/27 \
    --aux-address 'host=192.168.0.223' \
    -o parent=eth0 \
    -o macvlan_mode=bridge \
    macvlan0
```

### Start des MQTT-Brokers
Der *Smart Appliance Enabler* benötigt einen MQTT-Broker. Diesen kann man ebenfalls als Docker-Container starten, wobei man ihm eine IP-Adresse aus dem Docker-Netzwerk `macvlan0` zuweisen muss: 

```bash
$ docker run \
    --rm \
    --detach \
    --network macvlan0 \
    --ip 192.168.0.201 \
    --name mosquitto \
    eclipse-mosquitto \
    mosquitto -c /mosquitto-no-auth.conf
```

### Start des pigpiod 
Der *Smart Appliance Enabler* benötigt `pigpiod` für den Zugriff auf die GPIOs des Raspberry Pi. Diesen kann man ebenfalls als Docker-Container starten, wobei man ihm eine IP-Adresse aus dem Docker-Netzwerk `macvlan0` zuweisen muss:

```bash
$ docker run \
    --rm \
    --detach \
    --network macvlan0 \
    --ip 192.168.0.202 \
    --name pigpiod \
    --privileged \
    --device /dev/gpiochip0 \
    zinen2/alpine-pigpiod
```

### Start/Stop/Status des Smart Appliance Enablers
#### Starten des Containers
Beim Starten des *Smart Appliance Enabler* in einem neuen Container mit dem Namen _sae_ muss dem Docker-Container eine IP-Adresse aus dem Docker-Netzwerk `macvlan0` zugewiesen werden:  

```bash
$ docker run \
    -v sae:/opt/sae/data \
    --network macvlan0 \
    --ip 192.168.0.200 \
    --publish 8080:8080 \
    --privileged \
    --name=sae \
    avanux/smartapplianceenabler
```

Dabei können über die Docker-Variable _JAVA_OPTS_ auch Properties gesetzt werden:

```bash
$ docker ... \
    -e JAVA_OPTS="-Dserver.port=9000" \
    avanux/smartapplianceenabler
```

#### Stoppen des Containers

```bash
$ docker stop sae
```

### Automatisches Starten des Containers durch Systemd
Auch wenn der *Smart Appliance Enabler* als Docker-Container betrieben wird, bietet es sich an, den Container als Service des [Systemd](https://de.wikipedia.org/wiki/Systemd) zu verwalten. Dazu dient die Datei `/etc/systemd/system/smartapplianceenabler-docker.service`, die nachfolgend heruntergeladen und konfiguriert wird:

```bash
$ sudo wget https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/run/lib/systemd/system/smartapplianceenabler-docker.service -P /etc/systemd/system
$ sudo chown root.root /etc/systemd/system/smartapplianceenabler-docker.service
$ sudo chmod 755 /etc/systemd/system/smartapplianceenabler-docker.service
```
 
Damit der *Smart Appliance Enabler* beim Systemstart ebenfalls gestartet wird (via Systemd), muss folgender Befehl ausgeführt werden:

```bash
$ sudo systemctl enable smartapplianceenabler-docker.service
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler.service → /etc/systemd/system/smartapplianceenabler.service.
```
Nach diesen Änderungen muss der Systemd dazu gebracht werden, die Service-Konfigurationen neu zu lesen:

```bash
$ sudo systemctl daemon-reload
```

#### Starten des Containers
```bash
$ sudo systemctl start smartapplianceenabler-docker
```

#### Stoppen des Containers
```bash
$ sudo systemctl stop smartapplianceenabler-docker
```

#### Status des Containers
```bash
$ sudo systemctl status smartapplianceenabler-docker
● smartapplianceenabler-docker.service - Smart Appliance Enabler Container
   Loaded: loaded (/etc/systemd/system/smartapplianceenabler-docker.service; enabled; vendor preset: enabled)
   Active: active (running) since Wed 2019-12-25 17:18:25 CET; 2min 38s ago
  Process: 9566 ExecStartPre=/bin/sleep 1 (code=exited, status=0/SUCCESS)
 Main PID: 9567 (docker)
    Tasks: 11 (limit: 2200)
   Memory: 23.0M
   CGroup: /system.slice/smartapplianceenabler-docker.service
           └─9567 /usr/bin/docker run -v sae:/opt/sae/data --network macvlan0 --ip 192.168.0.200 --publish 8080:8080 --privileged --name=sae avanux/smartapplianceenabler

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

# Hilfreiche Befehle
## Shell im laufenden Smart Appliance Enabler-Container ausführen
Falls man einen Befehl im laufenden Container des *Smart Appliance Enabler* ausführen möchte, kann man mit nachfolgendem Befehl eine entsprechend Shell erzeugen:

```bash
$ docker exec -it sae bash
```

## Konsole-Log anzeigen
Folgender Befehl zeigt die Ausgaben des *Smart Appliance Enabler* auf der Konsole an:

```bash
$ docker logs sae
```

**Achtung:** Wenn der Container schon lange läuft, kann die Log-Ausgabe sehr lang sein! Daher empfiehlt sich an dieser Stelle die Verwendung der Optionen `--tail` oder `--since`, siehe [Dokumentation](https://docs.docker.com/engine/reference/commandline/logs/).


## Smart Appliance Enabler-Logdatei anzeigen
Zusätzlich zum Konsole-Log erzeugt der *Smart Appliance Enabler* für jeden Tag eine Log-Datei im `/tmp`-Verzeichnis.
Mit dem nachfolgenden Befehl kann dieses angezeigt werden, wobei das Datum entsprechend angepasst werden muss:

```bash
$ docker container exec sae tail -f /tmp/rolling-2019-12-25.log
```

### Wiederherstellen der Konfigurationsdateien im Docker-Volume `sae`
Um evtuell vorhandene Konfigurationsdateien des *Smart Appliance Enabler* auf dieses Volume zu kopieren, eignet sich dieser Befehl (die erste Zeile erzeugt einen Dummy-Container mit dem Namen _sae_, falls noch keiner läuft): 

```bash
$ docker run -v sae:/opt/sae/data --name sae busybox true
$ docker cp Appliances.xml sae:/opt/sae/data/
$ docker cp Device2EM.xml sae:/opt/sae/data/
```
