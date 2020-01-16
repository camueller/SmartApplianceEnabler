import {baseUrl} from './page/page';
import {configurationKey, createAndAssertAppliance, createAndAssertControl, createAndAssertMeter, fixtureName} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {S0ElectricityMeter} from '../../../main/angular/src/app/meter-s0/s0-electricity-meter';
import {switch_} from './fixture/control/switch';
import {Switch} from '../../../main/angular/src/app/control-switch/switch';
import {heatPump as heatPumpAppliance} from './fixture/appliance/heatpump';
import {s0Meter} from './fixture/meter/s0-meter';
import {generateApplianceId} from './shared/appliance-id-generator';

fixture('Heat pump').page(baseUrl());

function createHeatPump(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...heatPumpAppliance, id: generateApplianceId()},
    meter: {type: S0ElectricityMeter.TYPE, s0ElectricityMeter: s0Meter},
    control: {type: Switch.TYPE, startingCurrentDetection: false, switch_}
  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createHeatPump());
});

test('Create S0 meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create GPIO switch', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
