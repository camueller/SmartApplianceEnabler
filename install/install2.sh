#!/bin/sh
#
# Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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

if [ "$INSTALL_WEBMIN" = true ] ; then
  PACKAGES="$PACKAGES perl libnet-ssleay-perl openssl libauthen-pam-perl libpam-runtime libio-pty-perl apt-show-versions python mon"
fi
if [ -n "$WIFI_SSID" ] ; then
  IP_ADDRESS=`ip addr | grep wlan0 | grep inet | awk '{print $2}' | awk -F '/' '{print $1}'`
  echo "IP_ADDRESS=$IP_ADDRESS" >> $LOG
fi

echo "$PREFIX Update software catalog ..." >> $LOG
apt update 2>&1 >> $LOG

echo "$PREFIX Upgrading system ..." >> $LOG
apt upgrade -y 2>&1 >> $LOG

echo "Allow shared mounts by udevd (possibly reset by system upgrade) ..."
# Refer to https://raspberrypi.stackexchange.com/questions/100312/raspberry-4-usbmount-not-working/100375#100375
sed -i 's/PrivateMounts\=yes/PrivateMounts\=no/g' /lib/systemd/system/systemd-udevd.service

echo "$PREFIX Setting time zone ..." >> $LOG
echo $TIMEZONE > /etc/timezone
echo /etc/timezone >> $LOG

echo "$PREFIX Copy zoneinfo ..." >> $LOG
cp /usr/share/zoneinfo/$TIMEZONE /etc/localtime 2>&1 >> $LOG

echo "$PREFIX Install required packages ..." >> $LOG
apt install $PACKAGES -y 2>&1 >> $LOG

echo "$PREFIX Setting up user ..." >> $LOG
mkdir $SAE_HOME 2>&1 >> $LOG
groupadd sae 2>&1 >> $LOG
useradd -d $SAE_HOME -c "SmartApplianceEnabler" -g sae -M sae -s /bin/bash 2>&1 >> $LOG
usermod -a -G gpio,sudo sae 2>&1 >> $LOG
(echo $PASSWORD; echo $PASSWORD) | passwd sae 2>&1 >> $LOG
cp /home/pi/.profile $SAE_HOME 2>&1 >> $LOG
cp /home/pi/.bashrc $SAE_HOME 2>&1 >> $LOG

echo "$PREFIX Installing SAE ..." >> $LOG
wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/smartapplianceenabler -P $SAE_HOME 2>>$LOG
chmod 755 $SAE_HOME/smartapplianceenabler 2>&1 >> $LOG

wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/default/smartapplianceenabler -P /etc/default 2>>$LOG
chown root.root /etc/default/smartapplianceenabler 2>&1 >> $LOG
chmod 644 /etc/default/smartapplianceenabler 2>&1 >> $LOG
if [ -n "$IP_ADDRESS" ] ; then
  sed -i "s/#JAVA_OPTS=...JAVA_OPTS. -Dserver.address=.*$/JAVA_OPTS=\"\${JAVA_OPTS} -Dserver.address=$IP_ADDRESS\"/g" /etc/default/smartapplianceenabler
fi

wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/logback-spring.xml -P $SAE_HOME 2>>$LOG
chmod 644 $SAE_HOME/logback-spring.xml 2>&1 >> $LOG

SAE_VERSION=`curl -H "Accept: application/vnd.github.v3+json" https://api.github.com/repos/camueller/SmartApplianceEnabler/releases | jq -c '.[] | select( .prerelease == false) | .tag_name' | head -n 1 | tr -d '"'`
wget "https://github.com/camueller/SmartApplianceEnabler/releases/download/"$SAE_VERSION"/SmartApplianceEnabler-"$SAE_VERSION".war" -P $SAE_HOME 2>>$LOG

chown -R sae:sae $SAE_HOME 2>&1 >> $LOG

echo "$PREFIX Configure systemd ..." >> $LOG
wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/lib/systemd/system/smartapplianceenabler.service -P /lib/systemd/system 2>>$LOG
chown root.root /lib/systemd/system/smartapplianceenabler.service 2>&1 >> $LOG
chmod 755 /lib/systemd/system/smartapplianceenabler.service 2>&1 >> $LOG

systemctl enable smartapplianceenabler.service 2>&1 >> $LOG
systemctl daemon-reload 2>&1 >> $LOG

echo "$PREFIX Starting SAE ..." >> $LOG
systemctl start smartapplianceenabler.service 2>&1 >> $LOG

if [ "$INSTALL_WEBMIN" = true ] ; then
  echo "$PREFIX Installing Webmin ..." >> $LOG
  wget "http://prdownloads.sourceforge.net/webadmin/webmin_"$WEBMIN_VERSION"_all.deb" -P /tmp 2>>$LOG
  dpkg -i "/tmp/webmin_"$WEBMIN_VERSION"_all.deb" 2>&1 >> $LOG
fi

echo "$PREFIX Clean up installation files ..." >> $LOG
rm -v $PARENT_SCRIPT >> $LOG
mv -v $PARENT_SCRIPT_BACKUP $PARENT_SCRIPT  >> $LOG
rm -v  $CONFIG_FILE >> $LOG

echo "$PREFIX $0 finished" >> $LOG
echo 0 > $POWER_LED
sleep 3600
echo 255 > $POWER_LED