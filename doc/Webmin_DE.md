# Administration von Raspberry Pi und Smart Appliance Enabler mit `webmin`

Standardmässig wird [webmin](https://www.webmin.com) installiert, wodurch der Raspberry Pi mittels Web-Browser administriert werden kann. Das ist sehr hilfreich, wenn man sich nicht mit Linux auskennt.

`webmin` ist erreichbar unter der URL: `https://raspi:10000`, wobei "raspi" durch Hostname oder IP-Adresse des Raspberry PI zu ersetzen ist. Aktuell scheint das Zertifikat nicht gültig zu sein, weshalb man im Web-Browser bestätigen muss, dass man trotzdem *webmin* aufrufen möchte.

Danach erscheint die Anmeldeseite von *webmin*, wo man sich mit dem Benutzer `sae` und dem während der Installation vergebenen Passwort anmelden kann.

![Login](../pics/webmin/login.png)

Nach der erfolgreichen Anmeldung gelangt man zum **Dashboard**, das einen guten Überblick über den aktuellen Systemzustand bietet:

![Dashboard](../pics/webmin/dashboard.png)
