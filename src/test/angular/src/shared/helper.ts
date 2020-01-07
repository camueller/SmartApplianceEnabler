import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';
import {TopMenu} from '../page/top-menu.page';
import {SideMenu} from '../page/side.menu.page';
import {AppliancePage} from '../page/appliance.page';
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

export async function createAppliance(t: TestController, appliance: Appliance) {
  await TopMenu.clickAppliances(t);
  await SideMenu.clickNewAppliance(t);
  await AppliancePage.setAppliance(t, appliance);
  await AppliancePage.clickSave(t);

  await t.expect(SideMenu.appliance(appliance.id).exists)
    .ok('The appliance created should show up in the side menu', {timeout: saeRestartTimeout});
}

export async function createMeter(t: TestController, applianceId: string, meter: Meter) {
  await TopMenu.clickAppliances(t);
  await SideMenu.clickMeter(t, applianceId);
  if (meter.type === S0ElectricityMeter.TYPE) {
    await S0MeterPage.setS0ElectricityMeter(t, meter.s0ElectricityMeter);
  }
  if (meter.type === HttpElectricityMeter.TYPE) {
    await HttpMeterPage.setHttpElectricityMeter(t, meter.httpElectricityMeter);
  }
  await S0MeterPage.clickSave(t);
}

export async function createControl(t: TestController, applianceId: string, control: Control) {
  await TopMenu.clickAppliances(t);
  await SideMenu.clickControl(t, applianceId);
  if (control.type === AlwaysOnSwitch.TYPE) {
    await AlwaysOnSwitchPage.setAlwaysOnSwitch(t, control.alwaysOnSwitch);
  }
  if (control.type === Switch.TYPE) {
    await SwitchPage.setSwitch(t, control.switch_);
  }
  await SwitchPage.clickSave(t);
}
