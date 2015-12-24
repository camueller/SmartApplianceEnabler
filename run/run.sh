#!/bin/sh
sudo java -Xdebug -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -Dappliance.dir=/app -jar SmartApplianceEnabler-0.1.0.jar
