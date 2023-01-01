import {baseUrl} from './page/page';
import {
  configurationKey,
  createAndAssertAppliance,
  createAndAssertControl,
  createAndAssertMeter,
  fixtureName,
  testSpeed
} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {S0ElectricityMeter} from '../../../main/angular/src/app/meter/s0/s0-electricity-meter';
import {switch_} from './fixture/control/switch';
import {heatPump as heatPumpAppliance} from './fixture/appliance/heatpump';
import {s0Meter} from './fixture/meter/s0-meter';
import {Switch} from '../../../main/angular/src/app/control/switch/switch';
import {MasterElectricityMeter} from '../../../main/angular/src/app/meter/master/master-electricity-meter';

fixture('Heat pump')
    .beforeEach(async t => {
      await t.maximizeWindow();
      await t.setTestSpeed(testSpeed);
    })
    .page(baseUrl());

function createHeatPump(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...heatPumpAppliance, id: 'F-00000001-000000000001-00'},
    meter: {
      type: S0ElectricityMeter.TYPE,
      s0ElectricityMeter: s0Meter,
      isMasterMeter: true,
      masterElectricityMeter: new MasterElectricityMeter({slaveSwitchOn: true})
    },
    control: {type: Switch.TYPE, startingCurrentSwitchUsed: false, switch_}
  });
}

test('Create appliance with interruptions allowed and min/max on/off timings', async t => {
  await createAndAssertAppliance(t, createHeatPump());
});

test('Create S0 master meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create GPIO switch', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
