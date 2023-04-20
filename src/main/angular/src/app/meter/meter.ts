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

import {S0ElectricityMeter} from './s0/s0-electricity-meter';
import {ModbusElectricityMeter} from './modbus/modbus-electricity-meter';
import {HttpElectricityMeter} from './http/http-electricity-meter';
import {Notifications} from '../notification/notifications';
import {MasterElectricityMeter} from './master/master-electricity-meter';
import {SlaveElectricityMeter} from './slave/master-electricity-meter';
import {MqttElectricityMeter} from './mqtt/mqtt-electricity-meter';

export function simpleMeterType(meterType: string) {
  return meterType && meterType.split('.')[4];
}

export class Meter {
  type: string;
  isMasterMeter?: boolean;
  masterElectricityMeter?: MasterElectricityMeter;
  slaveElectricityMeter?: SlaveElectricityMeter;
  s0ElectricityMeter?: S0ElectricityMeter;
  modbusElectricityMeter?: ModbusElectricityMeter;
  mqttElectricityMeter?: MqttElectricityMeter;
  httpElectricityMeter?: HttpElectricityMeter;
  notifications?: Notifications;

  public constructor(init?: Partial<Meter>) {
    Object.assign(this, init);
  }
}
