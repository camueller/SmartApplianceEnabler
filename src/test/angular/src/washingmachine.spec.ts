import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {
  assertAppliance,
  assertControl,
  assertMeter,
  configurationKey,
  createAppliance,
  createControl,
  createMeter,
  fixtureName
} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter-http/http-electricity-meter';
import {washingMachine as washingMachineAppliance} from './fixture/appliance/washingmachine';
import {httpMeter_2HttpRead_complete} from './fixture/meter/http-meter';
import {generateApplianceId} from './shared/appliance-id-generator';
import {HttpSwitch} from '../../../main/angular/src/app/control-http/http-switch';
import {httpControl_2httpWrite_httpRead_complete} from './fixture/control/http-control';

fixture('Washing Machine').page(baseUrl());

function createWashingMachine(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...washingMachineAppliance, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete},
    control: {type: HttpSwitch.TYPE, startingCurrentDetection: false, httpSwitch: httpControl_2httpWrite_httpRead_complete}
  });
}

test('Create appliance', async t => {
  const washingMachine = createWashingMachine();
  const key = configurationKey(t, fixtureName(t));
  t.fixtureCtx[key] = washingMachine;
  GlobalContext.ctx[key] = washingMachine;
  await createAppliance(t, washingMachine.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, washingMachine.appliance);
});

test('Create HTTP meter', async t => {
  const washingMachine: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createMeter(t, washingMachine.appliance.id, washingMachine.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, washingMachine.appliance.id, washingMachine.meter);
});

test('Create HTTP switch', async t => {
  const washingMachine: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createControl(t, washingMachine.appliance.id, washingMachine.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, washingMachine.appliance.id, washingMachine.control);
});
