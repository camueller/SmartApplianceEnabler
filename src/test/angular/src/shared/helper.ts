import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {SideMenu} from '../page/side.menu.page';
import {AppliancePage} from '../page/appliance/appliance.page';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
import {MqttElectricityMeter} from '../../../../main/angular/src/app/meter/mqtt/mqtt-electricity-meter';
import {S0MeterPage} from '../page/meter/s0-meter.page';
import {S0ElectricityMeter} from '../../../../main/angular/src/app/meter/s0/s0-electricity-meter';
import {SwitchPage} from '../page/control/switch.page';
import {Control} from '../../../../main/angular/src/app/control/control';
import {HttpElectricityMeter} from '../../../../main/angular/src/app/meter/http/http-electricity-meter';
import {HttpMeterPage} from '../page/meter/http-meter.page';
import {AlwaysOnSwitchPage} from '../page/control/always-on-switch.page';
import {HttpControlPage} from '../page/control/http-control.page';
import {ModbusElectricityMeter} from '../../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {ModbusMeterPage} from '../page/meter/modbus-meter.page';
import {GlobalContext} from './global-context';
import {ApplianceConfiguration} from './appliance-configuration';
import {ModbusControlPage} from '../page/control/modbus-control.page';
import {HttpSwitch} from '../../../../main/angular/src/app/control/http/http-switch';
import {AlwaysOnSwitch} from '../../../../main/angular/src/app/control/alwayson/always-on-switch';
import {ModbusSwitch} from '../../../../main/angular/src/app/control/modbus/modbus-switch';
import {MqttSwitch} from '../../../../main/angular/src/app/control/mqtt/mqtt-switch';
import {Switch} from '../../../../main/angular/src/app/control/switch/switch';
import {ControlPage} from '../page/control/control.page';
import {StartingCurrentSwitchPage} from '../page/control/starting-current-switch.page';
import {EvCharger} from '../../../../main/angular/src/app/control/evcharger/ev-charger';
import {EvchargerPage} from '../page/control/evcharger.page';
import {MeterPage} from '../page/meter/meter.page';
import {Selector} from 'testcafe';
import {ElectricVehicle} from '../../../../main/angular/src/app/control/evcharger/electric-vehicle/electric-vehicle';
import {Schedule} from '../../../../main/angular/src/app/schedule/schedule';
import {SchedulesPage} from '../page/schedule/schedules.page';
import {NotificationPage} from '../page/notification/notification.page';
import {MasterMeterPage} from '../page/meter/master-meter.page';
import {SlaveElectricityMeter} from '../../../../main/angular/src/app/meter/slave/master-electricity-meter';
import {SlaveMeterPage} from '../page/meter/slave-meter.page';
import {PwmSwitch} from '../../../../main/angular/src/app/control/pwm/pwm-switch';
import {PwmControlPage} from '../page/control/pwm-control.page';
import {LevelSwitch} from '../../../../main/angular/src/app/control/level/level-switch';
import {LevelControlPage} from '../page/control/level-control.page';
import {MqttMeterPage} from '../page/meter/mqtt-meter.page';
import {MqttControlPage} from '../page/control/mqtt-control.page';

// Specifies the test speed. Must be a number between 1 (the fastest) and 0.01 (the slowest).
export const testSpeed = 1;

export function isDebug() {
  return !!process.env.DEBUG;
}

export function fixtureName(t: TestController) {
  // @ts-ignore
  return t.testRun.test.fixture.name;
}

export function configurationKey(t: TestController, configurationName: string) {
  return JSON.stringify({configurationName, userAgent: t.browser.name});
}

export async function createAndAssertAppliance(t: TestController, configuration: ApplianceConfiguration) {
  const key = configurationKey(t, fixtureName(t));
  t.fixtureCtx[key] = configuration;
  GlobalContext.ctx[key] = configuration;
  await createAppliance(t, configuration.appliance);
  await SideMenu.clickStatus(t);
  await assertAppliance(t, configuration.appliance);
}

