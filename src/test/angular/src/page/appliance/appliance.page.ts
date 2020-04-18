import {Selector} from 'testcafe';
import {
  assertCheckbox,
  assertInput, assertSelectNEW,
  inputText,
  selectOptionByAttribute, selectorCheckboxByFormControlName, selectorCheckboxCheckedByFormControlName,
  selectorInputByFormControlName,
  selectorSelectByFormControlName, selectorSelectedByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';
import {getTranslation} from '../../shared/ngx-translate';

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
    await assertInput(t, selectorInputByFormControlName('id'), id);
  }

  public static async setVendor(t: TestController, vendor: string) {
    await inputText(t, selectorInputByFormControlName('vendor'), vendor);
  }

  public static async assertVendor(t: TestController, vendor: string) {
    await assertInput(t, selectorInputByFormControlName('vendor'), vendor);
  }

  public static async setName(t: TestController, name: string) {
    await inputText(t, selectorInputByFormControlName('name'), name);
  }

  public static async assertName(t: TestController, name: string) {
    await assertInput(t, selectorInputByFormControlName('name'), name);
  }

  public static async setType(t: TestController, type: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('type'), type);
  }

  public static async assertType(t: TestController, type: string) {
    await assertSelectNEW(t, selectorSelectedByFormControlName('type'), type, 'ApplianceComponent.type.');
  }

  public static async setSerial(t: TestController, serial: string) {
    await inputText(t, selectorInputByFormControlName('serial'), serial);
  }

  public static async assertSerial(t: TestController, serial: string) {
    await assertInput(t, selectorInputByFormControlName('serial'), serial);
  }

  public static async setMinPowerConsumption(t: TestController, minPowerConsumption: number) {
    if (minPowerConsumption) {
      await inputText(t, selectorInputByFormControlName('minPowerConsumption'), minPowerConsumption.toString());
    }
  }

  public static async assertMinPowerConsumption(t: TestController, minPowerConsumption: number) {
    if (minPowerConsumption) {
      await assertInput(t, selectorInputByFormControlName('minPowerConsumption'), minPowerConsumption.toString());
    }
  }

  public static async setMaxPowerConsumption(t: TestController, maxPowerConsumption: number) {
    await inputText(t, selectorInputByFormControlName('maxPowerConsumption'), maxPowerConsumption.toString());
  }

  public static async assertMaxPowerConsumption(t: TestController, maxPowerConsumption: number) {
    await assertInput(t, selectorInputByFormControlName('maxPowerConsumption'), maxPowerConsumption.toString());
  }

  public static async setInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('interruptionsAllowed'), interruptionsAllowed);
  }

  public static async assertInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean) {
    assertCheckbox(t, selectorCheckboxCheckedByFormControlName('interruptionsAllowed'), interruptionsAllowed);
  }

  public static async setMinOnTime(t: TestController, minOnTime: number) {
    await inputText(t, selectorInputByFormControlName('minOnTime'), minOnTime && minOnTime.toString());
  }

  public static async assertMinOnTime(t: TestController, minOnTime: number) {
    await assertInput(t, selectorInputByFormControlName('minOnTime'), minOnTime ? minOnTime.toString() : '');
  }

  public static async setMaxOnTime(t: TestController, maxOnTime: number) {
    await inputText(t, selectorInputByFormControlName('maxOnTime'), maxOnTime && maxOnTime.toString());
  }

  public static async assertMaxOnTime(t: TestController, maxOnTime: number) {
    await assertInput(t, selectorInputByFormControlName('maxOnTime'), maxOnTime ? maxOnTime.toString() : '');
  }

  public static async setMinOffTime(t: TestController, minOffTime: number) {
    await inputText(t, selectorInputByFormControlName('minOffTime'), minOffTime && minOffTime.toString());
  }

  public static async assertMinOffTime(t: TestController, minOffTime: number) {
    await assertInput(t, selectorInputByFormControlName('minOffTime'), minOffTime ? minOffTime.toString() : '');
  }

  public static async setMaxOffTime(t: TestController, maxOffTime: number) {
    await inputText(t, selectorInputByFormControlName('maxOffTime'), maxOffTime && maxOffTime.toString());
  }

  public static async assertMaxOffTime(t: TestController, maxOffTime: number) {
    await assertInput(t, selectorInputByFormControlName('maxOffTime'), maxOffTime ? maxOffTime.toString() : '');
  }

  public static async clickSave(t: TestController) {
    await t.click(AppliancePage.saveButton);
  }
}
