import {baseUrl} from './page/page';
import {configurationKey, createAndAssertAppliance, createAndAssertControl, createAndAssertMeter, fixtureName} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {modbusMeter_complete} from './fixture/meter/modbus-meter';
import {generateApplianceId} from './shared/appliance-id-generator';
import {ModbusElectricityMeter} from '../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {evchargerPhoenixContact} from './fixture/appliance/evcharger-phoenixcontact';
import {EvCharger} from '../../../main/angular/src/app/control/evcharger/ev-charger';
import {EvChargerTemplates} from '../../../main/angular/src/app/control/evcharger/ev-charger-templates';
import {settings} from './fixture/settings/settings';
import {tesla_model3} from './fixture/control/electricvehicle/tesla_model3';

fixture('Wallbox mit PhoenixContact-Ladecontroller').page(baseUrl());

function createApplianceConfiguration(): ApplianceConfiguration {
  const configuration =  new ApplianceConfiguration({
    appliance: {...evchargerPhoenixContact, id: generateApplianceId()},
    meter: {type: ModbusElectricityMeter.TYPE, modbusElectricityMeter: modbusMeter_complete},
    control: {type: EvCharger.TYPE, evCharger: new EvCharger(EvChargerTemplates.getTemplates()['Phoenix Contact EM-CP-PP-ETH'])},
    controlTemplate: 'Phoenix Contact EM-CP-PP-ETH'
  });
  configuration.control.evCharger.modbusControl.idref = settings.modbusSettings[0].modbusTcpId;
  configuration.control.evCharger.vehicles = [
    tesla_model3,
  ];
  return configuration;
}

test('Create appliance with interruptions allowed without timing specification', async t => {
  await createAndAssertAppliance(t, createApplianceConfiguration());
});

test('Create Modbus meter', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create Modbus control', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