export async function createAndAssertMeter(t: TestController, configuration: ApplianceConfiguration) {
  await createMeter(t, configuration.appliance.id, configuration.meter);
  await SideMenu.clickStatus(t);
  await assertMeter(t, configuration.appliance.id, configuration.meter);
}

export async function createAndAssertControl(t: TestController, configuration: ApplianceConfiguration) {
  await createControl(t, configuration.appliance.id, configuration.control, configuration.controlTemplate);
  await SideMenu.clickStatus(t);
  await assertControl(t, configuration.appliance.id, configuration.control);
}

export async function createAndAssertElectricVehicle(t: TestController, applianceId: string, ev: ElectricVehicle, index: number,
                                                     clickControl: boolean, clickAdd: boolean, clickSave: boolean) {
  await EvchargerPage.setElectricVehicle(t, applianceId, ev, index, clickControl, clickAdd, clickSave);
  await SideMenu.clickStatus(t);
  await EvchargerPage.assertElectricVehicle(t, applianceId, ev, index, true);
}

export async function createAndAssertSchedules(t: TestController, configuration: ApplianceConfiguration, evName?: string) {
  await createSchedules(t, configuration.appliance.id, configuration.schedules);
  await SideMenu.clickStatus(t);
  await assertSchedules(t, configuration.appliance.id, configuration.schedules, evName);
}

export async function createAppliance(t: TestController, appliance: Appliance) {
  await SideMenu.clickNewAppliance(t);
  await AppliancePage.setAppliance(t, appliance);
  await AppliancePage.clickSave(t);

  await t.expect(Selector(SideMenu.appliance(appliance.id)).exists).ok('The appliance created should show up in the side menu');
}

export async function assertAppliance(t: TestController, appliance: Appliance) {
  await SideMenu.clickAppliance(t, appliance.id);
  await AppliancePage.assertAppliance(t, appliance);
}

export async function createMeter(t: TestController, applianceId: string, meter: Meter) {
  await SideMenu.clickMeter(t, applianceId);
  if (meter.type === S0ElectricityMeter.TYPE) {
    await S0MeterPage.setS0ElectricityMeter(t, meter.s0ElectricityMeter);
  }
  else if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.setHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  else if (meter.type === ModbusElectricityMeter.TYPE) {
    await ModbusMeterPage.setModbusElectricityMeter(t, meter.modbusElectricityMeter);
  }
  else if (meter.type === MqttElectricityMeter.TYPE) {
    await MqttMeterPage.setMqttElectricityMeter(t, meter.mqttElectricityMeter);
  }
  else if (meter.type === SlaveElectricityMeter.TYPE) {
    await SlaveMeterPage.setSlaveMeter(t, meter.slaveElectricityMeter);
  } else {
    await MasterMeterPage.setMasterMeter(t, meter.isMasterMeter, meter.masterElectricityMeter);
  }
  await NotificationPage.setNotifications(t, meter.notifications);
  await MeterPage.clickSave(t);
}
export async function assertMeter(t: TestController, applianceId: string, meter: Meter) {
  await SideMenu.clickMeter(t, applianceId);
  if (meter.type === S0ElectricityMeter.TYPE) {
    await S0MeterPage.assertS0ElectricityMeter(t, meter.s0ElectricityMeter);
  }
  else if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.assertHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  else if (meter.type === ModbusElectricityMeter.TYPE) {
    await ModbusMeterPage.assertModbusElectricityMeter(t, meter.modbusElectricityMeter);
  }
  else if (meter.type === MqttElectricityMeter.TYPE) {
    await MqttMeterPage.assertMqttElectricityMeter(t, meter.mqttElectricityMeter);
  }
  else if (meter.type === SlaveElectricityMeter.TYPE) {
    await SlaveMeterPage.assertSlaveMeter(t, meter.slaveElectricityMeter);
  } else {
    await MasterMeterPage.assertMasterMeter(t, meter.isMasterMeter, meter.masterElectricityMeter);
  }
  await NotificationPage.assertNotifications(t, meter.notifications);
}

