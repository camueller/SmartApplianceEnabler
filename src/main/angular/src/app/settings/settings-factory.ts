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
import {SettingsDefaults} from './settings-defaults';
import {Logger} from '../log/logger';
import {Info} from './info';
import {ModbusSetting} from './modbus/modbus-setting';

export class SettingsFactory {

  constructor(private logger: Logger) {
  }

  defaultsFromJSON(rawSettings: any): SettingsDefaults {
    this.logger.debug('SettingsDefaults: ' + JSON.stringify(rawSettings));
    return rawSettings;
  }

  fromJSON(rawSettings: any): Settings {
    this.logger.debug('Settings (JSON): ' + JSON.stringify(rawSettings));
    const settings = new Settings({...rawSettings});
    this.logger.debug('Settings (TYPE): ' + JSON.stringify(settings));
    return settings;
  }

  toJSON(settings: Settings): string {
    this.logger.debug('Settings (TYPE): ' + JSON.stringify(settings));
    const rawSettings = JSON.stringify(settings);
    this.logger.debug('Settings (JSON): ' + rawSettings);
    return rawSettings;
  }
}
