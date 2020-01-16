import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {
  assertAppliance, assertControl,
  assertMeter,
  configurationKey,
  createAndAssertAppliance, createAndAssertControl, createAndAssertMeter,
  createAppliance, createControl,
  createMeter,
  fixtureName
} from './shared/helper';
import {TopMenu} from './page/top-menu.page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {switch_} from './fixture/control/switch';
import {Switch} from '../../../main/angular/src/app/control-switch/switch';
import {pump as pumpAppliance} from './fixture/appliance/pump';
import {modbusMeter_complete} from './fixture/meter/modbus-meter';
import {generateApplianceId} from './shared/appliance-id-generator';
import {ModbusElectricityMeter} from '../../../main/angular/src/app/meter-modbus/modbus-electricity-meter';

fixture('Pump').page(baseUrl());

function createPump(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...pumpAppliance, id: generateApplianceId()},
    meter: {type: ModbusElectricityMeter.TYPE, modbusElectricityMeter: modbusMeter_complete},
    control: {type: Switch.TYPE, startingCurrentDetection: false, switch_}
  });
}

test('Create appliance', async t => {
  await createAndAssertAppliance(t, createPump());
});

test('Create Modbus meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

// test('Create GPIO switch', async t => {
//   await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
// });
