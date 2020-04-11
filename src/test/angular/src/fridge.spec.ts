import {baseUrl} from './page/page';
import {configurationKey, createAndAssertAppliance, createAndAssertControl, createAndAssertMeter, fixtureName} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {fridge as fridgeAppliance} from './fixture/appliance/fridge';
import {generateApplianceId} from './shared/appliance-id-generator';
import {HttpElectricityMeter} from '../../../main/angular/src/app/meter/http/http-electricity-meter';
import {httpMeter_2HttpRead_complete} from './fixture/meter/http-meter';
import {alwaysOnSwitch} from './fixture/control/always-on-switch';
import {AlwaysOnSwitch} from '../../../main/angular/src/app/control/alwayson/always-on-switch';

fixture('Fridge').page(baseUrl());

function createFridge(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...fridgeAppliance, id: generateApplianceId()},
    meter: {type: HttpElectricityMeter.TYPE, httpElectricityMeter: httpMeter_2HttpRead_complete},
    control: {type: AlwaysOnSwitch.TYPE, startingCurrentDetection: false, alwaysOnSwitch}
  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createFridge());
});

test('Create HTTP meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create always-on-switch', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
