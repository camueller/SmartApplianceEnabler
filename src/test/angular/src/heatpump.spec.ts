import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {
  assertAppliance,
  assertControl,
  assertMeter,
  createAppliance,
  createControl,
  createMeter, fixtureName,
  configurationKey
} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
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
  const heatPump = createHeatPump();
  const key = configurationKey(t, fixtureName(t));
  t.fixtureCtx[key] = heatPump;
  GlobalContext.ctx[key] = heatPump;
  await createAppliance(t, heatPump.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, heatPump.appliance);
});

test('Create S0 meter', async t => {
  const heatPump: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createMeter(t, heatPump.appliance.id, heatPump.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, heatPump.appliance.id, heatPump.meter);
});

test('Create GPIO switch', async t => {
  const heatPump: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createControl(t, heatPump.appliance.id, heatPump.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, heatPump.appliance.id, heatPump.control);
});
