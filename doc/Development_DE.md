# Hinweise für die Enwicklung

## UPnP Deaktivierung
In der Regel ist es nicht erwünscht, dass der Sunny Home Manager (SHM) die für die Entwicklung verwendete SAE-Instanz (in der IDE oder auf einem Entwicklungs-Raspi) per UPnP "entdeckt" und die Geräte übernimmt.
Deshalb kann das UPnP des SAE mit einem Property deaktiviert werden:
```
-Dsae.discovery.disable=true
```

## Docker
### Images bauen

```
root@resin:~# mkdir /var/lib/docker/sae
root@resin:~# mkdir /var/lib/docker/sae-de
```

```
scp -P22222 docker/sae/Dockerfile root@resin.local:/var/lib/docker/sae
scp -P22222 docker/sae-de/Dockerfile root@resin.local:/var/lib/docker/sae-de
```

#### Bauen des SAE-Basis-Images
```
root@resin:~# cd /var/lib/docker/sae
root@resin:/var/lib/docker/sae# docker build -t avanux/smartapplianceenabler:test1 -t avanux/smartapplianceenabler:latest .
```
Dabei werden die einzelnen Steps angezeigt, die beim Erzeugen des Images durchlaufen werden. Das Erzeugen war erfolgreich, wenn die letzte Zeile mit ```Successfully built``` beginnt:
```
Sending build context to Docker daemon 3.584 kB
Step 1/6 : FROM dpsmyth/raspberrypi3-alpine-java
latest: Pulling from dpsmyth/raspberrypi3-alpine-java
96598928ae71: Pull complete
b2da29541025: Pull complete
[...]
 ---> 9c1765bbbec0
Removing intermediate container 03b4a3fb2d52
Step 6/6 : CMD java -Djava.awt.headless=true -Xmx256m -Dsae.home=/app -Dsae.discovery.disable=true     -Dlogging.config=/app/logback-spring.xml -jar /opt/SmartApplianceEnabler.war
 ---> Running in 1f9e753cb4e5
 ---> 5e3bcb4acab0
Removing intermediate container 1f9e753cb4e5
Successfully built 5e3bcb4acab0
```

#### Bauen des SAE-Images für Deutschland
```
root@resin:~# cd /var/lib/docker/sae-de
root@resin:/var/lib/docker/sae-de# docker build -t avanux/smartapplianceenabler:test1 -t avanux/smartapplianceenabler:latest .
```

```
root@resin:/var/lib/docker/sae-de# docker build -t avanux/smartapplianceenabler-de:test2 -t avanux/smartapplianceenabler-de:latest .
Sending build context to Docker daemon 2.048 kB
Step 1/4 : FROM avanux/smartapplianceenabler
 ---> 1a6784c5f4ea
Step 2/4 : MAINTAINER Axel Mueller <axel.mueller@avanux.de>
 ---> Running in 17fa4b4e2b77
 ---> 3bef0e02db5a
Removing intermediate container 17fa4b4e2b77
Step 3/4 : ENV TZ "Europe/Berlin"
 ---> Running in 18bc32214694
 ---> a881b0d6d96b
Removing intermediate container 18bc32214694
Step 4/4 : RUN apk upgrade --update && apk add --update tzdata && cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime && echo "Europe/Berlin" > /etc/timezone && apk del tzdata
 ---> Running in 072d94fc5b38
fetch http://dl-cdn.alpinelinux.org/alpine/v3.6/main/armhf/APKINDEX.tar.gz
fetch http://dl-cdn.alpinelinux.org/alpine/v3.6/community/armhf/APKINDEX.tar.gz
OK: 40 MiB in 64 packages
fetch http://dl-cdn.alpinelinux.org/alpine/v3.6/main/armhf/APKINDEX.tar.gz
fetch http://dl-cdn.alpinelinux.org/alpine/v3.6/community/armhf/APKINDEX.tar.gz
(1/1) Installing tzdata (2017a-r0)
Executing busybox-1.26.2-r9.trigger
OK: 41 MiB in 65 packages
(1/1) Purging tzdata (2017a-r0)
Executing busybox-1.26.2-r9.trigger
OK: 40 MiB in 64 packages
 ---> d833e03c5cb7
Removing intermediate container 072d94fc5b38
Successfully built d833e03c5cb7
```

