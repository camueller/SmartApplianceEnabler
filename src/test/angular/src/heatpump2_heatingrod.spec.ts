import {baseUrl} from './page/page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {generateApplianceId} from './shared/appliance-id-generator';
import {SlaveElectricityMeter} from '../../../main/angular/src/app/meter/slave/master-electricity-meter';
import {
  configurationKey,
  createAndAssertAppliance,
  createAndAssertControl,
  createAndAssertMeter,
  fixtureName,
  testSpeed
} from './shared/helper';
import {heatingRod} from './fixture/appliance/heatingrod';
import {LevelSwitch} from '../../../main/angular/src/app/control/level/level-switch';
import {levelSwitch} from './fixture/control/level-switch';
import {s0Meter} from './fixture/meter/s0-meter';
import { S0ElectricityMeter } from '../../../main/angular/src/app/meter/s0/s0-electricity-meter';

fixture('Heating rod')
    .beforeEach(async t => {
      await t.maximizeWindow();
      await t.setTestSpeed(testSpeed);
    })
    .page(baseUrl());

function createHeatingRod(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...heatingRod, id: generateApplianceId()},
    meter: {
      type: S0ElectricityMeter.TYPE,
      s0ElectricityMeter: s0Meter,
    },
    control: {type: LevelSwitch.TYPE, startingCurrentSwitchUsed: false, levelSwitch }  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createHeatingRod());
});

test('Create slave meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create level switch', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
