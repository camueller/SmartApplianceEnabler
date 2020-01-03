import {TopMenu} from './page/top-menu.page';
import {SideMenu} from './page/side.menu.page';
import {Appliance} from './page/appliance.page';
import {heatPump} from './fixture/appliance/heatpump';
import {baseUrl} from './page/page';
import {S0ElectricityMeter} from '../../../main/angular/src/app/meter-s0/s0-electricity-meter';
import {S0MeterPage} from './page/meter/S0-meter.page';
import {s0MeterAllAttributes} from './fixture/meter/S0Meter';

fixture('Heat pump').page(baseUrl());

test('Create appliance', async t => {
  await TopMenu.clickAppliances(t);
  await SideMenu.clickNewAppliance(t);
  await Appliance.setId(t, heatPump.id);
  await Appliance.setVendor(t, heatPump.vendor);
  await Appliance.setName(t, heatPump.name);
  await Appliance.setType(t, heatPump.type);
  await Appliance.setSerial(t, heatPump.serial);
  await Appliance.setMaxPowerConsumption(t, heatPump.maxPowerConsumption);
  await Appliance.setInterruptionsAllowed(t, true);
  await Appliance.setMinOnTime(t, heatPump.minOnTime);
  await Appliance.setMaxOnTime(t, heatPump.maxOnTime);
  await Appliance.setMinOffTime(t, heatPump.minOffTime);
  await Appliance.setMaxOffTime(t, heatPump.maxOffTime);
  await Appliance.clickSave(t);

  await t.expect(SideMenu.appliance(heatPump.id).exists)
    .ok('The appliance created should show up in the side menu', { timeout: 10000 });
});

test('Create meter', async t => {
  await TopMenu.clickAppliances(t);
  await SideMenu.clickMeter(t, heatPump.id);
  await S0MeterPage.setType(t, S0ElectricityMeter.TYPE);
  await S0MeterPage.setGpio(t, s0MeterAllAttributes.gpio);
  await S0MeterPage.setPinPullResistance(t, s0MeterAllAttributes.pinPullResistance);
  await S0MeterPage.setImpulsesPerKwh(t, s0MeterAllAttributes.impulsesPerKwh);
  await S0MeterPage.setMeasurementInterval(t, s0MeterAllAttributes.measurementInterval);
  await S0MeterPage.clickSave(t);
  await t.wait(10000);
});