#### Installierte Images
Die Liste der installieren Images enthält jetzt das Minimal-Linux Alpine mit Oracle Java 8, das SAE-Basis-Image sowie das SAE-Image für Deutschland:
```
root@resin:/var/lib/docker/sae-de# docker image ls
REPOSITORY                         TAG                 IMAGE ID            CREATED             SIZE
avanux/smartapplianceenabler-de    latest              d833e03c5cb7        46 seconds ago      192 MB
avanux/smartapplianceenabler-de    test2               d833e03c5cb7        46 seconds ago      192 MB
avanux/smartapplianceenabler       latest              1a6784c5f4ea        18 minutes ago      191 MB
avanux/smartapplianceenabler       test2               1a6784c5f4ea        18 minutes ago      191 MB
dpsmyth/raspberrypi3-alpine-java   latest              1236a8fda552        6 months ago        156 MB
```

### Images zu Dockerhub pushen
Bevor man Images zu Dockerhub pushen kann, muss man sich zunächst anmelden:
```
docker login
```

Das Pushen *aller* Tags des SAE-Basis-Images erfolgt wie folgt:
```
docker push avanux/smartapplianceenabler
The push refers to a repository [docker.io/avanux/smartapplianceenabler]
1c2ac553eafa: Pushed
1f64bb14297f: Layer already exists
313d7bb23760: Layer already exists
c6b43e26df4e: Layer already exists
cf9607af3c46: Layer already exists
97702abb607e: Layer already exists
0bea0d3f858f: Layer already exists
2ad7652ee022: Layer already exists
ad5daa6145b3: Layer already exists
ac9b3704e8a8: Layer already exists
bdc9a5c40648: Layer already exists
d8cab8b941ce: Layer already exists
e0974bfaea82: Layer already exists
latest: digest: sha256:acdc63cd5a5726112c19dabdf129ab56687ed33f0e0a7fca2fe7d009457081fd size: 3038
1c2ac553eafa: Layer already exists
1f64bb14297f: Layer already exists
313d7bb23760: Layer already exists
c6b43e26df4e: Layer already exists
cf9607af3c46: Layer already exists
97702abb607e: Layer already exists
0bea0d3f858f: Layer already exists
2ad7652ee022: Layer already exists
ad5daa6145b3: Layer already exists
ac9b3704e8a8: Layer already exists
bdc9a5c40648: Layer already exists
d8cab8b941ce: Layer already exists
e0974bfaea82: Layer already exists
test2: digest: sha256:acdc63cd5a5726112c19dabdf129ab56687ed33f0e0a7fca2fe7d009457081fd size: 3038
```

Analog erfolgt das Pushen *aller* Tags des SAE-Images für Deutschland:
```
root@resin:/var/lib/docker/sae-de# docker push avanux/smartapplianceenabler-de
The push refers to a repository [docker.io/avanux/smartapplianceenabler-de]
21e79dd6fede: Pushed
1c2ac553eafa: Mounted from avanux/smartapplianceenabler
1f64bb14297f: Mounted from avanux/smartapplianceenabler
313d7bb23760: Mounted from avanux/smartapplianceenabler
c6b43e26df4e: Mounted from avanux/smartapplianceenabler
cf9607af3c46: Mounted from avanux/smartapplianceenabler
97702abb607e: Mounted from avanux/smartapplianceenabler
0bea0d3f858f: Mounted from avanux/smartapplianceenabler
2ad7652ee022: Mounted from avanux/smartapplianceenabler
ad5daa6145b3: Mounted from avanux/smartapplianceenabler
ac9b3704e8a8: Mounted from avanux/smartapplianceenabler
bdc9a5c40648: Mounted from avanux/smartapplianceenabler
d8cab8b941ce: Mounted from avanux/smartapplianceenabler
e0974bfaea82: Mounted from avanux/smartapplianceenabler
latest: digest: sha256:bf2abfece9fd6afb0168a4309430ee036b5e871e250babe3e03d2c75b8881e35 size: 3250
21e79dd6fede: Layer already exists
1c2ac553eafa: Layer already exists
1f64bb14297f: Layer already exists
313d7bb23760: Layer already exists
c6b43e26df4e: Layer already exists
cf9607af3c46: Layer already exists
97702abb607e: Layer already exists
0bea0d3f858f: Layer already exists
2ad7652ee022: Layer already exists
ad5daa6145b3: Layer already exists
ac9b3704e8a8: Layer already exists
bdc9a5c40648: Layer already exists
d8cab8b941ce: Layer already exists
e0974bfaea82: Layer already exists
test2: digest: sha256:bf2abfece9fd6afb0168a4309430ee036b5e871e250babe3e03d2c75b8881e35 size: 3250
```