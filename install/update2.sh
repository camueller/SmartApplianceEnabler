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

CONFIG_FILE=/usr/local/etc/update.config
. $CONFIG_FILE

echo "$PREFIX Updating SAE ..." >> $LOG
if [ "$BETA_VERSIONS" = true ] ; then
  SAE_VERSION=`curl -H "Accept: application/vnd.github.v3+json" https://api.github.com/repos/camueller/SmartApplianceEnabler/releases | jq -c '.[] | .tag_name' | head -n 1 | tr -d '"'`
else
  SAE_VERSION=`curl -H "Accept: application/vnd.github.v3+json" https://api.github.com/repos/camueller/SmartApplianceEnabler/releases | jq -c '.[] | select( .prerelease == false) | .tag_name' | head -n 1 | tr -d '"'`
fi

for FILENAME in $SAE_HOME/*.war; do mv $FILENAME $FILENAME.bak; done
wget "https://github.com/camueller/SmartApplianceEnabler/releases/download/"$SAE_VERSION"/SmartApplianceEnabler-"$SAE_VERSION".war" -P $SAE_HOME 2>>$LOG

chown -R sae:sae $SAE_HOME 2>&1 >> $LOG

echo "$PREFIX Stopping SAE ..." >> $LOG
systemctl stop smartapplianceenabler.service
echo "$PREFIX Starting SAE ..." >> $LOG
systemctl start smartapplianceenabler.service

echo "$PREFIX Clean up installation files ..." >> $LOG
rm -v $PARENT_SCRIPT >> $LOG
mv -v $PARENT_SCRIPT_BACKUP $PARENT_SCRIPT  >> $LOG
rm -v  $CONFIG_FILE >> $LOG

echo "$PREFIX $0 finished" >> $LOG
echo 0 > $POWER_LED
