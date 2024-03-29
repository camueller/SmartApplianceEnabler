#!/bin/sh
#
# Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#

set -x

CONFIG_FILE=/usr/local/etc/install.config
. $CONFIG_FILE

echo "$PREFIX Waiting until boot is complete ..." >> $LOG
sleep 60

# inspired by https://github.com/Honey-Pi/HoneyPi-Build-Raspbian/blob/master/stage-honeypi/01-install-honeypi/00-run.sh
echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections
export DEBIAN_FRONTEND=noninteractive

OS_RELEASE=`cat /etc/os-release | grep bullseye`
if [ -z "$OS_RELEASE" ] ; then
  # inspired by https://linuxnews.de/2021/11/raspberry-pi-os-auf-bullseye-aktualisieren/
  apt -y update 2>&1 >> $LOG
  apt -qy dist-upgrade 2>&1 >> $LOG
  sed -i 's/buster/bullseye/g' /etc/apt/sources.list
  sed -i 's/buster/bullseye/g' /etc/apt/sources.list.d/raspi.list
  apt -y update 2>&1 >> $LOG
  apt -qy install libgcc-8-dev gcc-8-base 2>&1 >> $LOG

  # inspired by https://serverfault.com/questions/527789/how-to-automate-changed-config-files-during-apt-get-upgrade-in-ubuntu-12
  apt -qy -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" dist-upgrade 2>&1 >> $LOG

  apt -qyf install 2>&1 >> $LOG
  apt -qy autoremove 2>&1 >> $LOG

  # inspired by https://raspberrypi.stackexchange.com/questions/133376/fix-error-failed-to-start-dhcp-client-daemon-after-upgrade-to-bullseye-remot
  sed -i 's|/usr/lib/dhcpcd5/dhcpcd|/usr/sbin/dhcpcd|g' /etc/systemd/system/dhcpcd.service.d/wait.conf

  reboot
else
  echo "$PREFIX Update software catalog ..." >> $LOG
  apt -y update 2>&1 >> $LOG
fi

echo "$PREFIX Install required packages ..." >> $LOG
apt -qy install jq pigpiod mosquitto 2>&1 >> $LOG

echo "$PREFIX Setting up pigpiod ..." >> $LOG
sed -i "s/ExecStart=\/usr\/bin\/pigpiod -l/ExecStart=\/usr\/bin\/pigpiod/g" /lib/systemd/system/pigpiod.service 2>&1 >> $LOG
systemctl start pigpiod 2>&1 >> $LOG
systemctl enable pigpiod 2>&1 >> $LOG

MOSQUITTO_INSTALLED=`dpkg -l mosquitto 2>&1 | grep ii`
if [ -z "$MOSQUITTO_INSTALLED" ] ; then
  echo "$PREFIX Setting up mosquitto ..." >> $LOG
  sed -i "s/persistence true/persistence false/g" /etc/mosquitto/mosquitto.conf 2>&1 >> $LOG
  echo "listener 1883" > /etc/mosquitto/conf.d/smartapplianceenabler.conf
  echo "allow_anonymous true" >> /etc/mosquitto/conf.d/smartapplianceenabler.conf
  systemctl start mosquitto 2>&1 >> $LOG
  systemctl enable mosquitto 2>&1 >> $LOG
else
  echo "$PREFIX mosquitto already installed" >> $LOG
fi

echo "$PREFIX Stopping SAE ..." >> $LOG
systemctl stop smartapplianceenabler.service 2>&1 >> $LOG

echo "$PREFIX Configure SAE installation..." >> $LOG
SAE_DEFAULT_CONFIG=/etc/default/smartapplianceenabler
echo -e "\n" >> $SAE_DEFAULT_CONFIG
echo "# Configure pigpioj to use pigpiod daemon in order to avoid forcing the Smart Appliance Enabler to run as root" >> $SAE_DEFAULT_CONFIG
echo "JAVA_OPTS=\"\${JAVA_OPTS} -DPIGPIOD_HOST=localhost\"" >> $SAE_DEFAULT_CONFIG

echo "$PREFIX Back up existing SAE installation..." >> $LOG
cp $SAE_HOME/Appliances.xml $SAE_HOME/Appliances.xml.bak
cp $SAE_HOME/Device2EM.xml $SAE_HOME/Device2EM.xml.bak
find $SAE_HOME -type f -name '*.war' -execdir mv {} {}.bak ';'

echo "$PREFIX Updating SAE ..." >> $LOG
SAE_VERSION=`curl --no-progress-meter -H "Accept: application/vnd.github.v3+json" https://api.github.com/repos/camueller/SmartApplianceEnabler/releases | jq -c '.[] | select( .prerelease == false) | .tag_name' | head -n 1 | tr -d '"'`
wget "https://github.com/camueller/SmartApplianceEnabler/releases/download/"$SAE_VERSION"/SmartApplianceEnabler-"$SAE_VERSION".war" -P $SAE_HOME 2>>$LOG
chown -R sae:sae $SAE_HOME 2>&1 >> $LOG

echo "$PREFIX Starting SAE ..." >> $LOG
systemctl start smartapplianceenabler.service 2>&1 >> $LOG

WEBMIN_CHECK=`dpkg --list webmin | wc -l`
if [ $WEBMIN_CHECK -gt 0 ]; then
  echo "$PREFIX Updating Webmin ..." >> $LOG
  WEBMIN_VERSION=2.001
  wget "http://prdownloads.sourceforge.net/webadmin/webmin_"$WEBMIN_VERSION"_all.deb" -P /tmp 2>>$LOG
  dpkg -i "/tmp/webmin_"$WEBMIN_VERSION"_all.deb" 2>&1 >> $LOG
  systemctl restart webmin.service 2>&1 >> $LOG
fi

echo "$PREFIX Clean up installation files ..." >> $LOG
rm -v $PARENT_SCRIPT >> $LOG
mv -v $PARENT_SCRIPT_BACKUP $PARENT_SCRIPT  >> $LOG
rm -v  $CONFIG_FILE >> $LOG

echo "$PREFIX $0 finished" >> $LOG
echo 0 > $POWER_LED
sleep 3600
echo 255 > $POWER_LED