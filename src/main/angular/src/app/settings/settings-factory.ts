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
import {ModbusSettings} from './modbus-settings';

export class SettingsFactory {

  constructor(private logger: Logger) {
  }

  createEmptySettingsDefaults(): SettingsDefaults {
    return new SettingsDefaults();
  }

  defaultsFromJSON(rawSettings: any): SettingsDefaults {
    this.logger.debug('SettingsDefaults (JSON): ' + JSON.stringify(rawSettings));
    const settings = new SettingsDefaults();
    settings.holidaysUrl = rawSettings.holidaysUrl;
    settings.modbusTcpHost = rawSettings.modbusTcpHost;
    settings.modbusTcpPort = Number.parseInt(rawSettings.modbusTcpPort);
    settings.modbusReadRegisterTypes = rawSettings.modbusReadRegisterTypes;
    settings.modbusWriteRegisterTypes = rawSettings.modbusWriteRegisterTypes;
    settings.pulseReceiverPort = Number.parseInt(rawSettings.pulseReceiverPort);
    this.logger.debug('SettingsDefaults (TYPE): ' + JSON.stringify(settings));
    return settings;
  }

  createEmptySettings(): Settings {
    return new Settings();
  }

  fromJSON(rawSettings: any): Settings {
    this.logger.debug('Settings (JSON): ' + JSON.stringify(rawSettings));
    const settings = new Settings();
    settings.holidaysEnabled = rawSettings.holidaysEnabled;
    settings.holidaysUrl = rawSettings.holidaysUrl;

    settings.modbusSettings = [] as ModbusSettings[];
    if (rawSettings.modbusSettings) {
      (rawSettings.modbusSettings as any[]).forEach((rawModbusSettings) => {
        const modbusSettings = new ModbusSettings();
        modbusSettings.modbusTcpId = rawModbusSettings.modbusTcpId;
        modbusSettings.modbusTcpHost = rawModbusSettings.modbusTcpHost;
        modbusSettings.modbusTcpPort = rawModbusSettings.modbusTcpPort;
        settings.modbusSettings.push(modbusSettings);
      });
    }

    settings.pulseReceiverEnabled = rawSettings.pulseReceiverEnabled;
    settings.pulseReceiverPort = rawSettings.pulseReceiverPort;

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
