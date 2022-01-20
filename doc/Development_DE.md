# Entwicklung
## Art der Anwendung
Beim *Smart Appliance Enabler* handelt es sich um eine [Spring Boot](http://projects.spring.io/spring-boot) -Anwendung mit einer [Angular](https://angular.io) -Web-Anwendung. 

## Continuous Integration (CI)
Für den *Smart Appliance Enabler* existiert eine [Continuous Integration](https://de.wikipedia.org/wiki/Kontinuierliche_Integration)-Umgebung, in der die Anwendung vollständig automatisiert gebaut und getestet wird:

... Bild aktualiseren

Als Automatisierungswerkzeug wird [Jenkins](https://www.jenkins.io/) eingesetzt, der aktuell aus Kostengründen auf meinem 24x7-Server läuft und nicht aus dem Internet erreichbar ist. Er läuft als Docker-Container und wird wie folgt gestartet:
```console
sudo docker run --name jenkins --rm --publish 8080:8080 --volume jenkins-docker-certs:/certs/client --volume jenkins-data:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v $(which docker):$(which docker) -e PATH=/var/jenkins_home/.local/bin:/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin -e TZ=Europe/Berlin jenkins/jenkins:alpine
```
Eine Shell im Jenkins-Docker-Container öffnet:
```console
sudo docker exec -it jenkins bash
```
Von Jenkins gibt es mehrere [offizielle Docker-Images](https://github.com/jenkinsci/docker), wobei `lts-jdk11` empfohlen wird. Mit diesem Image hatte ich das Problem, dass trotz erfolgreichem Ausführen der Browserstack-Tests der Build fehlgeschlagen ist, weil in Jenkins folgender Fehler auftrat:
```console
Error: spawn ps ENOENT
    at Process.ChildProcess._handle.onexit (node:internal/child_process:282:19)
    at onErrorNT (node:internal/child_process:477:16)
    at processTicksAndRejections (node:internal/process/task_queues:83:21)
    at runNextTicks (node:internal/process/task_queues:65:3)
    at processImmediate (node:internal/timers:437:9)
    at process.topLevelDomainCallback (node:domain:152:15)
    at process.callbackTrampoline (node:internal/async_hooks:128:24)
```
Aufgrund eines [Hinweises](https://github.com/bahmutov/start-server-and-test/issues/132#issuecomment-448581335) habe ich versucht `procps` zu installieren. Leider wird in Containern auf Basis des `lts-jdk11`-Images kein Paket diesen Namens in den Paketquellen gefunden. Anders dagegen das `alpine`-Image, bei dem ich das Paket als `root` problemlos installieren konnte:
```console
$ sudo docker exec -u 0 -it jenkins bash
bash-5.1# apk add procps
bash-5.1# ps
    PID TTY          TIME CMD
     95 pts/0    00:00:00 bash
    141 pts/0    00:00:00 ps
```
Nach der Installation von `ps` trat der oben genannte Fehler nicht mehr auf.

Damit der Jenkins-Docker-Container selbst mit dem Docker-Daemon kommunizieren kann, wird der Docker-Socket und das Docker-Executable jeweils als Volume in den Jenkins-Docker-Container gereicht. Damit der Zugriff auf den Docker-Socket aus dem Jenkins-Docker-Container erlaubt wird, habe ich die Zugriffsrechte darauf erweitert:
```console
sudo chmod o+rw /var/run/docker.sock
```
Jenkins nutzt als Home-Verzeichnis das Volume `jenkins-data`, das im Container unter `/var/jenkins_home` gemounted wird. Neben den Jenkins-Daten finden sich dort auch Maven- und NPM-Repository. Damit sich aus Jenkins heraus Tools aufrufen lassen, wird dem `PATH` das Verzeichnis `/var/jenkins_home/.local/bin` vorangestellt. In diesem können Links für diese Tools platziert werden:
```console
lrwxrwxrwx 1 jenkins jenkins   62 Jan 18 14:24 ng -> /var/jenkins_home/node/lib/node_modules/@angular/cli/bin/ng.js
lrwxrwxrwx 1 jenkins jenkins   31 Jan 17 18:20 node -> /var/jenkins_home/node/bin/node
lrwxrwxrwx 1 jenkins jenkins   30 Jan 17 18:20 npm -> /var/jenkins_home/node/bin/npm
lrwxrwxrwx 1 jenkins jenkins   30 Jan 17 18:21 npx -> /var/jenkins_home/node/bin/npx
```
Node.js muss manuell im Jenkins-Docker-Container in das Volume `jenkins-data` instaliert werden:
```console
cd ~
curl https://nodejs.org/dist/v14.17.0/node-v14.17.0-linux-x64.tar.gz --output node-v14.17.0-linux-x64.tar.gz
tar xvfz node-v14.17.0-linux-x64.tar.gz
ln -s node-v14.17.0-linux-x64 node
```

### travis-ci: Veraltet, da Credits aufgebraucht sind; wird gelöscht 
![Continuous Integration](../pics/CI.png)

Sobald Änderungen am [*Smart Appliance Enabler*-Github-Repository](https://github.com/camueller/SmartApplianceEnabler) erkannt werden, wird dies an [Travis-CI](https://travis-ci.org/camueller/SmartApplianceEnabler) signalisiert. Daraufhin wird dort eine virtuelle Umgebung gestartet, in welcher der *Smart Appliance Enabler* gebaut wird. Dabei werden auch die Unit-Tests ausgeführt, wobei Daten über die Testabdeckung gewonnen werden. Diese **Coverage-Daten** werden zu [codecov](https://codecov.io/gh/camueller/SmartApplianceEnabler) gepusht, wo sie visualisiert werden. Als Build-Artefakt entsteht die Datei **SmartApplianceEnabler.war**, die sich in einer **Java-Runtime** starten läßt. Um die Installation (Java, Konfigurationsdateien, ...) zu vereinfachen, wird ein **Docker-Image** speziell für CI erstellt, das den *Smart Appliance Enabler* mit allen benötigten Dateien inlusive Java-Runtime enthält. Wegen der Platformabhängigkeit der Java-Runtime ist das Docker-Image nur auf Prozessoren mit **amd64**-Architektur lauffähig (also nicht auf Raspberry Pi!). Das Docker-Image wird zu [Docker-Hub](https://hub.docker.com/) gepusht.

In der [AWS](https://aws.amazon.com/de/) -Cloud läuft eine [EC2](https://aws.amazon.com/de/ec2/) -Instanz mit Amazon Linux darauf. Als einzige Software wurde Docker installiert. Im Rahmen des Build-Vorgangs auf *Travis-CI* wird ein Shell-Script auf der **EC2-Instanz** ausgeführt, welches das *Smart Appliance Enabler*-Docker-Image für CI von *Docker-Hub* pullt und einen Container damit startet. Dadurch gibt es einen **laufenden Smart Appliance Enabler**, der vom Internet aus zugreifbar ist und für Tests zur Verfügung steht. 

Um die Funktionalität insgesamt (d.h. Web-Anwendung und Spring Boot-Anwendung) zu testen, wird über Web-Browser eine neue Konfiguration erstellt. Diese **Browser-basierten Tests** werden mittels [BrowserStack](https://www.browserstack.com/) mit verschieden Web-Browsern auf unterschiedlichen Betriebssystemen ausgeführt, wobei die URL des *Smart Appliance Enabler* in EC2-Instanz verwendet wird.

Für den Betrieb des *Smart Appliance Enabler* als Docker-Container auf einem *Raspberry Pi* muss für jedes Release (nicht für jeden Push zu Github!) ein **Docker-Image** mit einer Java-Runtime für **arm32**-Architektur erstellt werden. Was liegt näher, als diese Aufgabe von einem meiner Raspberry Pi's automatisch erledigen zu lassen. Auch dieses Image wird zu [Docker-Hub](https://hub.docker.com/) gepusht. 

#### AWS Setup

- EC2 Instance anlegen:
    - Amazon Linux 2 AMI (HVM), SSD Volume Type
    - Type: t2.micro
- ggf. Keypair erzeugen
- Security Group anlegen mit Inbound Access für SSH + HTTP (Source: Anywhere)
- Security Group zuweisen: Actions -> Networking -> Change Security Groups

In der gestarteten Instanz folgende Befehle ausführen:
```console
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo service docker status
```

Falls das _EC2 Dashboard_ laufende Instanzen nicht anzeigt, stimmt möglicherweise die Region nicht mit der Region überein, in der die Instanzen angelegt wurden (z.B. us-east-2).

### Setup des Raspberry Pi zum Bauen der arm32-Docker-Images

1. Docker installieren
Siehe [Docker-Installation](Docker_DE.md)

2. Git-Repositoy clonen, damit die Scripts/Dockerfiles im docker-Verzeichnis genutzt werden können:
```console
cd /opt/sae
git clone https://github.com/camueller/SmartApplianceEnabler.git
ln -s SmartApplianceEnabler/docker
```

3. Cron-Job verlinken, damit periodisch auf neue Versionen auf Github geprüft wird. Bei Vorhandensein einer neuen Version wird das Bauen des zugehörigen Images gestartet und das Image zu Docker-Hub hochgeladen:
```console
sae@raspi3:~ $ cd /etc/cron.hourly/
sae@raspi3:/etc/cron.hourly $ sudo ln -s /opt/sae/docker/cronjob 
```

## Lokales Entwickeln
### Source-Download
Für alle nachfolgenden Schritte müssen die Sourcen lokal vorhanden sein.
Am einfachsten verwendet man einen Git-Client ([Git installieren](https://git-scm.com/book/de/v1/Los-geht%E2%80%99s-Git-installieren)) zum Klonen des Repositories:
```console
git clone https://github.com/camueller/SmartApplianceEnabler.git
```

Notfalls kann man die Sourcen auch als [ZIP-Datei](https://github.com/camueller/SmartApplianceEnabler/archive/master.zip) herunterladen. Letzteres muß natürlich erst noch ausgepackt werden.

### Bauen

Zum Bauen ist das Build-Tool [Maven](https://maven.apache.org) erforderlich, das gegebenenfalls noch [installiert](https://maven.apache.org/install.html) werden muss.

Um den *Smart Appliance Enabler* ohne Web-Oberfläche zu bauen, ruft man Maven im Verzeichnis *SmartApplianceEnabler* wie folgt auf:
```console
mvn clean package
```

Soll der *Smart Appliance Enabler* mit Web-Oberfläche gebaut werden, muss das Maven-Profil `web` aktiviert werden:
```console
mvn clean package -Pweb
```

Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.war` im Unterverzeichnis `target`.

### Starten


#### UPnP Deaktivierung
In der Regel ist es nicht erwünscht, dass der Sunny Home Manager die für die Entwicklung verwendete SAE-Instanz (in der IDE oder auf einem Entwicklungs-Raspi) per UPnP "entdeckt" und die Geräte übernimmt.
Deshalb kann das UPnP des SAE mit einem Property deaktiviert werden:
```console
-Dsae.discovery.disable=true
```
#### Zugriff auf den Entwicklungs-Web-Server
Normalerweise kann aus Sicherheistgründen nur lokal auf den Entwicklungs-Web-Server zugegriffen werden. Manchmal ist es sinnvoll, diese Einschränk aufzuheben um z.B. mit dem Handy auf den Entwicklungs-Web-Server zuzugreifen. Dazu muss der Entwicklungs-Web-Server wie folgt gestartet werden:
```console
$ ng serve --host 0.0.0.0 --disable-host-check
```

### Tests
#### Simulation der Interaktion durch den Sunny Home Manager
Zum Einschalten eines Gerätes eignet sich folgender Befehl, wobei die der Parameter `RecommendedPowerConsumption` nur für Wallboxen relevant ist:
```console
curl -X POST -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000099-00</DeviceId><On>true</On><RecommendedPowerConsumption>6000</RecommendedPowerConsumption></DeviceControl></EM2Device>' --header 'Content-Type: application/xml' http://raspi:8080/semp
```
Zum Ausschalten eignet sich der folgende Befehl:
```console
curl -X POST -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000099-00</DeviceId><On>false</On></DeviceControl></EM2Device>' --header 'Content-Type: application/xml' http://raspi:8080/semp
```

#### Testcafe
##### Lokal
Unter Verwendung des lokalen Browsers werden die Tests wie folgt gestartet:
```console
$ ./node_modules/.bin/testcafe chrome "src/*.spec.ts"
 Running tests in:
 - Chrome 92.0.4515.159 / Linux 0.0

 Settings
 ✓ Modbus

 Wallbox go-eCharger
 ✓ Create appliance with interruptions allowed without timing specification
 ✓ Create HTTP meter
 ✓ Create HTTP control
 ✓ Add electric vehicle
 ✓ Create Schedule with SOC request from friday to sunday

 Wallbox mit PhoenixContact-Ladecontroller
 ✓ Create appliance with interruptions allowed without timing specification
 ✓ Create Modbus meter
 ✓ Create Modbus control
 ✓ Create Schedule with nightly energy request

 Fridge
 ✓ Create appliance
 ✓ Create HTTP meter
 ✓ Create always-on-switch

 Heat pump
 ✓ Create appliance with interruptions allowed and min/max on/off timings
 ✓ Create S0 meter
 ✓ Create GPIO switch

 Pump
 ✓ Create appliance with interruptions allowed without timing specification
 ✓ Create Modbus meter with specific poll interval and all notifications enabled
 ✓ Create Modbus control with selected notifications enabled

 Washing Machine
 ✓ Create appliance
 ✓ Create HTTP meter with specifc poll interval
 ✓ Create HTTP switch with starting current detection
 ✓ Create Schedules for weekdays and weekend


 23 passed (3m 02s)
```

Um mehr Informationen (z.B. CSS-Selectoren) zu sehen, muss `DEBUG=true` gesetzt werden:
```console
$ DEBUG=true testcafe chrome "src/washingmachine.spec.ts"
 Running tests in:
 - Chrome 81.0.4044.92 / Linux 0.0

 Washing Machine
Selector:    input[formcontrolname="id"] 
Selector:    input[formcontrolname="vendor"] 
Selector:    input[formcontrolname="name"] 
Selector:    mat-select[formcontrolname="type"] 
Open select ...
Option selector:  mat-option[ng-reflect-value="WashingMachine"]
Option selector exists= true
clicked
...
```


##### Browserstack
Zur Ausführung der Tests in [BrowserStack](https://www.browserstack.com/) werden die Tests wie folgt gestartet:
```console
$ node_modules/.bin/testcafe "browserstack:chrome@92.0:Windows 10" "src/aa_settings.spec.ts"
 Running tests in:
 - Chrome 92.0.4515.107 / Windows 10 (https://automate.browserstack.com/builds/a3d339b91d11e8b07134e05b1cb29050a74dfb64/sessions/f8f5d223d8317b22e989cf9f98f501e9bea56901)

 Settings
 ✓ Modbus


 1 passed (17s)
```
... auch mit mehreren Browsern:
```console
axel@p51:/data/IdeaProjects/SmartApplianceEnabler/src/test/angular$ testcafe "browserstack:chrome@69.0:Windows 10,browserstack:firefox@71.0:Windows 10" "src/heatpump.spec.ts"
 Running tests in:
 - Chrome 69.0.3497.92 / Windows 10 (https://automate.browserstack.com/builds/9be7af7e23e9e7a63ea57062980ddffdcbc03f8e/sessions/e3c72a4aa3df464416f6b82063e6fec62e108055)
 - Firefox 71.0 / Windows 10 (https://automate.browserstack.com/builds/9be7af7e23e9e7a63ea57062980ddffdcbc03f8e/sessions/07bfbba39b44bfe5ed7544a2d66c326553ae4d11)

 Heat pump
 ✓ Create appliance
 ✓ Create meter
 ✓ Create control


 3 passed (48s)
```

Weitere Infos zur Verwendung von Browserstack mit Testcafe: https://www.browserstack.com/automate/testcafe

Zum Anzeigen der unterstützten Kombinationen eignet sich der folgende Befehl:
```console
$ testcafe -b browserstack
"browserstack:firefox@71.0:OS X Catalina"
"browserstack:firefox@70.0:OS X Catalina"
"browserstack:firefox@69.0:OS X Catalina"
"browserstack:firefox@68.0:OS X Catalina"
...
```
