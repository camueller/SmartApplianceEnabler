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

MOUNT_POINT=/media

. $MOUNT_POINT/install.config

echo "$PREFIX Copy installation files ..." >> $LOG
cp $MOUNT_POINT/install2.sh $INSTALL_DIR/
chmod +x $INSTALL_DIR/install2.sh
cp $MOUNT_POINT/install.config /usr/local/etc

echo "$PREFIX Add install script to rc.local ..." >> $LOG
mv $PARENT_SCRIPT $PARENT_SCRIPT_BACKUP
echo "#!/bin/sh -e" > $PARENT_SCRIPT
echo "$INSTALL_DIR/install2.sh" >> $PARENT_SCRIPT
echo "exit 0" >> $PARENT_SCRIPT
chmod +x $PARENT_SCRIPT

if [ -n "$WIFI_SSID" ] ; then
  echo "$PREFIX Setting up wi-fi ..." >> $LOG
  cp /etc/wpa_supplicant/wpa_supplicant.conf /boot
  echo "country=$WIFI_COUNTRY" >> /boot/wpa_supplicant.conf
  echo "network={" >> /boot/wpa_supplicant.conf
  echo "  ssid=\"$WIFI_SSID\"" >> /boot/wpa_supplicant.conf
  echo "  psk=\"$WIFI_PSK\"" >> /boot/wpa_supplicant.conf
  echo "}" >> /boot/wpa_supplicant.conf
fi

echo "$PREFIX $0 finished" >> $LOG