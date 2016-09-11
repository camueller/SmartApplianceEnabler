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
mvn clean package
```

Beim erstmaligen Aufruf von Maven werden dabei alle benötigten Bibliotheken aus dem offiziellen Maven-Repository heruntergeladen. Das Bauen war nur dann erfolgreich, wenn *BUILD SUCCESS* erscheint! In diesem Fall findet sich die Datei `SmartApplianceEnabler-*.jar` im Unterverzeichnis `target`.

