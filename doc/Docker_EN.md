# Docker images
For the *Smart Appliance Enabler* docker images are provided for `arm` (including Raspberry Pi) and `amd64`, each containing the appropriate Java version (therefore platform-specific images).

The repository for these images is [avanux/smartapplianceenabler](https://hub.docker.com/r/avanux/smartapplianceenabler)

# Docker installation
Before the *Smart Appliance Enabler* can be operated as a Docker container, Docker must be installed.

## Raspberry Pi
The Docker installation is very simple, but must be done in a root shell:

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

After giving the user the `docker` role as suggested by the Docker installation, you have to log out and log in before you can check the Docker installation:

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

## Other platformes
General instructions for installation on all officially supported platforms can be found at https://docs.docker.com/get-docker/

# Docker configuration
The image contains the *Smart Appliance Enabler* in the `/opt/sae` directory.

The *Smart Appliance Enabler* configuration files (`Appliances.xml` and `Device2EM.xml`) are stored in the Docker volume `sae`.
At runtime, this volume is mounted under `/opt/sae/data`.

There are two options for configuring as a container:
* Configuration with `docker-compose` based on a YAML file (recommended!)
* Configuration with various `docker` commands

The *Smart Appliance Enabler* implements SMA's SEMP protocol. This protocol is based on UPnP, which in turn requires IP multicast. The configurations described below therefore use a [`macvlan` network](https://docs.docker.com/network/macvlan/), with the help of which the Docker container of the *Smart Appliance Enabler* creates its own MAC and IP address received. If this is not possible or desired, the *Smart Appliance Enabler* Docker container must be started with `--net=host`.

## configuration using `docker-compose` commands
`docker-compose` allows a comfortable configuration of the container via a YAML file.

### Installation of `docker-compose`
`docker-compose` must be installed in addition to `docker`.

#### Raspberry Pi
Before installing `docker-compose` on the Raspberry Pi, the Python package manager must be installed:
```console
pi@raspberrypi:~ $ sudo apt-get -y install python3-pip
```

The actual installation of `docker-compose` is then done with:
```console
pi@raspberrypi:~ $ sudo pip3 -v install docker-compose
```

#### Other platforms
The description of the `docker-compose` installation can be found at https://docs.docker.com/compose/install.

### YAML-Datei
For the *Smart Appliance Enabler* there is a preconfigured YAML file for which a directory must be created in order to download it afterwards:
```console
pi@raspberrypi:~ $ sudo mkdir -p /etc/docker/compose/smartapplianceenabler
pi@raspberrypi:~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/docker/compose/docker-compose.yml -P /etc/docker/compose/smartapplianceenabler
```
Notes on the necessary adjustments can be found as comments in the file itself.
The docker volume `sae` will be created automatically at startup if it doesn't already exist.

### `systemd` for `docker-compose`
Even if the *Smart Appliance Enabler* is operated as a Docker container, it makes sense to manage the container as a [Systemd](https://de.wikipedia.org/wiki/Systemd) service. The file ```/etc/systemd/system/smartapplianceenabler-docker-compose.service``` is used for this, which is downloaded and configured below:
```console
$ sudo wget https://github.com/camueller/SmartApplianceEnabler/blob/master/run/lib/systemd/system/smartapplianceenabler-docker-compose.service -P /etc/systemd/system
$ sudo chown root.root /etc/systemd/system/smartapplianceenabler-docker-compose.service
$ sudo chmod 644 /etc/systemd/system/smartapplianceenabler-docker-compose.service
```

The following command must be executed so that the *Smart Appliance Enabler* is also started when the system starts (via systemd):
```console
pi@raspberrypi ~ $ sudo systemctl enable smartapplianceenabler-docker-compose.service
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler-docker-compose.service → /etc/systemd/system/smartapplianceenabler-docker-compose.service.
```
After making these changes, the systemd needs to be tricked into rereading the service configurations:
```console
pi@raspberrypi ~ $ sudo systemctl daemon-reload
```

#### Starting the container
```console
sudo systemctl start smartapplianceenabler-docker-compose
```

#### Stopping the container
```console
sudo systemctl stop smartapplianceenabler-docker-compose
```

#### <a name="container-status"></a> Status of the container

If the container is running with the *Smart Appliance Enabler*, the status should be `active (running)`:
```console
sae@raspi:~ $ sudo systemctl status smartapplianceenabler-docker-compose.service
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

### Commands using `docker-compose`
For all following commands you have to be in the directory with the *Smart Appliance Enabler* related `docker-compose.yml` file (usually `/etc/docker/compose/smartapplianceenabler`)!

#### Starting the containers
```console
pi@raspberrypi:/etc/docker/compose/smartapplianceenabler $ docker-compose up -d
Creating network "macvlan0" with driver "macvlan"
Creating mosquitto ... done
Creating pigpiod   ... done
Creating sae       ... done
```

#### Stopping the containers
```console
pi@raspberrypi:~ $ docker-compose down
Stopping sae       ... done
Stopping mosquitto ... done
Stopping pigpiod   ... done
Removing sae       ... done
Removing mosquitto ... done
Removing pigpiod   ... done
Removing network macvlan0
```

#### Display console log
```console
$ docker-compose logs
[...]
sae    | 17:06:20.733 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) with context path ''
sae    | 17:06:24.615 [http-nio-8080-exec-1] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring DispatcherServlet 'dispatcherServlet'
sae    | 17:06:24.616 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Initializing Servlet 'dispatcherServlet'
sae    | 17:06:24.708 [http-nio-8080-exec-1] INFO  o.s.web.servlet.DispatcherServlet - Completed initialization in 91 ms[...]
```

## configuration using `docker` commands
### SAE volume
The *Smart Appliance Enabler* needs a writable directory in which to store its files. For this purpose, the volume *sae* is created in Docker.
```console
pi@raspberrypi:~ $ docker volume create sae
sae
```
After that, the new volume should be included in the list of existing volumes:
```console
pi@raspberrypi:~ $ docker volume ls
DRIVER              VOLUME NAME
local               sae
```

### Create `macvlan` network
The following commands (apart from `docker create network ...`) are only active until the next reboot and may have to be placed in an init script.

First, a `macvlan` interface called `macvlan0` is created as a link to the physical interface (here eth0):
```console
sudo ip link add macvlan0 link eth0 type macvlan mode bridge
```
An overlay network of IP addresses must be defined for the IP addresses of the Docker containers, which overlays the address range of the physical interface. This address range must be ignored by the DHCP server, i.e. it must not assign these addresses to anyone!!

The [IP Calculator](http://jodies.de/ipcalc) is suitable for determining suitable networks.
The `macvlan` interface is then configured to an IP address from the address range of the overlay network.
```console
sudo ifconfig macvlan0 192.168.0.223 netmask 255.255.255.0 up
```
Finally, a routing entry for the overlay mesh must be added:
```console
sudo ip route add 192.168.0.192/27 dev macvlan0
```

Now the docker network `macvlan0` can be created. The `subnet` parameter corresponds to the network of the physical interface. The `gateway` parameter is the destination of the default route (usually the internal IP address of the internet router). The overlay network is specified as the `ip-range` parameter, with the IP address of the `macvlan` interface being specified in the `aux-address` parameter so that it is not assigned to a Docker container. The physical interface is specified for the `parent` parameter.

So the command looks like this:
```console
docker network create -d macvlan --subnet=192.168.0.0/24 --gateway=192.168.0.1 --ip-range 192.168.0.192/27 --aux-address 'host=192.168.0.223' -o parent=eth0 -o macvlan_mode=bridge macvlan0
```

### Starting MQTT broker
The *Smart Appliance Enabler* requires an MQTT broker. You can also start this as a Docker container, whereby you have to assign it an IP address from the Docker network `macvlan0`:

```console
docker run --rm --detach --network macvlan0 --ip 192.168.0.201 --name mosquitto eclipse-mosquitto mosquitto -c /mosquitto-no-auth.conf
```

### Starting pigpiod
The *Smart Appliance Enabler* needs `pigpiod` to access the GPIOs of the Raspberry Pi. This can also be started as a Docker container, whereby you have to assign it an IP address from the Docker network `macvlan0`:

```console
docker run --rm --detach --network macvlan0 --ip 192.168.0.202 --name pigpiod --privileged --device /dev/gpiochip0 zinen2/alpine-pigpiod
```

### Start/Stop/Status of Smart Appliance Enabler
#### Starting the container
When starting the *Smart Appliance Enabler* in a new container named _sae_ the docker container needs to be assigned an IP address from the docker network `macvlan0`:
```console
pi@raspberrypi:~ $ docker run -v sae:/opt/sae/data --network macvlan0 --ip 192.168.0.200 --publish 8080:8080 --privileged --name=sae avanux/smartapplianceenabler
```

Properties can also be set via the Docker variable _JAVA_OPTS_:
```console
pi@raspberrypi:~ $ docker ... -e JAVA_OPTS="-Dserver.port=9000" avanux/smartapplianceenabler
```

#### Stopping the container
```console
pi@raspberrypi:~ $ docker stop sae
sae
```

### Automatic start of container by systemd
Even if the *Smart Appliance Enabler* is operated as a Docker container, it makes sense to manage the container as a [Systemd](https://de.wikipedia.org/wiki/Systemd) service. The file `/etc/systemd/system/smartapplianceenabler-docker.service` is used for this, which is downloaded and configured below:
```console
pi@raspberrypi ~ $ sudo wget https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/run/lib/systemd/system/smartapplianceenabler-docker.service -P /etc/systemd/system
pi@raspberrypi ~ $ sudo chown root.root /etc/systemd/system/smartapplianceenabler-docker.service
pi@raspberrypi ~ $ sudo chmod 755 /etc/systemd/system/smartapplianceenabler-docker.service
```

The following command must be executed so that the *Smart Appliance Enabler* is also started when the system starts (via systemd):
```console
pi@raspberrypi ~ $ sudo systemctl enable smartapplianceenabler-docker.service
Created symlink /etc/systemd/system/multi-user.target.wants/smartapplianceenabler.service → /etc/systemd/system/smartapplianceenabler.service.
```
After making these changes, the systemd needs to be tricked into rereading the service configurations:
```console
pi@raspberrypi ~ $ sudo systemctl daemon-reload
```

#### Starting the container
```console
sudo systemctl start smartapplianceenabler-docker
```

#### Stopping the container
```console
sudo systemctl stop smartapplianceenabler-docker
```

#### Status of the container
```console
sae@raspberrypi:~/docker $ sudo systemctl status smartapplianceenabler-docker
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

# Useful commands
## Shell in running Smart Appliance Enabler container
If you want to execute a command in the running container of the *Smart Appliance Enabler*, you can create a corresponding shell with the following command:
```console
pi@raspberrypi:~ $ docker exec -it sae bash
```

## Display console log
The following command displays the output of the *Smart Appliance Enabler* on the console:
```console
pi@raspberrypi:~ $ docker logs sae
```

## Display log file of Smart Appliance Enabler
In addition to the console log, the *Smart Appliance Enabler* creates a log file in the `/tmp` directory for each day.

This can be displayed with the following command, whereby the date must be adjusted accordingly:
```console
pi@raspberrypi:~ $ docker container exec sae tail -f /tmp/rolling-2019-12-25.log
```

### Restore configuration files in `sae` Docker volume
This command is suitable for copying any *Smart Appliance Enabler* configuration files to this volume (the first line creates a dummy container with the name _sae_ if one is not already running):
```console
pi@raspberrypi:~ docker run -v sae:/opt/sae/data --name sae busybox true
pi@raspberrypi:~ docker cp Appliances.xml sae:/opt/sae/data/
pi@raspberrypi:~ docker cp Device2EM.xml sae:/opt/sae/data/
```
