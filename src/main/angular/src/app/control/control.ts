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

import {StartingCurrentSwitch} from '../control-startingcurrent/starting-current-switch';
import {Switch} from '../control-switch/switch';
import {ModbusSwitch} from '../control-modbus/modbus-switch';
import {HttpSwitch} from '../control-http/http-switch';
import {AlwaysOnSwitch} from './always-on-switch';
import {MockSwitch} from './mock-switch';
import {EvCharger} from '../control-evcharger/ev-charger';

export class Control {
  type: string;
  startingCurrentDetection: boolean;
  startingCurrentSwitch: StartingCurrentSwitch;
  alwaysOnSwitch: AlwaysOnSwitch;
  switch_: Switch;
  modbusSwitch: ModbusSwitch;
  mockSwitch: MockSwitch;
  httpSwitch: HttpSwitch;
  evCharger: EvCharger;

  public constructor(init?: Partial<Control>) {
    Object.assign(this, init);
  }
}
