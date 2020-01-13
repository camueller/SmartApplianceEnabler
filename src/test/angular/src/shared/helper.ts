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
import {Switch} from '../../../../main/angular/src/app/control-switch/switch';
import {AlwaysOnSwitch} from '../../../../main/angular/src/app/control-alwayson/always-on-switch';
import {AlwaysOnSwitchPage} from '../page/control/always-on-switch.page';
import {HttpSwitch} from '../../../../main/angular/src/app/control-http/http-switch';
import {HttpControlPage} from '../page/control/http-control.page';

export function fixtureName(t: TestController) {
  // @ts-ignore
  return t.testRun.test.fixture.name;
}

export function configurationKey(t: TestController, configurationName: string) {
  return JSON.stringify({configurationName, userAgent: t.browser.name});
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
}
