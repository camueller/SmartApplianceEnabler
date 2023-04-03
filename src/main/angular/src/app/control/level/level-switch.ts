/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import {Switch} from '../switch/switch';
import {ModbusSwitch} from '../modbus/modbus-switch';
import {HttpSwitch} from '../http/http-switch';
import {PowerLevel} from './power-level';
import {MqttSwitch} from '../mqtt/mqtt-switch';

export class LevelSwitch {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.LevelSwitch';
  }
  '@class' = LevelSwitch.TYPE;
  controls?: (Switch | HttpSwitch | ModbusSwitch | MqttSwitch)[];
  powerLevels?: PowerLevel[];

  public constructor(init?: Partial<LevelSwitch>) {
    Object.assign(this, init);
  }
}
