# Mercedes Me
Mercedes stellt eine API bereit, die es erlaubt, verschiedene Daten wie z.B. SoC oder Restreichweite des eigenen Fahrzeugs auszulesen. Anbei findet sich ein shellbasiertes Skript, das an das Projekt "Mercedes_me_Api" angelehnt ist (https://github.com/xraver/mercedes_me_api).

Voraussetzung ist, dass man einen Mercedes Me Account erstellt und dort in der Diensteverwaltung für das Auto den Dienst "Schnittstelle Drittanbieter: Fahrzeugdaten" aktiviert hat. Nun loggt man sich im [Developer-Portal](https://developer.mercedes-benz.com/) ein. Dort muss ein Projekt erstellt werden, dem dann verschiedene Dienste zugeordnet werden können. Für den SoC ist der Dienst "Electric Vehicle Status" wichtig. Unter der "Bring your own car"-Option ist der Zugriff aufs eigene Fahrzeug kostenlos möglich. Beim Anlegen des Projekts wird eine Client Id und ein Client Secret erzeugt, die man später für die Authentisierung braucht.
Die API verwendet das OAUTH2.0 Verfahren.

Im Folgenden werden mehrere Skripte erstellt. Eines stellt die Verbindung zur API von Mercedes her und dient gleichzeitig der Generierung von Access- und Refreshtoken. Ein zweites Skript ruft das erste in Verbindung mit einem Argument auf um den SoC zu erhalten. Dieses Skript wird vom SAE ausgeführt. Ein drittes Script wird als Cronjob eingerichtet, um den nur 60 Minuten gültigen Token zu erneuern.

## Mercedes Me API-Script
Als Erstes muss das Verzeichnis für die Skripte und die Konfigurationsdatei angelegt und dorthin gewechselt werden:

```bash
$ mkdir /opt/sae/soc
$ cd /opt/sae/soc
```

Die Konfigurationsdatei mit den auf der Developer-Seite erhaltenen IDs wird mit dem Namen `.mercedesme_config` im selben Verzeichnis erstellt und hat folgenden Inhalt:

```bash
CLIENT_ID="INSERT_YOUR_CLIENT_ID"
CLIENT_SECRET="INSERT_YOUR_CLIENT_SECRET"
VEHICLE_ID="INSERT_YOUR_VEHICLE_ID"
```

Entsprechende Felder in `""` sind mit der Client ID und dem Client Secret des Developer-Projekts zu ersetzen. VEHICLE_ID ist die Fahrzeugidentifikationsnummer.

Das eigentliche Mercedes Me API-Script wird mit dem Namen `mercedes_me_api.sh` und folgendem Inhalt angelegt:

```bash
#!/bin/bash

# Author: G. Ravera
# Version 0.8
# Creation Date: 28/09/2020
#
# Change log:
#             28/09/2020 - 0.1 - First Issue
#             18/10/2020 - 0.2 - Added the possibility to retrieve the list of resources available
#             03/12/2020 - 0.3 - Fix in resources list
#             18/12/2020 - 0.4 - Added macOS support (robert@klep.name)
#             19/12/2020 - 0.5 - Added Electric Vehicle Status support
#             23/12/2020 - 0.6 - Added PayAsYouDrive support (danielrheinbay@gmail.com)
#             04/03/2022 - 0.7 - Only Electric Vehicle Status support
#             06/10/2022 - 0.8 - Implement changes of new API management

# Script Name & Version
NAME="mercedes_me_api.sh"
VERSION="0.8"

# Script Parameters
TOKEN_FILE=".mercedesme_token"
CONFIG_FILE=".mercedesme_config"
# Mercedes me Application Parameters
REDIRECT_URL="https://localhost"
SCOPE="openid%20mb:vehicle:mbdata:evstatus%20offline_access"
RES_URL_PREFIX="https://api.mercedes-benz.com/vehicledata/v2"
# Resources
RES_ELECTRIC=(soc rangeelectric)

# set "extended regular expression" argument for sed based on OS
if [ "X$(uname -s)" = "XDarwin" ]
then
  SED_FLAG="-E"
else
  SED_FLAG="-r"
fi

# Credentials
CLIENT_ID=""
CLIENT_SECRET=""
VEHICLE_ID=""
# Loading Credentials
if [[ -f "$CONFIG_FILE" ]]; then
  . $CONFIG_FILE
fi
if [ -z $CLIENT_ID ] | [ -z $CLIENT_ID ] | [ -z $CLIENT_ID ]; then
  echo "Please create $CONFIG_FILE with CLIENT_ID=\"\", CLIENT_SECRET=\"\", VEHICLE_ID=\"\""
  exit
fi

# Formatting RES_URL
RES_URL="$RES_URL_PREFIX/vehicles/$VEHICLE_ID/resources"

function usage ()
{
  echo "Usage:    $NAME <arguments>"
  echo
  echo "Example:  $NAME --token --electric-status"
  echo "     or:  $NAME -e"
  echo
  echo "Arguments:"
  echo "    -t, --token           Procedure to obtatin the Access Token (stored into $TOKEN_FILE)"
  echo "    -r, --refresh         Procedure to refresh the Access Token (stored into $TOKEN_FILE)"
  echo "    -e, --electric-status Retrieve the General Electric Status of your Vehicle"
  echo "    -R, --resources       Retrieve the list of available resources of your Vehicle"
  exit
}

function parse_options ()
{
	# Check Options
	OPT=$(getopt -o trflseoR --long token,refresh,electric-status,resources -n "$NAME parse-error" -- "$@")
	if [ $? != 0 ] || [ $# -eq 0 ]; then
		usage
	fi

	eval set -- "$OPT"

	# Parse Options
	while [ $# -gt 0 ]; do
		case $1 in
			-t | --token )
				getAuthCode
				shift
				;;
			-r | --refresh )
				refreshAuthCode	
				shift
				;;
			-e | --electric-status )
				printStatus "${RES_ELECTRIC[@]}"
				shift
				;;
			-R | --resources )
				printResources
				shift
				;;
			* ) shift ;;
		esac
	done
}

function generateBase64 ()
{
  BASE64=$(echo -n $CLIENT_ID:$CLIENT_SECRET | base64 | sed $SED_FLAG 's/ //')
  BASE64=$(echo $BASE64 | sed $SED_FLAG 's/ //')
}

function getAuthCode () 
{
  generateBase64
  
  echo "Open the browser and insert this link:"
  echo 
  echo "https://ssoalpha.dvb.corpinter.net/v1/auth?response_type=code&client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URL&scope=$SCOPE"
  echo 
  echo "Copy the code in the url:"

  read AUTH_CODE

  TOKEN=$(curl --request POST \
               --url https://ssoalpha.dvb.corpinter.net/v1/token \
               --header "Authorization: Basic $BASE64" \
               --header "content-type: application/x-www-form-urlencoded" \
               --data "grant_type=authorization_code&code=$AUTH_CODE&redirect_uri=$REDIRECT_URL")

  echo $TOKEN > $TOKEN_FILE
}

function refreshAuthCode ()
{
  generateBase64
  extractRefreshToken

  TOKEN=$(curl --request POST \
               --url https://ssoalpha.dvb.corpinter.net/v1/token \
               --header "Authorization: Basic $BASE64" \
               --header "content-type: application/x-www-form-urlencoded" \
               --data "grant_type=refresh_token&refresh_token=$REFRESH_CODE")

  echo $TOKEN > $TOKEN_FILE
}

function extractAccessToken ()
{
  ACCESS_TOKEN=$(cat $TOKEN_FILE | grep -Eo '"access_token"[^:]*:[^"]*"[^"]+"' | grep -Eo '"[^"]+"$' | tr -d '"')
}

function extractRefreshToken ()
{
  REFRESH_CODE=$(cat $TOKEN_FILE | grep -Eo '"refresh_token"[^:]*:[^"]*"[^"]+"' | grep -Eo '"[^"]+"$' | tr -d '"')
}

function printStatus ()
{
  extractAccessToken

  for r in "$@"
    do
    echo "Retrieving $r:"
    curl -X GET "$RES_URL/$r" -H "accept: application/json;charset=utf-8" -H "authorization: Bearer $ACCESS_TOKEN"
    echo
  done
}

function printResources ()
{
  extractAccessToken

  curl -X GET "$RES_URL" -H "accept: application/json;charset=utf-8" -H "authorization: Bearer $ACCESS_TOKEN"

}

echo $NAME - $VERSION
echo
parse_options $@
```

Nun wird das eigentliche Skript `/opt/sae/soc/soc.sh` angelegt, das dann vom *Smart Appliance Enabler* aufgerufen wird. Es hat diesen Inhalt:

```bash
#!/bin/sh
cd /opt/sae/soc
./mercedes_me_api.sh -e
```

Beide Skripte müssen ggf. noch sae:sae zugewiesen und ausführbar gemacht werden:

```bash
$ chown sae:sae -R /opt/sae/soc/
$ chmod +x /opt/sae/soc/mercedes_me_api.sh
$ chmod +x /opt/sae/soc/soc.sh
```

### Erstmalige Generierung von Access- und Refreshtoken
Um einen Accesstoken zu erhalten, muss das Skript `mercedes_me_api.sh` manuell mit dem Argument `-t` ausgeführt werden. 

```bash
$ /opt/sae/soc/mercedes_me_api.sh -t
```

Man wird nun aufgefordert, eine URL aufzurufen. Diese einfach kopieren und mit einem Browser öffnen. Nun gelangt man zur Authentifizierung der angefragten Dienste. Dort muss der Zugriff bestätigt werden. Anschließend erfolgt ein redirect auf eine Seite mit der url https://localhost, was verständlicherweise zu einer Fehlermeldung führt. Hier ist aber der "Code" wichtig, der sich in dieser URL verbirgt. Diesen Code kopieren und in das laufende Skript einfügen. Mit Bestätigung durch die Eingabetaste ist die Tokengenerierung erfolgt. Im Hintergrund wird dabei eine Datei namens `.mercedesme_token` angelegt.

### Ausführung
Nun kann das angelegte Skript gestestet werden und sollte SoC und Restreichweite zurückgeben.

```bash
$ /opt/sae/soc/soc.sh
mercedes_me_api.sh - 0.8

Retrieving soc:
{"soc":{"value":"56","timestamp":1646662088000}}
Retrieving rangeelectric:
{"rangeelectric":{"value":"227","timestamp":1646666769000}}
```

Im *Smart Appliance Enabler* wird als SoC-Script `/opt/sae/soc/soc.sh` angegeben.
Außerdem muss der nachfolgende *reguläre Ausdruck* angegeben werden, um aus den Ausgaben den eigentlichen Zahlenwert zu extrahieren:

```
.*soc":\{"value":"([0-9.]+).*
```

### Cronjob zur Generierung des Accesstoken
Da der Accesstoken nach 60 Minuten abläuft, muss ein Cronjob eingerichtet werden, der innerhalb dieser Zeit mit Hilfe des Refreshtokens einen neuen Accesstoken erzeugt. Dafür wird ein entsprechendes Script im `hourly`-Ordner des Cron-Dienstes angelegt: `/etc/cron.hourly/token_refresh.sh`. Der Inhalt des Scripts sieht wie folgt aus:

```bash
#!/bin/sh
cd /opt/sae/soc
./mercedes_me_api.sh -r
```
