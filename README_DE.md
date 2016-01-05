# Smart Appliance Enabler

## Wozu?
Der *Smart Appliance Enabler* dient dazu, beliebige Geräte (Wärmepumpe, Waschmaschine, ...) in eine **(Smart-Home-) Steuerung** zu integrieren. Dazu kann der *Smart Appliance Enabler* von der Steuerung **Schalt-Empfehlungen** entgegen nehmen und die von ihm verwalteten Geräte ein- oder ausschalten. Falls für diese Geräte individuelle, **digitale Stromzähler** verwendet werden, können diese ausgelesen werden und der Stromverbrauch an die (Smart-Home-) Steuerung gemeldet werden, um der Steuerung künftig energieeffiziente Schaltempfehlungen zu ermöglichen.

![SmartHomeEnablerSchema](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchema.png)

Damit der *Smart Appliance Enabler* in die (Smart-Home-) Steuerung integriert werden kann, muss er deren Protokoll(e) unterstützen. Obwohl die Unterstützung diverser Steuerungen konzeptionell berücksichtigt wurde, wird aktuell nur das **SEMP**-Protokoll zur Integration mit dem [Sunny Home Manager](http://www.sma.de/produkte/monitoring-control/sunny-home-manager.html) von [SMA](http://www.sma.de) unterstützt.

## Hardware
### Raspberry Pi
Der *Smart Appliance Enabler* benötigt einen [Raspberry Pi](https://de.wikipedia.org/wiki/Raspberry_Pi) als Hardware. Dieser extrem preiswerte Kleinstcomputer (ca. 40 Euro) ist perfekt zum Steuern und Messen geeignet, da er bereits [digitale Ein-/Ausgabe-Schnittstellen](https://de.wikipedia.org/wiki/Raspberry_Pi#GPIO) enthält, die zum Schalten sowie zum Messen des Stromverbrauchs benötigt werden. Der aktuelle **Raspberry Pi 2 Model B** ist ist deutlich performanter als die Vorgängermodelle, was der vom *Smart Appliance Enabler* benötigten Software zugute kommt.

Für den Raspberry Pi existieren verschiedene, darauf zugeschnittene, Betriebsysteme (Images), wobei  [Raspbian Jessie](https://www.raspberrypi.org/downloads/raspbian) verwendet werden sollte, da dieses bereits die vom *Smart Appliance Enabler* benötigte Java-Runtime beinhaltet ([Installationsanleitung](http://www.pc-magazin.de/ratgeber/raspberry-pi-raspbian-einrichten-installieren-windows-mac-linux-anleitung-tutorial-2468744.html)).

An die GPIO-Pins des Raspberry können diverse Schalter und/oder Stromzähler angeschlossen werden, d.h. ein einziger Raspberry Pi kann eine Vielzahl von Geräten verwalten. Dabei darf jedoch die **Stromstärke** am 5V-Pin den Wert von 300 mA (Model B) bzw. 500mA (Model A) und am 3,3V-Pin den Wert von 50mA nicht überschreiten ([Quelle](http://elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29))!

Die Nummerierung der Pins richtet sich nach [Pi4J](http://pi4j.com/images/gpio-control-example-large.png) und weicht von der offiziellen Nummerierung ab!

### Schaltbeispiele
Die nachfolgenden Schaltbeispiele zeigen Schaltungen zum Schalten mittels **Solid-State-Relais** und zur Stromverbrauchsmessung mittels Stromzähler mit **S0-Schnittstelle**. Beides ist unabhängig voneinander, d.h. Solid-State-Relais oder Stromzähler können entfallen, falls nur geschaltet oder der Stromverbrauch ermittelt werden soll.

In den Schaltbeispielen ist der für den Stromzähler notwendige **Pull-Down-Widerstand** nicht eingezeichnet, weil dafür die auf dem Raspberry Pi vorhandenen Pull-Down-Widerstände per Software-Konfiguration aktiviert werden.

*Hinweis: Die Installation von steckerlosen 200/400V-Geräten sollte grundsätzlich durch einen autorisierten Fachbetrieb vorgenommen werden!*

#### Schaltbeispiel 1: 240V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Schalten eines 240V-Gerätes (z.B. Pumpe) könnte wie folgt aussehen:

![Schaltbeispiel1](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/SmartHomeEnablerSchaltung.png)

#### Schaltbeispiel 2: 400V-Gerät mit Stromverbrauchsmessung
Der Aufbau zum Schalten eines 400V-Gerätes (z.B. Heizstab) könnte wie folgt aussehen:

![Schaltbeispiel2](https://github.com/camueller/SmartApplianceEnabler/blob/master/pics/Schaltbeispiel400VMitMessung.png)

## Software
### Dank und Anerkennung
Der *Smart Appliance Enabler* verwendet intern folgende Open-Source-Software:
* [Spring Boot](http://projects.spring.io/spring-boot) für RESTful Web-Services (SEMP-Protokoll)
* [Cling](http://4thline.org/projects/cling) für UPnP (SEMP-Protokoll)

### Bauen
Bevor die Software auf dem Raspberry Pi installiert werden kann, muß diese zunächst gebaut werden.
Den dafür notwendigen Source-Code kann man mit einem Git-Client ([Git installieren](https://git-scm.com/book/de/v1/Los-geht%E2%80%99s-Git-installieren)) herunterladen
```
git clone https://github.com/camueller/SmartApplianceEnabler.git
```

oder als [ZIP-Datei](https://github.com/camueller/SmartApplianceEnabler/archive/master.zip). Letzteres muß natürlich erst noch ausgepackt werden.

Zum Bauen ist weiterhin [Maven](https://maven.apache.org) erforderlich, das gegebenenfalls noch [installiert](https://maven.apache.org/install.html) werden muss.

Um *Smart Appliance Enabler* zu bauen, ruft man Maven im Verzeichnis *SmartApplianceEnabler* wie folgt auf:
```
axel@tpw520:~/git/SmartApplianceEnabler$ mvn clean package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building SmartApplianceEnabler 0.1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ SmartApplianceEnabler ---
[INFO] Deleting /data/git/SmartApplianceEnabler/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ SmartApplianceEnabler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ SmartApplianceEnabler ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 33 source files to /data/git/SmartApplianceEnabler/target/classes
[WARNING] /data/git/SmartApplianceEnabler/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java: /data/git/SmartApplianceEnabler/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java uses unchecked or unsafe operations.
[WARNING] /data/git/SmartApplianceEnabler/src/main/java/de/avanux/smartapplianceenabler/appliance/FileHandler.java: Recompile with -Xlint:unchecked for details.
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ SmartApplianceEnabler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /data/git/SmartApplianceEnabler/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ SmartApplianceEnabler ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.18.1:test (default-test) @ SmartApplianceEnabler ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.5:jar (default-jar) @ SmartApplianceEnabler ---
[INFO] Building jar: /data/git/SmartApplianceEnabler/target/SmartApplianceEnabler-0.1.0.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:1.3.0.RELEASE:repackage (default) @ SmartApplianceEnabler ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.348 s
[INFO] Finished at: 2015-12-24T17:49:00+01:00
[INFO] Final Memory: 28M/316M
[INFO] ------------------------------------------------------------------------
```
Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.jar` im Unterverzeichnis `target`.

## Konfiguration
Die Konfiguration besteht aus zwei [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language)-Dateien:
* die Datei `Device2EM.xml` enthält Gerätebeschreibung für den EnergyManager
* die Datei `Appliances.xml` enthält die Gerätekonfiguration für den Raspberry Pi

Im Verzeichnis `example` finden sich Beispieldateien mit Kommentaren zu den einzelnen Angaben.
Diese sollen dabei helfen, die für die eigenen Geräte passenden Dateien `Device2EM.xml` und `Appliances.xml` (mit genau diesen Namen und entsprechender Groß-/Kleinschreibung!) zu erstellen.

Die angepassten XML-Dateien sollten hinsichtlich ihrer Gültigkeit überprüft werden.
Dazu ist die Seite http://www.freeformatter.com/xml-validator-xsd.html besonders geeignet:
Der Inhalt der XML-Datei wird in das Fenster *XML Input* kopiert. Bei *XSD Input* muss nur *Option 2* eingegeben werden:
* beim Prüfen von Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.1.5.xsd
* beim Prüfen von Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-1.0.xsd

Ist die Prüfung erfolgreich, erscheint oberhalb des *XML Input* eine grün unterlegte Meldung *The XML document is fully valid.*. Bei Fehlern erscheint eine rot unterlegte Meldung mit entsprechender Fehlerbeschreibung.

### Installation
Die Installation des *Smart Appliance Enabler* besteht darin, folgende Dateien auf den Raspberry zu kopieren:
* die beim Bauen erstellte Datei `SmartApplianceEnabler-*.jar`
* die Konfigurationsdatei `Device2EM.xml`
* die Konfigurationsdatei `Appliances.xml`
* das Startscript `run.sh` aus dem Verzeichnis `run`
Dazu sollte entweder die IP-Adresse des Raspberry bekannt sein, oder der der Raspberry einen festen Hostnamen besitzen. Nachfolgend gehe ich von Letzterem aus, da er bei mir im Netz unter dem Namen `raspi` erreichbar ist.

Zunächst sollte auf dem Raspberry ein eigenes Verzeichis für diese Dateien erstellt werden, z.B. `/app` (das Passwort für den User *pi* ist *raspberry*, wenn Ihr das noch nicht geändert habt):
```
axel@tpw520:/data/git/SmartApplianceEnabler$ ssh pi@raspi
pi@raspi's password: 

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Sun Dec  6 19:17:12 2015
pi@raspberrypi ~ $ sudo mkdir /app
pi@raspberrypi ~ $ sudo chown pi.pi /app
pi@raspberrypi ~ $ exit
```
Danach muss man die genannten Dateien auf den Raspberry kopieren:
```
axel@tpw520:/data/git/SmartApplianceEnabler$ scp target/SmartApplianceEnabler-0.1.0.jar pi@raspi:/app
pi@raspi's password:
SmartApplianceEnabler-0.1.0.jar                         100%   15MB   1.4MB/s   00:11
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/Appliances.xml pi@raspi:/app
pi@raspi's password:
Appliances.xml                                          100%  590     0.6KB/s   00:00
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/Device2EM.xml  pi@raspi:/app
pi@raspi's password:
Device2EM.xml                                           100% 1288     1.3KB/s   00:00
axel@tpw520:/data/git/SmartApplianceEnabler$ scp run/run.sh  pi@raspi:/app
pi@raspi's password:
run.sh                                                  100%  153     0.2KB/s   00:00
axel@tpw520:/data/git/SmartApplianceEnabler$
```

Jetzt kann man sich erneut auf dem Raspberry einloggen um den *Smart Appliance Enabler* mittels des `run.sh`-Scriptes zu starten. Dabei sollte man etwa folgende Ausgaben zu sehen bekommen:
```
axel@tpw520:/data/git/SmartApplianceEnabler$ ssh pi@raspi
pi@raspi's password: 

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Sun Dec  6 19:17:12 2015
pi@raspberrypi /app $ ./run.sh 
sudo java -Dappliance.dir=. -jar SmartApplianceEnabler-0.1.0.jar
17:31:18,279 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback.groovy]
17:31:18,289 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback-test.xml]
17:31:18,290 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Found resource [logback.xml] at [jar:file:/app/SmartApplianceEnabler-0.1.0.jar!/logback.xml]
17:31:18,492 |-INFO in ch.qos.logback.core.joran.spi.ConfigurationWatchList@1a1bbc7 - URL [jar:file:/app/SmartApplianceEnabler-0.1.0.jar!/logback.xml] is not of type file
17:31:18,881 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - debug attribute not set
17:31:18,928 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]
17:31:18,990 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [STDOUT]
17:31:19,690 |-WARN in ch.qos.logback.core.ConsoleAppender[STDOUT] - This appender no longer admits a layout as a sub-component, set an encoder instead.
17:31:19,690 |-WARN in ch.qos.logback.core.ConsoleAppender[STDOUT] - To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.
17:31:19,690 |-WARN in ch.qos.logback.core.ConsoleAppender[STDOUT] - See also http://logback.qos.ch/codes.html#layoutInsteadOfEncoder for details
17:31:19,693 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to INFO
17:31:19,694 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [STDOUT] to Logger[ROOT]
17:31:19,698 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.
17:31:19,704 |-INFO in ch.qos.logback.classic.joran.JoranConfigurator@e0ed11 - Registering current configuration as safe fallback point


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.3.0.RELEASE)

17:31:23.337 [main] INFO  d.a.s.Application - Starting Application v0.1.0 on raspberrypi with PID 1591 (/app/SmartApplianceEnabler-0.1.0.jar started by root in /app)
17:31:23.358 [main] INFO  d.a.s.Application - No profiles are active
17:31:23.938 [main] INFO  o.s.b.c.e.AnnotationConfigEmbeddedWebApplicationContext - Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@55e19a: startup date [Sun Dec 27 17:31:23 UTC 2015]; root of context hierarchy
17:31:25.569 [pool-1-thread-1] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.2.2.Final
17:31:31.566 [main] INFO  o.s.b.f.s.DefaultListableBeanFactory - Overriding bean definition for bean 'beanNameViewResolver' with a different definition: replacing [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration$WhitelabelErrorViewConfiguration; factoryMethodName=beanNameViewResolver; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [org/springframework/boot/autoconfigure/web/ErrorMvcAutoConfiguration$WhitelabelErrorViewConfiguration.class]] with [Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter; factoryMethodName=beanNameViewResolver; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [org/springframework/boot/autoconfigure/web/WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter.class]]
17:31:36.930 [main] INFO  o.s.b.c.e.t.TomcatEmbeddedServletContainer - Tomcat initialized with port(s): 8080 (http)
17:31:37.032 [main] INFO  o.a.catalina.core.StandardService - Starting service Tomcat
17:31:37.040 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet Engine: Apache Tomcat/8.0.28
17:31:37.720 [localhost-startStop-1] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
17:31:37.722 [localhost-startStop-1] INFO  o.s.web.context.ContextLoader - Root WebApplicationContext: initialization completed in 13815 ms
17:31:40.674 [localhost-startStop-1] INFO  o.s.b.c.e.ServletRegistrationBean - Mapping servlet: 'dispatcherServlet' to [/]
17:31:40.720 [localhost-startStop-1] INFO  o.s.b.c.e.FilterRegistrationBean - Mapping filter: 'characterEncodingFilter' to: [/*]
17:31:40.722 [localhost-startStop-1] INFO  o.s.b.c.e.FilterRegistrationBean - Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
17:31:40.723 [localhost-startStop-1] INFO  o.s.b.c.e.FilterRegistrationBean - Mapping filter: 'httpPutFormContentFilter' to: [/*]
17:31:40.724 [localhost-startStop-1] INFO  o.s.b.c.e.FilterRegistrationBean - Mapping filter: 'requestContextFilter' to: [/*]
17:31:41.133 [main] INFO  d.a.s.appliance.FileHandler - Using appliance directory .
17:31:41.569 [main] INFO  d.a.s.semp.webservice.SempController - Controller ready to handle SEMP requests.
17:31:43.975 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerAdapter - Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@55e19a: startup date [Sun Dec 27 17:31:23 UTC 2015]; root of context hierarchy
17:31:44.690 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/semp/DeviceStatus],methods=[GET],produces=[application/xml]}" onto public java.lang.String de.avanux.smartapplianceenabler.semp.webservice.SempController.deviceStatus(java.lang.String)
17:31:44.697 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/semp],methods=[GET],produces=[application/xml]}" onto public java.lang.String de.avanux.smartapplianceenabler.semp.webservice.SempController.device2EM()
17:31:44.699 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/semp/DeviceInfo],methods=[GET],produces=[application/xml]}" onto public java.lang.String de.avanux.smartapplianceenabler.semp.webservice.SempController.deviceInfo(java.lang.String)
17:31:44.700 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/semp/PlanningRequest],methods=[GET],produces=[application/xml]}" onto public java.lang.String de.avanux.smartapplianceenabler.semp.webservice.SempController.planningRequest(java.lang.String)
17:31:44.702 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/semp],methods=[POST],consumes=[application/xml]}" onto public void de.avanux.smartapplianceenabler.semp.webservice.SempController.em2Device(de.avanux.smartapplianceenabler.semp.webservice.EM2Device)
17:31:44.719 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest)
17:31:44.721 [main] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
17:31:45.122 [main] INFO  o.s.w.s.h.SimpleUrlHandlerMapping - Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
17:31:45.122 [main] INFO  o.s.w.s.h.SimpleUrlHandlerMapping - Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
17:31:45.593 [main] INFO  o.s.w.s.h.SimpleUrlHandlerMapping - Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
17:31:46.779 [main] INFO  o.s.j.e.a.AnnotationMBeanExporter - Registering beans for JMX exposure on startup
17:31:47.013 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-8080"]
17:31:47.105 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-8080"]
17:31:47.176 [main] INFO  o.a.tomcat.util.net.NioSelectorPool - Using a shared selector for servlet write/read
17:31:47.318 [main] INFO  o.s.b.c.e.t.TomcatEmbeddedServletContainer - Tomcat started on port(s): 8080 (http)
17:31:47.345 [main] INFO  d.a.s.Application - Started Application in 26.603 seconds (JVM running for 30.461)
17:31:47.624 [Thread-4] INFO  org.fourthline.cling.UpnpServiceImpl - >>> Starting UPnP service...
17:31:47.626 [Thread-4] INFO  org.fourthline.cling.UpnpServiceImpl - Using configuration: de.avanux.smartapplianceenabler.semp.discovery.SempDiscovery$1
17:31:47.809 [Thread-4] INFO  o.fourthline.cling.transport.Router - Creating Router: org.fourthline.cling.transport.RouterImpl
17:31:47.851 [Thread-4] INFO  o.f.c.t.spi.MulticastReceiver - Creating wildcard socket (for receiving multicast datagrams) on port: 1900
17:31:47.864 [Thread-4] INFO  o.f.c.t.spi.MulticastReceiver - Joining multicast group: /239.255.255.250:1900 on network interface: eth0
17:31:47.941 [Thread-7] INFO  d.a.s.appliance.ApplianceManager - 1 appliance(s) configured.
17:31:47.943 [Thread-4] INFO  o.f.cling.transport.spi.StreamServer - Created socket (for receiving TCP streams) on: /192.168.69.5:52416
17:31:47.958 [Thread-4] INFO  o.f.cling.transport.spi.DatagramIO - Creating bound socket (for datagram input/output) on: /192.168.69.5
17:31:47.973 [Thread-7] INFO  d.a.s.appliance.Switch - Switch uses pin GPIO 1 (reversed states)
17:31:48.297 [Thread-4] INFO  org.fourthline.cling.UpnpServiceImpl - <<< UPnP service started successfully
17:31:52.161 [cling-5] INFO  d.a.s.s.d.SempDeviceDescriptorBinderImpl - SEMP UPnP will redirect to http://192.168.69.5:8080
```
## Integration in den SMA Home Manager
Der *SMA Home Manager* sollte jetzt den *Smart Appliance Enabler* finden und die von ihm verwalteten Geräte konfigurieren können. Falls das nicht so ist, sollen folgende Punkte geprüft werden:

### Erweiterte Meldungsausgabe
Auf der Console werden normalerweise nur wichtig Meldungen ausgegeben. Zur Fehlersuche kann es jedoch hilfreich sein, zusätzliche Meldungen anzeigen zu lassen. Dazu wird beim Start des *Smart Appliance Enabler* einfach der Parameter `-d` angehangen: 
```
pi@raspberrypi /app $ ./run.sh -d
```
### Verbindung zwischen Home Manager und Smart Appliance Enabler
Home Manager auf den *Smart Appliance Enabler* müssen sich im gleichen Netz befinden!
Wenn die Erweiterte Meldungsausgabe zuvor aktiviert wurde, kann man auf der Console sehen, wenn der Home Manager auf den *Smart Appliance Enabler* zugreift:
```
20:25:17.390 [http-nio-8080-exec-1] DEBUG d.a.s.semp.webservice.SempController - Device info/status/planning requested.
```
## Fragen / Fehler
Bei Verdacht auf Fehler in der Software oder bei Fragen zur Verwendung des *Smart Appliance Enabler* sollte [Issue](https://github.com/camueller/SmartApplianceEnabler/issues) erstellt werden.

## Lizenz
Die Inhalte in diesem Repository sind lizensiert unter der [GNU GENERAL PUBLIC LICENSE](LICENSE.txt), falls nicht anders angegeben.
