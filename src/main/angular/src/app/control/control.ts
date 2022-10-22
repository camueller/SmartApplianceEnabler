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

import {MockSwitch} from './mock-switch';
import {HttpSwitch} from './http/http-switch';
import {StartingCurrentSwitch} from './startingcurrent/starting-current-switch';
import {AlwaysOnSwitch} from './alwayson/always-on-switch';
import {ModbusSwitch} from './modbus/modbus-switch';
import {Switch} from './switch/switch';
import {EvCharger} from './evcharger/ev-charger';
import {Notifications} from '../notification/notifications';
import {MeterReportingSwitch} from './meterreporting/meter-reporting-switch';
import {PwmSwitch} from './pwm/pwm-switch';
import {LevelSwitch} from './level/level-switch';
import {SwitchOption} from './switchoption/switch-option';

export class Control {
  type: string;
  startingCurrentSwitchUsed?: boolean;
  startingCurrentSwitch?: StartingCurrentSwitch;
  switchOptionUsed?: boolean;
  switchOption?: SwitchOption;
  meterReportingSwitch?: MeterReportingSwitch;
  alwaysOnSwitch?: AlwaysOnSwitch;
  switch_?: Switch;
  modbusSwitch?: ModbusSwitch;
  mockSwitch?: MockSwitch;
  httpSwitch?: HttpSwitch;
  levelSwitch?: LevelSwitch;
  pwmSwitch?: PwmSwitch;
  evCharger?: EvCharger;
  notifications?: Notifications;

  public constructor(init?: Partial<Control>) {
    Object.assign(this, init);
  }
}
