import {Selector} from 'testcafe';
import {
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export class AppliancePage {

  private static saveButton = Selector('button[type="submit"]');

  public static async setAppliance(t: TestController, appliance: Appliance) {
    await AppliancePage.setId(t, appliance.id);
    await AppliancePage.setVendor(t, appliance.vendor);
    await AppliancePage.setName(t, appliance.name);
    await AppliancePage.setType(t, appliance.type);
    await AppliancePage.setSerial(t, appliance.serial);
    await AppliancePage.setMinPowerConsumption(t, appliance.minPowerConsumption);
    await AppliancePage.setMaxPowerConsumption(t, appliance.maxPowerConsumption);
    await AppliancePage.setInterruptionsAllowed(t, appliance.interruptionsAllowed);
    await AppliancePage.setMinOnTime(t, appliance.minOnTime);
    await AppliancePage.setMaxOnTime(t, appliance.maxOnTime);
    await AppliancePage.setMinOffTime(t, appliance.minOffTime);
    await AppliancePage.setMaxOffTime(t, appliance.maxOffTime);
  }
  public static async assertAppliance(t: TestController, appliance: Appliance) {
    await AppliancePage.assertId(t, appliance.id);
    await AppliancePage.assertVendor(t, appliance.vendor);
    await AppliancePage.assertName(t, appliance.name);
    await AppliancePage.assertType(t, appliance.type);
    await AppliancePage.assertSerial(t, appliance.serial);
    await AppliancePage.assertMinPowerConsumption(t, appliance.minPowerConsumption);
    await AppliancePage.assertMaxPowerConsumption(t, appliance.maxPowerConsumption);
    await AppliancePage.assertInterruptionsAllowed(t, appliance.interruptionsAllowed);
    await AppliancePage.assertMinOnTime(t, appliance.minOnTime);
    await AppliancePage.assertMaxOnTime(t, appliance.maxOnTime);
    await AppliancePage.assertMinOffTime(t, appliance.minOffTime);
    await AppliancePage.assertMaxOffTime(t, appliance.maxOffTime);
  }

  public static async setId(t: TestController, id: string) {
    await inputText(t, selectorInputByFormControlName('id'), id);
  }
  public static async assertId(t: TestController, id: string) {
    await t.expect(selectorInputByFormControlName('id').value).eql(id);
  }

  public static async setVendor(t: TestController, vendor: string) {
    await inputText(t, selectorInputByFormControlName('vendor'), vendor);
  }
  public static async assertVendor(t: TestController, vendor: string) {
    await t.expect(selectorInputByFormControlName('vendor').value).eql(vendor);
  }

  public static async setName(t: TestController, name: string) {
    await inputText(t, selectorInputByFormControlName('name'), name);
  }
  public static async assertName(t: TestController, name: string) {
    await t.expect(selectorInputByFormControlName('name').value).eql(name);
  }

  public static async setType(t: TestController, type: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('type'), type);
  }
  public static async assertType(t: TestController, type: string) {
    await t.expect(selectorSelectByFormControlName('type').value).eql(type);
  }

  public static async setSerial(t: TestController, serial: string) {
    await inputText(t, selectorInputByFormControlName('serial'), serial);
  }
  public static async assertSerial(t: TestController, serial: string) {
    await t.expect(selectorInputByFormControlName('serial').value).eql(serial);
  }

  public static async setMinPowerConsumption(t: TestController, minPowerConsumption: number) {
    await inputText(t, selectorInputByFormControlName('minPowerConsumption'), minPowerConsumption && minPowerConsumption.toString());
  }
  public static async assertMinPowerConsumption(t: TestController, minPowerConsumption: number) {
    await t.expect(selectorInputByFormControlName('minPowerConsumption').value).eql(
      minPowerConsumption ? minPowerConsumption.toString() : '');
  }

  public static async setMaxPowerConsumption(t: TestController, maxPowerConsumption: number) {
    await inputText(t, selectorInputByFormControlName('maxPowerConsumption'), maxPowerConsumption && maxPowerConsumption.toString());
  }
  public static async assertMaxPowerConsumption(t: TestController, maxPowerConsumption: number) {
    await t.expect(selectorInputByFormControlName('maxPowerConsumption').value).eql(
      maxPowerConsumption ? maxPowerConsumption.toString() : '');
  }

  public static async setInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean) {
    await setCheckboxEnabled(t, selectorInputByFormControlName('interruptionsAllowed'), interruptionsAllowed);
  }
  public static async assertInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean) {
    await t.expect(selectorInputByFormControlName('interruptionsAllowed').checked).eql(interruptionsAllowed);
  }

  public static async setMinOnTime(t: TestController, minOnTime: number) {
    await inputText(t, selectorInputByFormControlName('minOnTime'), minOnTime && minOnTime.toString());
  }
  public static async assertMinOnTime(t: TestController, minOnTime: number) {
    await t.expect(selectorInputByFormControlName('minOnTime').value).eql(
      minOnTime ? minOnTime.toString() : '');
  }

  public static async setMaxOnTime(t: TestController, maxOnTime: number) {
    await inputText(t, selectorInputByFormControlName('maxOnTime'), maxOnTime && maxOnTime.toString());
  }
  public static async assertMaxOnTime(t: TestController, maxOnTime: number) {
    await t.expect(selectorInputByFormControlName('maxOnTime').value).eql(
      maxOnTime ? maxOnTime.toString() : '');
  }

  public static async setMinOffTime(t: TestController, minOffTime: number) {
    await inputText(t, selectorInputByFormControlName('minOffTime'), minOffTime && minOffTime.toString());
  }
  public static async assertMinOffTime(t: TestController, minOffTime: number) {
    await t.expect(selectorInputByFormControlName('minOffTime').value).eql(
      minOffTime ? minOffTime.toString() : '');
  }

  public static async setMaxOffTime(t: TestController, maxOffTime: number) {
    await inputText(t, selectorInputByFormControlName('maxOffTime'), maxOffTime && maxOffTime.toString());
  }
  public static async assertMaxOffTime(t: TestController, maxOffTime: number) {
    await t.expect(selectorInputByFormControlName('maxOffTime').value).eql(
      maxOffTime ? maxOffTime.toString() : '');
  }

  public static async clickSave(t: TestController) {
    await t.click(AppliancePage.saveButton);
  }
}
