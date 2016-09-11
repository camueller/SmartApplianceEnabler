### Bauen
Bevor die Software auf dem Raspberry Pi installiert werden kann, muß diese zunächst gebaut werden.
Den dafür notwendigen Source-Code kann man mit einem Git-Client ([Git installieren](https://git-scm.com/book/de/v1/Los-geht%E2%80%99s-Git-installieren)) herunterladen
```
git clone https://github.com/camueller/SmartApplianceEnabler.git
```

oder als [ZIP-Datei](https://github.com/camueller/SmartApplianceEnabler/archive/master.zip). Letzteres muß natürlich erst noch ausgepackt werden.

Zum Bauen ist weiterhin [Maven](https://maven.apache.org) erforderlich, das gegebenenfalls noch [installiert](https://maven.apache.org/install.html) werden muss.

Um *Smart Appliance Enabler* zu bauen, ruft man Maven im Verzeichnis *SmartApplianceEnabler* zunächst nur mit dem Goal ```clean``` auf, damit die Bibliothek [J2Mod](https://sourceforge.net/projects/j2mod) im lokalen Maven-Repository installiert wird:
```
axel@tpw520:~/git/SmartApplianceEnabler$ mvn clean
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building SmartApplianceEnabler 0.1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ SmartApplianceEnabler ---
[INFO] Deleting /data/git/SmartApplianceEnabler/target
[INFO] 
[INFO] --- maven-install-plugin:2.5.2:install-file (default) @ SmartApplianceEnabler ---
[INFO] pom.xml not found in j2mod-1.06.jar
[INFO] Installing /data/git/SmartApplianceEnabler/lib/j2mod-1.06.jar to /home/axel/.m2/repository/com/ghgande/j2mod/1.06/j2mod-1.06.jar
[INFO] Installing /tmp/mvninstall9214044059867338460.pom to /home/axel/.m2/repository/com/ghgande/j2mod/1.06/j2mod-1.06.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 0.492 s
[INFO] Finished at: 2016-02-21T18:58:17+01:00
[INFO] Final Memory: 12M/304M
[INFO] ------------------------------------------------------------------------
```
Jetzt kann man den eigentlichen Build von *SmartApplianceEnabler* starten:

```
axel@tpw520:~/git/SmartApplianceEnabler$ mvn package
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
Nachdem man einmalig ```mvn clean``` aufgerufen hat, kann man nachfolgend immer die Goals ``` clean``` und ```build``` zusammenfassen zu ```mvn clean build```.

Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.jar` im Unterverzeichnis `target`.

