# Entwicklung
## Art der Anwendung
Beim *Smart Appliance Enabler* handelt es sich um eine [Spring Boot](http://projects.spring.io/spring-boot)-Anwendung mit einer [Angular](https://angular.io)-Web-Anwendung. 

## Continuous Integration (CI)
Für den *Smart Appliance Enabler* existiert eine [Continuous Integration](https://de.wikipedia.org/wiki/Kontinuierliche_Integration) Umgebung, in der die Anwendung vollständig automatisiert gebaut und getestet wird:

![Continuous Integration](../pics/CI.png)

Als Automatisierungswerkzeug wird [Jenkins](https://www.jenkins.io/) eingesetzt, der aktuell aus Kostengründen auf **meinem 24x7-Server** läuft und nicht aus dem Internet erreichbar ist. Sobald dieser Änderungen am [*Smart Appliance Enabler*-Github-Repository](https://github.com/camueller/SmartApplianceEnabler) erkennt, started er die [Build-Pipeline](https://github.com/camueller/SmartApplianceEnabler/blob/master/Jenkinsfile). In der Pipeline wird zunächst das Java-Backend kompiliert und die zugehörigen Unit-Tests ausgeführt. Danach wird die Web-Anwendung gebaut und durch die zugehörigen Unit-Tests ebenfalls ausgeführt. Beides wird in einer `war`-Datei paketiert, welche von einer **Java-Runtime** als Web-Anwendung ausgeführt werden kann.

Unter Verwendung der `war`-Datei wird ein amd64-Docker-Image speziell für die nachfolgenden Browser-Tests erstellt und ein Container davon gestartet. Anschliessend werden die **Browser-basierten Tests** gestartet, d.h. ausgehend von einem nicht konfigurierten *Smart Appliance Enabler* erfolgt browser-basiert die Konfiguration, das Anlegen von Geräten, Zählern, Schaltern und Schaltplänen. Diese Tests werden mittels [BrowserStack](https://www.browserstack.com/) nacheinander in verschiedenen Browsern (Chrome, Firefox, Safari) ausgeführt, wobei der Docker-Container mit *Smart Appliance Enabler* sowie das Docker-Volume für jeden Browser neu erzeugt werden.

Wenn die Pipeline mit dem Flag "push to dockerhub" ausgeführt wird, wird ein amd64-Docker-Image für das Release gebaut und zu [Docker-Hub](https://hub.docker.com/) gepusht. Ausserdem wird auf einem Raspberry, auf dem ein Jenkins-Slave läuft, ein arm32-Docker-Image gebaut und ebenfalls zu Docker-Hub gepusht. 

### Jenkins
Von Jenkins gibt es [offizielle Docker-Images](https://github.com/jenkinsci/docker). Dabei wird Jenkins wie folgt gestartet:
```bash
sudo docker run \
    --name jenkins \
    -d \
    --rm \
    --publish 8080:8080 \
    --volume jenkins-docker-certs:/certs/client \
    --volume jenkins-data:/var/jenkins_home \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume $(which docker):$(which docker) \
    -e PATH=/var/jenkins_home/.local/bin:/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
    -e TZ=Europe/Berlin \
    jenkins/jenkins
```

Damit der Jenkins-Docker-Container selbst mit dem Docker-Daemon kommunizieren kann, wird der Docker-Socket und das Docker-Executable jeweils als Volume in den Jenkins-Docker-Container gereicht. Damit der Zugriff auf den Docker-Socket aus dem Jenkins-Docker-Container erlaubt wird, habe ich die Zugriffsrechte darauf erweitert:
```bash
sudo chmod o+rw /var/run/docker.sock
```

Jenkins nutzt als Home-Verzeichnis das Volume `jenkins-data`, das im Container unter `/var/jenkins_home` gemounted wird. Neben den Jenkins-Daten finden sich dort auch Maven- und NPM-Repository. Damit sich aus Jenkins heraus Tools aufrufen lassen, wird dem `PATH` das Verzeichnis `/var/jenkins_home/.local/bin` vorangestellt. In diesem können Links für diese Tools platziert werden:
```bash
lrwxrwxrwx 1 jenkins jenkins   31 Jan 17 18:20 node -> /var/jenkins_home/node/bin/node
lrwxrwxrwx 1 jenkins jenkins   30 Jan 17 18:20 npm -> /var/jenkins_home/node/bin/npm
lrwxrwxrwx 1 jenkins jenkins   30 Jan 17 18:21 npx -> /var/jenkins_home/node/bin/npx
```

Die Dateien aus dem `lib`-Verzeichnis, welche nicht mehr in den offiziellen Maven-Repos verfügbar sind, müssen manuell in das lokale Maven-Repo installiert werden.

Node.js muss manuell im Jenkins-Docker-Container in das Volume `jenkins-data` installiert werden:
```bash
cd ~
curl https://nodejs.org/dist/v14.17.0/node-v14.17.0-linux-x64.tar.gz \
    --output node-v14.17.0-linux-x64.tar.gz
tar xvfz node-v14.17.0-linux-x64.tar.gz
ln -s node-v14.17.0-linux-x64 node
```

Eine Shell im Jenkins-Docker-Container öffnet:
```bash
sudo docker exec -it jenkins bash
```

### Mosquitto
Damit der *Smart Appliance Enabler* getestet werden kann, ist ein MQTT-Broker erforderlich. Hier wird dazu Mosquitto eingesetzt, der ebenfalls als Docker-Container läuft und wie folgt gestartet wird:
```bash
sudo docker run \
    -it \
    -d \
    --rm \
    -p 1883:1883 \
    --name mosquitto \
    eclipse-mosquitto \
    mosquitto -c /mosquitto-no-auth.conf
```

### Testen der automatischen Installation

Die automatische Installation soll es Nutzern ohne Linux Know-How ermöglichen, den *Smart Appliance Enabler* ohne Nutzung einer SSH-Shell und Eingabe von Befehlen zu installieren. Während der automatischen Installation werden die im Image enthaltenen Pakete aktualisiert und auch Pakete (z.B. Java) nachinstalliert. Dabei kann es zu Problemen kommen, z.B. weil die benötigten Pakete nicht mehr im Repository enthalten sind oder das Repository nicht mehr existiert. Um diese Problem zu erkennen und zu beheben (z.B. durch Bereitstellung eines neueren Images) muss die automatische Installation automatisiert getestet werden.  

Das automatisierte Testen der automatischen Installation erfolgt durch Eumlation eines Raspberry Pi mittels QEMU:
```bash
sudo apt install qemu-system-arm
```

Die benötigten Kernel finden sich auf: https://github.com/dhruvvyas90/qemu-rpi-kernel
```bash
$ git clone https://github.com/dhruvvyas90/qemu-rpi-kernel
Cloning into 'qemu-rpi-kernel'...
remote: Enumerating objects: 470, done.
remote: Counting objects: 100% (93/93), done.
remote: Compressing objects: 100% (77/77), done.
remote: Total 470 (delta 44), reused 40 (delta 16), pack-reused 377
Receiving objects: 100% (470/470), 88.77 MiB | 6.82 MiB/s, done.
Resolving deltas: 100% (241/241), done.
```

Ich habe ein [Script erstellt](../src/test/install2-test.sh), welches
- ein Raspbian-Image vergössert
- die Installationsdateien hineinkopiert, sodass die Installation nach dem Booten startet
- den virtuellen Raspberry Pi startet

Die Installation funktioniert zwar prinzipiell, aber
- Java wird zwar installiert, aber es fehlen die Links unter `/etc/alternatives`
- wenn man das Java-Binary direkt aufruft, kommt ein Hinweis, dass die VM nur unter `v7l` läuft, nicht jedoch unter `v6l`. In `qemu-system-arm` scheint zwar Support für andere ARM-Prozesssoren (z.B. cortex-v7) enthalten zu sein, aber ich konnte mit keinem (außer `versatilepb`) ein Raspbian erfolgreich booten. `versatilepb` untersützt nur `v6l` und ist auf 256 MB begrenzt. 

Angesichts dieser Probleme stoppe ich hier erstmal den Versuch, die automatische Installation automatisiert zu testen.

## Erstellen eines neuen Releases

1. `CHANGELOG.md` aktualisieren
1. Version in `pom.xml` erhöhen
1. Version in `README.md` und `README_EN.md` erhöhen 
1. Tag mit neuer Version erstellen, z.B. `2.0.2`
1. Änderungen inkl. Tags zu Github pushen
1. Jenkins-Build manuell starten und Docker-Push aktivieren (dauert inkl. Browserstack-Tests aktuell ca. 50 Minuten)
   1. compiliert Source-Files und baut Java-Backend und Angular-Webanwendung
   1. führt Unit-Tests aus
   1. Browserstack-Tests
      1. baut Docker-Image des *Smart Appliance Enabler* für Browserstack-Tests
      1. startet gebauten Docker-Container, wobei HTTP/Modbus-Aufrufe der während der Tests erstellten Schalter/Zähler unterdrückt werden
      1. führt Browserstack-Tests in verschiedenen Browser aus
   1. erstellt Docker-Image für amd64 und pusht es zu Dockerhub
   1. erstellt Docker-Image für arm32 und pusht es zu Dockerhub
1. Kopieren des Build-Artefakts aus dem Jenkins-Docker-Container auf den Host:
   ```bash 
   docker cp jenkins:/var/jenkins_home/workspace/SmartApplianceEnabler2/target/SmartApplianceEnabler-2.0.2.war /tmp
   ```
1. Kopieren des Build-Artefakts vom Server auf den lokalen Rechner: 
   ```bash
   scp server:/tmp/SmartApplianceEnabler-2.0.2.war /tmp
   ```
1. Erstellen eines neuen Releases auf Github unter Verwendung des bereits angelegten Tags
1. Bekanntmachen des neuen Releases auf Gihub-Discussions und Sperren des Themas zur Vermeidung von Diskussionen innerhalb der Bekanntmachung

## Lokales Entwickeln
### Source-Download
Für alle nachfolgenden Schritte müssen die Sourcen lokal vorhanden sein.
Am einfachsten verwendet man einen Git-Client ([Git installieren](https://git-scm.com/book/de/v1/Los-geht%E2%80%99s-Git-installieren)) zum Klonen des Repositories:
```bash
git clone https://github.com/camueller/SmartApplianceEnabler.git
```

Notfalls kann man die Sourcen auch als [ZIP-Datei](https://github.com/camueller/SmartApplianceEnabler/archive/master.zip) herunterladen. Letzteres muß natürlich erst noch ausgepackt werden.

### Bauen

Zum Bauen ist das Build-Tool [Maven](https://maven.apache.org) erforderlich, das gegebenenfalls noch [installiert](https://maven.apache.org/install.html) werden muss.

Maven lädt alle benötigen Bibliotheken aus dem zentralen Maven-Repository. Einige Bibliotheken sind jedoch dort nicht mehr verfügbar und deshalb im Git-Repository des *Smart Appliance Enabler* enthalten. Diese müssen zunächst in das lokale Maven-Repository installiert werden:
```bash
mvn install:install-file -Dfile=lib/parent-1.0.0.pom -DpomFile=lib/parent-1.0.0.pom -Dpackaging=pom
mvn install:install-file -Dfile=lib/seamless-http-1.0.0.jar -DpomFile=lib/seamless-http-1.0.0.pom
mvn install:install-file -Dfile=lib/seamless-xml-1.0.0.jar -DpomFile=lib/seamless-xml-1.0.0.pom
mvn install:install-file -Dfile=lib/seamless-util-1.0.0.jar -DpomFile=lib/seamless-util-1.0.0.pom
mvn install:install-file -Dfile=lib/cling-2.0.0.pom -DpomFile=lib/cling-2.0.0.pom -Dpackaging=pom
mvn install:install-file -Dfile=lib/cling-core-2.0.0.jar -DpomFile=lib/cling-core-2.0.0.pom
```

Um den *Smart Appliance Enabler* ohne Web-Oberfläche zu bauen, ruft man Maven im Verzeichnis *SmartApplianceEnabler* wie folgt auf:
```bash
mvn clean package
```

Soll der *Smart Appliance Enabler* mit Web-Oberfläche gebaut werden, muss das Maven-Profil `web` aktiviert werden:
```bash
mvn clean package -Pweb
```

Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.war` im Unterverzeichnis `target`.

### Starten
#### UPnP Deaktivierung
In der Regel ist es nicht erwünscht, dass der Sunny Home Manager die für die Entwicklung verwendete SAE-Instanz (in der IDE oder auf einem Entwicklungs-Raspi) per UPnP "entdeckt" und die Geräte übernimmt. Deshalb kann das UPnP des SAE mit einem Property deaktiviert werden:
```bash
-Dsae.discovery.disable=true
```
#### Zugriff auf den Entwicklungs-Web-Server
Normalerweise kann aus Sicherheitsgründen nur lokal auf den Entwicklungs-Web-Server zugegriffen werden. Manchmal ist es sinnvoll, diese Einschränkung aufzuheben, um z.B. mit dem Handy auf den Entwicklungs-Web-Server zuzugreifen. Dazu muss der Entwicklungs-Web-Server wie folgt gestartet werden:
```bash
ng serve --host 0.0.0.0 --disable-host-check
```

### Tests
#### Simulation der Interaktion durch den Sunny Home Manager
Zum Einschalten eines Gerätes eignet sich folgender Befehl, wobei der Parameter `RecommendedPowerConsumption` nur für Geräte mit variabler Leistungsaufnahme (z.B. Wallboxen) relevant ist:
```bash
curl \
    -X POST \
    -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000099-00</DeviceId><On>true</On><RecommendedPowerConsumption>6000</RecommendedPowerConsumption></DeviceControl></EM2Device>' \
    --header 'Content-Type: application/xml' \
    http://raspi:8080/semp
```

Zum Ausschalten eignet sich der folgende Befehl:
```bash
curl \
    -X POST \
    -d '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl><DeviceId>F-00000001-000000000099-00</DeviceId><On>false</On></DeviceControl></EM2Device>' \
    --header 'Content-Type: application/xml' \
    http://raspi:8080/semp
```

#### Testcafe
##### Lokal
Unter Verwendung des lokalen Browsers werden die Tests mit folgenden Parametern gestartet: 

- `SKIP_MQTT_CONFIGURATION=true`: Überspringen der Anpssung der MQTT-Konfiguration
- `E2E_TEST_URL=http://localhost:4200/?lang=de`: schaltet die Anwendung in den Test-Modus für deutsche Sprache:
  - Anzeige der deutschen Texte
  - Deaktivieren der Tooltips, damit die selektierten Elemente nicht durch Tooltips verdeckt werden

```bash
$ SKIP_MQTT_CONFIGURATION=true E2E_TEST_URL=http://localhost:4200/?lang=de ./node_modules/.bin/testcafe chrome "src/*.spec.ts"
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

Um mehr Informationen (z.B. CSS-Selektoren) zu sehen, muss `DEBUG=true` gesetzt werden:
```bash
$ SKIP_MQTT_CONFIGURATION=true E2E_TEST_URL=http://localhost:4200/?lang=de DEBUG=true testcafe chrome "src/washingmachine.spec.ts"
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
```bash
$ node_modules/.bin/testcafe "browserstack:chrome@92.0:Windows 10" "src/aa_settings.spec.ts"
 Running tests in:
 - Chrome 92.0.4515.107 / Windows 10 (https://automate.browserstack.com/builds/a3d339b91d11e8b07134e05b1cb29050a74dfb64/sessions/f8f5d223d8317b22e989cf9f98f501e9bea56901)

 Settings
 ✓ Modbus


 1 passed (17s)
```
... auch mit mehreren Browsern:
```bash
$ testcafe "browserstack:chrome@69.0:Windows 10,browserstack:firefox@71.0:Windows 10" "src/heatpump.spec.ts"
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
```bash
$ testcafe -b browserstack
"browserstack:firefox@71.0:OS X Catalina"
"browserstack:firefox@70.0:OS X Catalina"
"browserstack:firefox@69.0:OS X Catalina"
"browserstack:firefox@68.0:OS X Catalina"
...
```

## Sonstige Tools
### MQTT Explorer
Zum testweisen Senden und Empfangen von MQTT-Nachrichten eignet sich der [MQTT Explorer](http://mqtt-explorer.com/), der auch als Paket für diverse Linux-Distributionen verfübar ist.
```bash
$ mqtt-explorer
```
