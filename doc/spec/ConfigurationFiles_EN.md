# Configuration files

## Device2EM.xml and Appliances.xml
The configuration of the *Smart Appliance Enabler* is located in two [XML](https://de.wikipedia.org/wiki/Extensible_Markup_Language) files:
* the file `Device2EM.xml` contains a device description for the Sunny Home Manager
* the file `Appliances.xml` contains the device configuration for the *Smart Appliance Enabler*

The case of the file names must be exactly as specified here!

The files must be in the directory pointed to by the variable `SAE_HOME` (usually `/opt/sae`)

### Save button
This data is written when you click the save button on the web interface. This will restart the *Smart Appliance Enabler* internally for the changed configuration to take effect. Currently running devices are always stopped/switched off at this moment in order to maintain a defined status. Energy/runtimes of devices that have already been used are also reset and rescheduled. To avoid this, you should change the configuration when the devices to be controlled are not running.

### Manual changes in the configuration files
Danger! Making direct changes to the configuration files can result in the *Smart Appliance Enabler* no longer being able to use these files and/or not starting! Be sure to back up these files beforehand!

You can either edit the XML files on the Raspberry Pi or [transfer them to the PC](#scp).

The customized XML files should be checked for validity. The page http://www.freeformatter.com/xml-validator-xsd.html is particularly suitable for this:

The content of the XML file is copied to the *XML Input* window.

The content (not the URL itself!) of the following URL must be copied into the *XSD Input* window:
* checking Device2EM.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SEMP-1.3.xsd
* when checking Appliances.xml: https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/xsd/SmartApplianceEnabler-2.0.xsd

If the check is successful, a message with a green background *The XML document is valid.* appears above the *XML input*. In the event of errors, a message with a red background appears with a corresponding description of the error.

### <a name="scp"></a> Copying the configuration files between Raspberry Pi and PC

To transfer the files between Raspberry Pi and PC you can use `scp` under Linux, under Windows there is `WinSCP` ([Video with WinSCP instructions in German](https://www.youtube.com/watch?v=z6yJDMjTdMg )).

*Webmin*: With the [Webmin file manager](Webmin_EN.md) the transfer between Raspberry Pi and PC can be done under Linux in the browser.

## <a name="etc-default-smartapplianceenabler"></a> Server configuration

The configuration settings for the *Smart Appliance Enabler* can be found in the file `/etc/default/smartapplianceenabler`. The parameters contained therein (e.g. network address, port, Java settings, ...) are documented in the file itself. Normally you should be able to leave the file unchanged.

## <a name="log-configuration"></a> Log configuration

The logging is configured in the file `/opt/sae/logback-spring.xml`.
