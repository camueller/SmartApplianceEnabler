#!/bin/sh
#
# Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

# für Telegram muss die senderId auf den von BotFather für den Bot zugewiesener Token gesetzt sein
senderId=$1
# Name des Gerätes in Device2EM.xml
deviceName=$2
# Typ des Gerätes in Device2EM.xml
deviceType=$3
# Hersteller des Gerätes in Device2EM.xml
deviceVendor=$4
# Seriennummer des Gerätes in Device2EM.xml
deviceSerial=$5
# vom @getidsbot ermittelte ID des Chats (bzw. der Gruppe) in den die Benachrichtigungen gepostet werden sollen
chat=
# Benachrichtigungs-Key des Ereignisses (z.B. CONTROL_OFF)
key=$6
# Benachrichtigungs-Text des Ereignisses (z.B. "Das Gerät wurde ausgeschaltet")
text=$7

curl -d chat_id=$chat -d text="$text" https://api.telegram.org/bot$senderId/sendMessage