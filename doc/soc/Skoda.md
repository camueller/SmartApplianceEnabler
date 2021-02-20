# Skoda
Skoda stellt leider auch kein offizielles API zur Verfügung, jedoch kann mittels Skoda Connect / MySkoda auf die Daten der Fahrzeugs zugegriffen werden.
Voraussetzung ist ein Account, der via www.skoda-connect.com genutzt werden kann. Dies gilt auch für den Citigo E iV, bei dem im Portal nichts angezeigt wird.  

**Hinweis:** Die Tests wurde mit einem Citigo E iV durchgeführt!  
**Dank an:** Die hier vorgestellte Lösung nutzt das API skodaconnect https://pypi.org/project/skodaconnect/, da die Authentisierung bei Skoda deutlich anders durchgeführt wird als bei Volkswagen: 

## Python-Implementierung
### Installation
Zunächst muss der Python-Package-Manager installiert werden.  
Entweder man arbeitet direkt auf dem Raspberry oder man nutzt den Webmin https://raspberrypi:10000 Zugang und startet dort die Command Shell, um die folgenden zwei Befehle nacheinander auszuführen. Die jeweilige installation kann durchaus etwas dauern, da die Packages erst aus dem Internet geladen werden müssen: 
```console
yes | sudo apt install python3-pip
```
```console
sudo pip3 install skodaconnect
```
