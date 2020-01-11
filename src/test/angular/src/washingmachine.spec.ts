import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {assertAppliance, assertMeter, createAppliance, createMeter, fixtureName, configurationKey} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {switch_} from './fixture/control/switch';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter-http/http-electricity-meter';
import {Switch} from '../../../main/angular/src/app/control-switch/switch';
import {washingMachine as washingMachineAppliance} from './fixture/appliance/washingmachine';
import {httpMeter_2HttpRead_complete} from './fixture/meter/http-meter';
import {generateApplianceId} from './shared/appliance-id-generator';

fixture('Washing Machine').page(baseUrl());

function createWashingMachine(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...washingMachineAppliance, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete},
    control: {type: Switch.TYPE, startingCurrentDetection: false, switch_}
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
