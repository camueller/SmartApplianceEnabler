# Hinweise für die Enwicklung

## UPnP Deaktivierung
In der Regel ist es nicht erwünscht, dass der Sunny Home Manager (SHM) die für die Entwicklung verwendete SAE-Instanz (in der IDE oder auf einem Entwicklungs-Raspi) per UPnP "entdeckt" und die Geräte übernimmt.
Deshalb kann das UPnP des SAE mit einem Property deaktiviert werden:
```
-Dsae.discovery.disable=true
```
