import {baseUrl} from './page/page';
import {configurationKey, createAndAssertAppliance, createAndAssertControl, createAndAssertMeter, fixtureName} from './shared/helper';
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
  await createAndAssertAppliance(t, createWashingMachine());
});

test('Create HTTP meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create HTTP switch', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
