import {baseUrl} from './page/page';
import {configurationKey, createAndAssertAppliance, createAndAssertControl, createAndAssertMeter, fixtureName} from './shared/helper';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {pump as pumpAppliance} from './fixture/appliance/pump';
import {modbusMeter_pollInterval} from './fixture/meter/modbus-meter';
import {generateApplianceId} from './shared/appliance-id-generator';
import {ModbusElectricityMeter} from '../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {modbusSwitch_2modbusWrite_complete} from './fixture/control/modbus-control';
import {ModbusSwitch} from '../../../main/angular/src/app/control/modbus/modbus-switch';
import {Notifications} from '../../../main/angular/src/app/notification/notifications';
import {NotificationType} from '../../../main/angular/src/app/notification/notification-type';

fixture('Pump').page(baseUrl());

function createPump(): ApplianceConfiguration {
  return new ApplianceConfiguration({
    appliance: {...pumpAppliance, id: generateApplianceId(), notificationSenderId: 'myNotificationId'},
    meter: {type: ModbusElectricityMeter.TYPE, modbusElectricityMeter: modbusMeter_pollInterval,
      notifications: new Notifications()},
    control: {type: ModbusSwitch.TYPE, startingCurrentSwitchUsed: false, modbusSwitch: modbusSwitch_2modbusWrite_complete,
      notifications: new Notifications({types: [NotificationType.CONTROL_ON]})},
  });
}

test('Create appliance with interruptions allowed without timing specification', async t => {
  await createAndAssertAppliance(t, createPump());
});

test('Create Modbus meter with specific poll interval and all notifications enabled', async t => {
  await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create Modbus control with selected notifications enabled', async t => {
  await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