export async function createControl(t: TestController, applianceId: string, control: Control, controlTemplate?: string) {
  await SideMenu.clickControl(t, applianceId);
  if (control.type === AlwaysOnSwitch.TYPE) {
    await AlwaysOnSwitchPage.setAlwaysOnSwitch(t, control.alwaysOnSwitch);
  }
  else if (control.type === Switch.TYPE) {
    await SwitchPage.setSwitch(t, control.switch_);
  }
  else if (control.type === HttpSwitch.TYPE) {
    await HttpControlPage.setHttpSwitch(t, control.httpSwitch);
  }
  else if (control.type === ModbusSwitch.TYPE) {
    await ModbusControlPage.setModbusSwitch(t, control.modbusSwitch);
  }
  else if (control.type === MqttSwitch.TYPE) {
    await MqttControlPage.setMqttSwitch(t, control.mqttSwitch);
  }
  else if (control.type === LevelSwitch.TYPE) {
    await LevelControlPage.setLevelSwitch(t, control.levelSwitch);
  }
  else if (control.type === PwmSwitch.TYPE) {
    await PwmControlPage.setPwmSwitch(t, control.pwmSwitch);
  }
  else if (control.type === EvCharger.TYPE) {
    await EvchargerPage.setEvChargerFromTemplate(t, control.evCharger, controlTemplate);
    await EvchargerPage.setElectricVehicles(t, applianceId, control.evCharger.vehicles);
  }
  if (control.startingCurrentSwitchUsed) {
    await StartingCurrentSwitchPage.setStartingCurrentSwitch(t, control.startingCurrentSwitchUsed, control.startingCurrentSwitch);
  }
  await NotificationPage.setNotifications(t, control.notifications);
  await ControlPage.clickSave(t);
}
export async function assertControl(t: TestController, applianceId: string, control: Control) {
  await SideMenu.clickControl(t, applianceId);
  if (control.type === AlwaysOnSwitch.TYPE) {
    await AlwaysOnSwitchPage.assertAlwaysOnSwitch(t, control.alwaysOnSwitch);
  }
  else if (control.type === Switch.TYPE) {
    await SwitchPage.assertSwitch(t, control.switch_);
  }
  else if (control.type === HttpSwitch.TYPE) {
    await HttpControlPage.assertHttpSwitch(t, control.httpSwitch);
  }
  else if (control.type === ModbusSwitch.TYPE) {
    await ModbusControlPage.assertModbusSwitch(t, control.modbusSwitch);
  }
  else if (control.type === MqttSwitch.TYPE) {
    await MqttControlPage.assertMqttSwitch(t, control.mqttSwitch);
  }
  else if (control.type === LevelSwitch.TYPE) {
    await LevelControlPage.assertLevelSwitch(t, control.levelSwitch);
  }
  else if (control.type === PwmSwitch.TYPE) {
    await PwmControlPage.assertPwmSwitch(t, control.pwmSwitch);
  }
  else if (control.type === EvCharger.TYPE) {
    await EvchargerPage.assertEvCharger(t, control.evCharger);
    await EvchargerPage.assertElectricVehicles(t, applianceId, control.evCharger.vehicles);
  }
  if (control.startingCurrentSwitchUsed) {
    await StartingCurrentSwitchPage.assertStartingCurrentSwitch(t, control.startingCurrentSwitchUsed, control.startingCurrentSwitch);
  }
  await NotificationPage.assertNotifications(t, control.notifications);
}

export async function createSchedules(t: TestController, applianceId: string, schedules: Schedule[]) {
  await SideMenu.clickSchedule(t, applianceId);
  await SchedulesPage.setSchedules(t, schedules);
  await SchedulesPage.clickSave(t);
}
export async function assertSchedules(t: TestController, applianceId: string, schedules: Schedule[], evName?: string) {
  await SideMenu.clickSchedule(t, applianceId);
  await SchedulesPage.assertSchedules(t, schedules, evName);
}
