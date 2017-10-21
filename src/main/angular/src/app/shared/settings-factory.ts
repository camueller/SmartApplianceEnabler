/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import {Settings} from './settings';

export class SettingsFactory {

  static createEmptySettings(): Settings {
    return new Settings();
  }

  static fromJSON(rawSettings: any): Settings {
    console.log('Settings (JSON): ' + JSON.stringify(rawSettings));
    const settings = new Settings();
    settings.holidaysEnabled = rawSettings.holidaysEnabled;
    settings.defaultHolidaysUrl = rawSettings.defaultHolidaysUrl;
    settings.holidaysUrl = rawSettings.holidaysUrl;

    settings.modbusEnabled = rawSettings.modbusEnabled;
    settings.defaultModbusTcpHost = rawSettings.defaultModbusTcpHost;
    settings.modbusTcpHost = rawSettings.modbusTcpHost;
    settings.defaultModbusTcpPort = rawSettings.defaultModbusTcpPort;
    settings.modbusTcpPort = rawSettings.modbusTcpPort;

    settings.pulseReceiverEnabled = rawSettings.pulseReceiverEnabled;
    settings.defaultPulseReceiverPort = rawSettings.defaultPulseReceiverPort;
    settings.pulseReceiverPort = rawSettings.pulseReceiverPort;

    console.log('Settings (TYPE): ' + JSON.stringify(settings));
    return settings;
  }

  static toJSON(settings: Settings): string {
    console.log('Settings (TYPE): ' + JSON.stringify(settings));
    const rawSettings = JSON.stringify(settings);
    console.log('Settings (JSON): ' + rawSettings);
    return rawSettings;
  }
}
