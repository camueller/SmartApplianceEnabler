import {baseUrl} from './page/page';
import {configurationKey, createAndAssertAppliance, createAndAssertControl, createAndAssertMeter, fixtureName} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {pump as pumpAppliance} from './fixture/appliance/pump';
import {modbusMeter_1ModbusRead_complete} from './fixture/meter/modbus-meter';
import {generateApplianceId} from './shared/appliance-id-generator';
import {ModbusElectricityMeter} from '../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {modbusSwitch_2modbusWrite_complete} from './fixture/control/modbus-control';
import {ModbusSwitch} from '../../../main/angular/src/app/control/modbus/modbus-switch';

fixture('Pump').page(baseUrl());

function createPump(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...pumpAppliance, id: generateApplianceId()},
    meter: {type: ModbusElectricityMeter.TYPE, modbusElectricityMeter: modbusMeter_1ModbusRead_complete},
    control: {type: ModbusSwitch.TYPE, startingCurrentDetection: false, modbusSwitch: modbusSwitch_2modbusWrite_complete}
  });
}

test('Create appliance with interruptions allowed without timing specification', async t => {
  await createAndAssertAppliance(t, createPump());
});

test('Create Modbus meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create Modbus control', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
