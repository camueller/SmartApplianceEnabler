import {baseUrl} from './page/page';
import {GlobalContext} from './shared/global-context';
import {assertAppliance, assertMeter, configurationKey, createAppliance, createMeter, fixtureName} from './shared/helper';
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
  // FIXME rausziehen ...
  const configuration = createPump();
  const key = configurationKey(t, fixtureName(t));
  t.fixtureCtx[key] = configuration;
  GlobalContext.ctx[key] = configuration;
  await createAppliance(t, configuration.appliance);
  await TopMenu.clickStatus(t);
  await assertAppliance(t, configuration.appliance);
});

test('Create Modbus meter', async t => {
  const configuration: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
  await createMeter(t, configuration.appliance.id, configuration.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, configuration.appliance.id, configuration.meter);
});

// test('Create GPIO switch', async t => {
//   const configuration: ApplianceConfiguration = t.fixtureCtx[configurationKey(t, fixtureName(t))];
//   await createControl(t, configuration.appliance.id, configuration.control);
//   await TopMenu.clickStatus(t);
//   await assertControl(t, configuration.appliance.id, configuration.control);
// });
