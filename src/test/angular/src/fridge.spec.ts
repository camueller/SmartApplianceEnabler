import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {
  assertAppliance,
  assertControl,
  assertMeter,
  createAppliance,
  createControl,
  createMeter,
  fixtureName,
  configurationKey
} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {fridge as fridgeAppliance} from './fixture/appliance/fridge';
import {generateApplianceId} from './shared/appliance-id-generator';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter-http/http-electricity-meter';
import {httpMeter_2HttpRead_complete} from './fixture/meter/http-meter';
import {AlwaysOnSwitch} from '../../../main/angular/src/app/control-alwayson/always-on-switch';
import {alwaysOnSwitch} from './fixture/control/always-on-switch';

fixture('Fridge').page(baseUrl());

function createFridge(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...fridgeAppliance, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete},
    control: {type: AlwaysOnSwitch.TYPE, startingCurrentDetection: false, alwaysOnSwitch}
  });
}

test('Create appliance', async t => {
  const fridge = createFridge();
  const key = configurationKey(t, fixtureName(t));
  t.fixtureCtx[key] = fridge;
  GlobalContext.ctx[key] = fridge;
  await createAppliance(t, fridge.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, fridge.appliance);
});

test('Create HTTP meter', async t => {
  const fridge: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createMeter(t, fridge.appliance.id, fridge.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, fridge.appliance.id, fridge.meter);
});

test('Create always-on-switch', async t => {
  const fridge: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createControl(t, fridge.appliance.id, fridge.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, fridge.appliance.id, fridge.control);
});
