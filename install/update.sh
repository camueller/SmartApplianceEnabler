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
CONFIG_FILE=$MOUNT_POINT/update.config

. $CONFIG_FILE

echo "$PREFIX Copy installation files ..." >> $LOG
cp $MOUNT_POINT/$SCRIPT2 $INSTALL_DIR/
chmod +x $INSTALL_DIR/$SCRIPT2
cp $CONFIG_FILE /usr/local/etc

echo "$PREFIX Add install script to rc.local ..." >> $LOG
mv $PARENT_SCRIPT $PARENT_SCRIPT_BACKUP
echo "#!/bin/sh -e" > $PARENT_SCRIPT
echo "$INSTALL_DIR/$SCRIPT2" >> $PARENT_SCRIPT
echo "exit 0" >> $PARENT_SCRIPT
chmod +x $PARENT_SCRIPT

echo "$PREFIX $0 finished" >> $LOG