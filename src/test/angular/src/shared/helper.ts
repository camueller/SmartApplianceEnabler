import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {SideMenu} from '../page/side.menu.page';
import {AppliancePage} from '../page/appliance/appliance.page';
import {saeRestartTimeout} from './timeout';
import {Meter} from '../../../../main/angular/src/app/meter/meter';
import {S0MeterPage} from '../page/meter/s0-meter.page';
import {S0ElectricityMeter} from '../../../../main/angular/src/app/meter-s0/s0-electricity-meter';
import {SwitchPage} from '../page/control/switch.page';
import {Control} from '../../../../main/angular/src/app/control/control';
import {HttpElectricityMeter} from '../../../../main/angular/src/app/meter-http/http-electricity-meter';
import {HttpMeterPage} from '../page/meter/http-meter.page';
import {AlwaysOnSwitchPage} from '../page/control/always-on-switch.page';
import {HttpControlPage} from '../page/control/http-control.page';
import {ModbusElectricityMeter} from '../../../../main/angular/src/app/meter-modbus/modbus-electricity-meter';
import {ModbusMeterPage} from '../page/meter/modbus-meter.page';
import {GlobalContext} from './global-context';
import {TopMenu} from '../page/top-menu.page';
import {ApplianceConfiguration} from './appliance-configuration';
import {ModbusControlPage} from '../page/control/modbus-control.page';
import {HttpSwitch} from '../../../../main/angular/src/app/control/http/http-switch';
import {AlwaysOnSwitch} from '../../../../main/angular/src/app/control/alwayson/always-on-switch';
import {ModbusSwitch} from '../../../../main/angular/src/app/control/modbus/modbus-switch';
import {Switch} from '../../../../main/angular/src/app/control/switch/switch';

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
  await TopMenu.clickStatus(t);
  await assertAppliance(t, configuration.appliance);
}

export async function createAndAssertMeter(t: TestController, configuration: ApplianceConfiguration) {
  await createMeter(t, configuration.appliance.id, configuration.meter);
  await TopMenu.clickStatus(t);
  await assertMeter(t, configuration.appliance.id, configuration.meter);
}

export async function createAndAssertControl(t: TestController, configuration: ApplianceConfiguration) {
  await createControl(t, configuration.appliance.id, configuration.control);
  await TopMenu.clickStatus(t);
  await assertControl(t, configuration.appliance.id, configuration.control);
}

export async function createAppliance(t: TestController, appliance: Appliance) {
  await SideMenu.clickNewAppliance(t);
  await AppliancePage.setAppliance(t, appliance);
  await AppliancePage.clickSave(t);

  await t.expect(SideMenu.appliance(appliance.id).exists)
    .ok('The appliance created should show up in the side menu', {timeout: saeRestartTimeout});
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
  if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.setHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  if (meter.type === ModbusElectricityMeter.TYPE) {
    await ModbusMeterPage.setModbusElectricityMeter(t, meter.modbusElectricityMeter);
  }
  await S0MeterPage.clickSave(t);
}
export async function assertMeter(t: TestController, applianceId: string, meter: Meter) {
  await SideMenu.clickMeter(t, applianceId);
  if (meter.type === S0ElectricityMeter.TYPE) {
    await S0MeterPage.assertS0ElectricityMeter(t, meter.s0ElectricityMeter);
  }
  if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.assertHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  if (meter.type === ModbusElectricityMeter.TYPE) {
    await ModbusMeterPage.assertModbusElectricityMeter(t, meter.modbusElectricityMeter);
  }
}

export async function createControl(t: TestController, applianceId: string, control: Control) {
  await SideMenu.clickControl(t, applianceId);
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
  await SwitchPage.clickSave(t);
}
export async function assertControl(t: TestController, applianceId: string, control: Control) {
  await SideMenu.clickControl(t, applianceId);
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
}
