import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {SideMenu} from '../page/side.menu.page';
import {AppliancePage} from '../page/appliance/appliance.page';
import {saeRestartTimeout} from './timeout';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
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

export async function waitForApplianceToExist() {
  await Selector('app-appliance', {timeout: saeRestartTimeout}).exists;
}

export async function createAndAssertMeter(t: TestController, configuration: ApplianceConfiguration) {
  await createMeter(t, configuration.appliance.id, configuration.meter);
  await SideMenu.clickStatus(t);
  await assertMeter(t, configuration.appliance.id, configuration.meter);
}

export async function waitForMeterToExist() {
  await Selector('app-meter', {timeout: saeRestartTimeout}).exists;
}

export async function createAndAssertControl(t: TestController, configuration: ApplianceConfiguration) {
  await createControl(t, configuration.appliance.id, configuration.control, configuration.controlTemplate);
  await SideMenu.clickStatus(t);
  await assertControl(t, configuration.appliance.id, configuration.control);
}

export async function waitForControlToExist() {
  await Selector('app-control', {timeout: saeRestartTimeout}).exists;
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
  await waitForApplianceToExist();
  await AppliancePage.setAppliance(t, appliance);
  await AppliancePage.clickSave(t);

  await t.expect(Selector(SideMenu.appliance(appliance.id)).exists)
    .ok('The appliance created should show up in the side menu', {timeout: saeRestartTimeout});
}

export async function assertAppliance(t: TestController, appliance: Appliance) {
  await SideMenu.clickAppliance(t, appliance.id);
  await waitForApplianceToExist();
  await AppliancePage.assertAppliance(t, appliance);
}

export async function createMeter(t: TestController, applianceId: string, meter: Meter) {
  await SideMenu.clickMeter(t, applianceId);
  await waitForMeterToExist();
  if (meter.type === S0ElectricityMeter.TYPE) {
    await S0MeterPage.setS0ElectricityMeter(t, meter.s0ElectricityMeter);
  }
  if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.setHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  if (meter.type === ModbusElectricityMeter.TYPE) {
    await ModbusMeterPage.setModbusElectricityMeter(t, meter.modbusElectricityMeter);
  }
  await NotificationPage.setNotifications(t, meter.notifications);
  await MeterPage.clickSave(t);
}
export async function assertMeter(t: TestController, applianceId: string, meter: Meter) {
  await SideMenu.clickMeter(t, applianceId);
  await waitForMeterToExist();
  if (meter.type === S0ElectricityMeter.TYPE) {
    await S0MeterPage.assertS0ElectricityMeter(t, meter.s0ElectricityMeter);
  }
  if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.assertHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  if (meter.type === ModbusElectricityMeter.TYPE) {
    await ModbusMeterPage.assertModbusElectricityMeter(t, meter.modbusElectricityMeter);
  }
  await NotificationPage.assertNotifications(t, meter.notifications);
}

export async function createControl(t: TestController, applianceId: string, control: Control, controlTemplate?: string) {
  await SideMenu.clickControl(t, applianceId);
  await waitForControlToExist();
  if (control.type === AlwaysOnSwitch.TYPE) {
    await AlwaysOnSwitchPage.setAlwaysOnSwitch(t, control.alwaysOnSwitch);
  }
  if (control.type === Switch.TYPE) {
    await SwitchPage.setSwitch(t, control.switch_);
  }
  if (control.type === HttpSwitch.TYPE) {
    await HttpControlPage.setHttpSwitch(t, control.httpSwitch);
  }
  if (control.type === ModbusSwitch.TYPE) {
    await ModbusControlPage.setModbusSwitch(t, control.modbusSwitch);
  }
  if (control.type === EvCharger.TYPE) {
    await EvchargerPage.setEvChargerFromTemplate(t, control.evCharger, controlTemplate);
    await EvchargerPage.setElectricVehicles(t, applianceId, control.evCharger.vehicles);
  }
  if (control.startingCurrentDetection) {
    await StartingCurrentSwitchPage.setStartingCurrentSwitch(t, control.startingCurrentDetection, control.startingCurrentSwitch);
  }
  await NotificationPage.setNotifications(t, control.notifications);
  await ControlPage.clickSave(t);
}
export async function assertControl(t: TestController, applianceId: string, control: Control) {
  await SideMenu.clickControl(t, applianceId);
  await waitForControlToExist();
  if (control.type === AlwaysOnSwitch.TYPE) {
    await AlwaysOnSwitchPage.assertAlwaysOnSwitch(t, control.alwaysOnSwitch);
  }
  if (control.type === Switch.TYPE) {
    await SwitchPage.assertSwitch(t, control.switch_);
  }
  if (control.type === HttpSwitch.TYPE) {
    await HttpControlPage.assertHttpSwitch(t, control.httpSwitch);
  }
  if (control.type === ModbusSwitch.TYPE) {
    await ModbusControlPage.assertModbusSwitch(t, control.modbusSwitch);
  }
  if (control.type === EvCharger.TYPE) {
    await EvchargerPage.assertEvCharger(t, control.evCharger);
    await EvchargerPage.assertElectricVehicles(t, applianceId, control.evCharger.vehicles);
  }
  if (control.startingCurrentDetection) {
    await StartingCurrentSwitchPage.assertStartingCurrentSwitch(t, control.startingCurrentDetection, control.startingCurrentSwitch);
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
