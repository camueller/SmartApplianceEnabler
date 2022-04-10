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

import {LevelSwitch} from '../../../../../main/angular/src/app/control/level/level-switch';
import { PowerLevel } from '../../../../../main/angular/src/app/control/level/power-level';
import {Switch} from '../../../../../main/angular/src/app/control/switch/switch';
import {SwitchStatus} from '../../../../../main/angular/src/app/control/level/switch-status';

export const levelSwitch = new LevelSwitch({
  controls: [
    new Switch({ gpio: 17 }),
    new Switch({ gpio: 18 }),
  ],
  powerLevels: [
    new PowerLevel({power: 500, switchStatuses: [new SwitchStatus({on: true}), new SwitchStatus({on: false})]}),
    new PowerLevel({power: 1000, switchStatuses: [new SwitchStatus({on: false}), new SwitchStatus({on: true})]}),
    new PowerLevel({power: 1500, switchStatuses: [new SwitchStatus({on: true}), new SwitchStatus({on: true})]}),
  ],
})
