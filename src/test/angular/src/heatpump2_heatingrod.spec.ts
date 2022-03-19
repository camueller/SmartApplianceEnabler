import {baseUrl} from './page/page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {generateApplianceId} from './shared/appliance-id-generator';
import {SlaveElectricityMeter} from '../../../main/angular/src/app/meter/slave/master-electricity-meter';
import {configurationKey, createAndAssertAppliance, createAndAssertMeter, fixtureName} from './shared/helper';
import {heatingRod} from './fixture/appliance/heatingrod';

fixture('Heating rod').page(baseUrl());

function createHeatingRod(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...heatingRod, id: generateApplianceId()},
    meter: {
      type: SlaveElectricityMeter.TYPE,
      slaveElectricityMeter: new SlaveElectricityMeter({masterElectricityMeterApplianceId: 'F-00000001-000000000001-00'}),
    },
  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createHeatingRod());
});

test('Create slave meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

// test('Create GPIO switch', async t => {
//   await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
// });
