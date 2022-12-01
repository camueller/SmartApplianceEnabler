# Node-RED
[Node-RED](https://nodered.org/) can be used to visualize the *Smart Appliance Enabler* MQTT messages in the Node-RED dashboard. The performance charts in particular can help to understand and optimize the behavior of the *Smart Appliance Enabler*.

## Installation without Docker
A separate user should be used for Node-RED, which is added to the `sudo` group and whose password is also set:
```console
pi@raspberrypi ~ $ sudo useradd -d /opt/nodered -m -s /bin/bash nodered
pi@raspberrypi ~ $ sudo usermod -a -G sudo nodered
pi@raspberrypi ~ $ sudo passwd nodered
```

From now on you should work with this user.

Node-RED can be installed from the Raspbian repository with `apt install ...`, but you will end up with an outdated version of Node-RED and Node.js, which may therefore not be able to use libraries directly from to install `github`. Therefore, the installation of Node-RED and also node.js should be done according to the [instructions on the Node-RED homepage](https://nodered.org/docs/getting-started/raspberrypi):
```console
nodered@raspberrypi ~ $ bash <(curl -sL https://raw.githubusercontent.com/node-red/linux-installers/master/deb/update-nodejs-and-nodered)
Running Node-RED update for user nodered at /opt/nodered on raspbian

[sudo] password for nodered: 

This can take 20-30 minutes on the slower Pi versions - please wait.

  Stop Node-RED                       ✔
  Remove old version of Node-RED      ✔
  Remove old version of Node.js       ✔   
  Install Node.js 14 LTS              ✔   v14.18.2   Npm 6.14.15
  Clean npm cache                     ✔
  Install Node-RED core               ✔   2.1.4 
  Move global nodes to local          -
  Npm rebuild existing nodes          ✔
  Install extra Pi nodes              -
  Add shortcut commands               ✔
  Update systemd script               ✔
                                      

Any errors will be logged to   /var/log/nodered-install.log
All done.
You can now start Node-RED with the command  node-red-start
  or using the icon under   Menu / Programming / Node-RED
Then point your browser to localhost:1880 or http://{your_pi_ip-address}:1880

Started :  Sat Jan  1 15:32:31 CET 2022 
Finished:  Sat Jan  1 15:35:15 CET 2022
 
You may want to run   node-red admin init
to configure your initial options and settings.
```
The following command is suitable for starting:
```console
nodered@raspberrypi ~ $ sudo systemctl start nodered
```
So that Node-RED is also started when the system starts (via systemd), the following command must be executed:
```console
pi@raspberrypi ~ $ sudo systemctl enable nodered
Created symlink /etc/systemd/system/multi-user.target.wants/nodered.service → /lib/systemd/system/nodered.service.
```

## Installation using Docker
Alternatively, Node-RED can also be installed as a Docker container. The corresponding Docker container is installed with:
```console
docker pull nodered/node-red
```
A volume is required to store the data:
```console
docker volume create node_red_data
```
The following command is suitable for starting:
```console
$ docker run -it --rm -p 1880:1880 -v node_red_data:/data --name nodered nodered/node-red
```

## Installation of required libraries
The following modules must be installed via `Manage Palette -> Install`:
- node-red-node-ui-table
- node-red-dashboard

For the subsequent installation of `camueller/node-red-contrib-ui-timelines-chart` `git` must be installed, which can be achieved with the command `sudo apt install git`.

A few more libraries need to be installed manually in the shell while in the `~/.node-red` (Docker: `/data`) directory:
- camueller/node-red-contrib-ui-timelines-chart
- date-fns

To do this, the following command must be executed once for each name from the previous list, whereby `<name>` must be replaced by the list entry:
```console
$ npm i <name>
```

The library `date-fns` still has to be entered in the file `~/.node-red/settings.js` (Docker: `/data/settings.js`). To do this, look for `functionGlobalContext` in the file and change it as follows:
```console
functionGlobalContext: {                                                         
  datefns:require('date-fns')                                                  
},
```

## Export of flows
![Export der Flows](../pics/nodered/StatusExport.png)

Clicking on the export button opens the export dialog:

![Export der Flows](../pics/nodered/ExportDialog.png)

In the export dialog, the `Copy content to clipboard` button can be pressed directly to copy the displayed Node-RED Flow JSON to the clipboard. This completes the export.

## Import of flows
### Remove existing flows
The flow JSON exported by the *Smart Appliance Enabler* is complete, i.e. it contains the global configuration nodes in addition to the flows.

To avoid duplicate nodes and the associated errors or performance losses, the nodes originating from a previous import of the flow JSON exported by the *Smart Appliance Enabler* should be deleted before importing again.

If Node-RED is only used for the *Smart Appliance Enabler*, all flows can be deleted simply by deleting the `~/.node-red/flows.json` file and restarting Node-RED - done!

Alternatively, you can selectively delete the following items:
- flows
    - `General`
    - all flows with names according to device ids `F-.....`
- Dashboard
    - `General`
    - all dashboard groups with names according to device ids `F-.....`
- Global configuration nodes
    - mqtt-broker: `MQTT Broker (SAE)`
    - [if Node-RED Dashboard is only used by *Smart Appliance Enabler*] ui_base: `Node-RED Dashboard`
    - ui-tab: `Smart Appliance Enabler`
    - ui-group: all entries with `[Smart Appliance Enabler]`

### Import
The flows are imported into Node-RED via the `Import` menu:

![Menu Import](../pics/nodered/MenuImport.png)

After clicking in the central, red area of ​​the import dialog, the Node-RED Flow JSON can be pasted from the clipboard. By clicking the `Import` button, the flows are imported into Node-RED.

A tab with a flow is created for each device in the *Smart Appliance Enabler*:

![Flow](../pics/nodered/Flow.png)

In addition, a `General` tab for device-independent nodes is created.

### Adaptation of the configured MQTT broker
After the import there is a global configuration node named `MQTT-Broker (SAE)`. This contains the host name and port of the MQTT broker, as specified in the *Smart Appliance Enabler* settings. Normally it should not be necessary to configure these values ​​differently in Node-RED.

If Node-RED and MQTT server are operated as a Docker container, it must be noted that its IP address in the Docker Bridge network is used for container-to-container communication. This can be determined as follows ([see also](https://www.tutorialworks.com/container-networking/)):
```console
$ docker inspect mosquitto | grep IPAddress
            "SecondaryIPAddresses": null,
            "IPAddress": "172.17.0.2",
                    "IPAddress": "172.17.0.2",
```

### Deploying flows
Normally it should be possible to deploy the imported flows directly without any adjustments.

To take over, you only have to press the `Deploy` button:
![Flow](../pics/nodered/Deploy.png)

After the transfer, `Connected` must appear in all tabs below the `MQTT in` node:

![MQTT connected](../pics/nodered/MqttConnected.png)

If not, then Node-RED was unable to connect to the MQTT broker.

## Dashboard
In the settings of the *Smart Appliance Enabler*, the `Dashboard URL` of Node-RED can be configured, which is called up when you click on the link button in the status display.

![Einstellungen Dashboard URL](../pics/nodered/EinstellungenDashboardUrl.png)

You will almost always have to adjust the default URL `http://localhost:1880/ui` displayed as a placeholder so that `localhost` is replaced by the host name under which Node-RED can be reached.

The Node-RED dashboard can then be called up via the link button in the status display of the *Smart Appliance Enabler*:
![Aufruf des Dashboards](../pics/nodered/StatusDashboard.png)

The dashboard itself shows the following information on a tab called `Smart Appliance Enabler`:
- Time of the last query of the *Smart Appliance Enabler* by the *Sunny Home Manager*
- all devices managed by *Smart Appliance Enabler*

![Dashboard](../pics/nodered/Dashboard.png)

The dashboard receives all MQTT messages from the *Smart Appliance Enabler* and updates itself automatically. As a result, it offers a real-time display of the status of the *Smart Appliance Enabler*. 