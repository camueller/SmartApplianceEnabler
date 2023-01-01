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

import {baseUrl} from './page/page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {generateApplianceId} from './shared/appliance-id-generator';
import {
  configurationKey,
  createAndAssertAppliance,
  createAndAssertControl,
  createAndAssertMeter,
  fixtureName,
  testSpeed
} from './shared/helper';
import {PwmSwitch} from '../../../main/angular/src/app/control/pwm/pwm-switch';
import {pwmSwitch} from './fixture/control/pwm-switch';
import {s0Meter} from './fixture/meter/s0-meter';
import {S0ElectricityMeter} from '../../../main/angular/src/app/meter/s0/s0-electricity-meter';
import {charger} from './fixture/appliance/charger';

fixture('Charger')
    .beforeEach(async t => {
      await t.maximizeWindow();
      await t.setTestSpeed(testSpeed);
    })
    .page(baseUrl());

function createCharger(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...charger, id: generateApplianceId()},
    meter: {
      type: S0ElectricityMeter.TYPE,
      s0ElectricityMeter: s0Meter,
    },
    control: {type: PwmSwitch.TYPE, startingCurrentSwitchUsed: false, pwmSwitch }  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createCharger());
});

test('Create S0 meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create PWM switch', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
