# Hinweise zur Konfiguation der Umgebung zum Bauen der Docker-Images

1. Docker installieren

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
sae@raspi3:/etc/cron.hourly $
```