import {Selector} from 'testcafe';
import {inputText, selectOptionByAttribute, setCheckboxEnabled} from '../../shared/form';
import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export class AppliancePage {

  private static idInput = Selector('input[formcontrolname="id"]');
  private static vendorInput = Selector('input[formcontrolname="vendor"]');
  private static nameInput = Selector('input[formcontrolname="name"]');
  private static typeSelect = Selector('select[formcontrolname="type"]');
  private static serialInput = Selector('input[formcontrolname="serial"]');
  private static minPowerConsumptionInput = Selector('input[formcontrolname="minPowerConsumption"]');
  private static maxPowerConsumptionInput = Selector('input[formcontrolname="maxPowerConsumption"]');
  private static interruptionsAllowedInput = Selector('input[formcontrolname="interruptionsAllowed"]');
  private static minOnTimeInput = Selector('input[formcontrolname="minOnTime"]');
  private static maxOnTimeInput = Selector('input[formcontrolname="maxOnTime"]');
  private static minOffTimeInput = Selector('input[formcontrolname="minOffTime"]');
  private static maxOffTimeInput = Selector('input[formcontrolname="maxOffTime"]');
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

  public static async setId(t: TestController, id: string): Promise<TestController> {
    await inputText(t, AppliancePage.idInput, id);
    return t;
  }
  public static async assertId(t: TestController, id: string) {
    await t.expect(AppliancePage.idInput.value).eql(id);
  }

  public static async setVendor(t: TestController, vendor: string): Promise<TestController> {
    await inputText(t, AppliancePage.vendorInput, vendor);
    return t;
  }
  public static async assertVendor(t: TestController, vendor: string) {
    await t.expect(AppliancePage.vendorInput.value).eql(vendor);
  }

  public static async setName(t: TestController, name: string): Promise<TestController> {
    await inputText(t, AppliancePage.nameInput, name);
    return t;
  }
  public static async assertName(t: TestController, name: string) {
    await t.expect(AppliancePage.nameInput.value).eql(name);
  }

  public static async setType(t: TestController, type: string): Promise<TestController> {
    await selectOptionByAttribute(t, AppliancePage.typeSelect, type);
    return t;
  }
  public static async assertType(t: TestController, type: string) {
    await t.expect(AppliancePage.typeSelect.value).eql(type);
  }

  public static async setSerial(t: TestController, serial: string): Promise<TestController> {
    await inputText(t, AppliancePage.serialInput, serial);
    return t;
  }
  public static async assertSerial(t: TestController, serial: string) {
    await t.expect(AppliancePage.serialInput.value).eql(serial);
  }

  public static async setMinPowerConsumption(t: TestController, minPowerConsumption: number): Promise<TestController> {
    await inputText(t, AppliancePage.minPowerConsumptionInput, minPowerConsumption && minPowerConsumption.toString());
    return t;
  }
  public static async assertMinPowerConsumption(t: TestController, minPowerConsumption: number) {
    await t.expect(AppliancePage.minPowerConsumptionInput.value).eql(
      minPowerConsumption ? minPowerConsumption.toString() : '');
  }

  public static async setMaxPowerConsumption(t: TestController, maxPowerConsumption: number): Promise<TestController> {
    await inputText(t, AppliancePage.maxPowerConsumptionInput, maxPowerConsumption && maxPowerConsumption.toString());
    return t;
  }
  public static async assertMaxPowerConsumption(t: TestController, maxPowerConsumption: number) {
    await t.expect(AppliancePage.maxPowerConsumptionInput.value).eql(
      maxPowerConsumption ? maxPowerConsumption.toString() : '');
  }

  public static async setInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean): Promise<TestController> {
    await setCheckboxEnabled(t, AppliancePage.interruptionsAllowedInput, interruptionsAllowed);
    return t;
  }
  public static async assertInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean) {
    await t.expect(AppliancePage.interruptionsAllowedInput.checked).eql(interruptionsAllowed);
  }

  public static async setMinOnTime(t: TestController, minOnTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.minOnTimeInput, minOnTime && minOnTime.toString());
    return t;
  }
  public static async assertMinOnTime(t: TestController, minOnTime: number) {
    await t.expect(AppliancePage.minOnTimeInput.value).eql(
      minOnTime ? minOnTime.toString() : '');
  }

  public static async setMaxOnTime(t: TestController, maxOnTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.maxOnTimeInput, maxOnTime && maxOnTime.toString());
    return t;
  }
  public static async assertMaxOnTime(t: TestController, maxOnTime: number) {
    await t.expect(AppliancePage.maxOnTimeInput.value).eql(
      maxOnTime ? maxOnTime.toString() : '');
  }

  public static async setMinOffTime(t: TestController, minOffTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.minOffTimeInput, minOffTime && minOffTime.toString());
    return t;
  }
  public static async assertMinOffTime(t: TestController, minOffTime: number) {
    await t.expect(AppliancePage.minOffTimeInput.value).eql(
      minOffTime ? minOffTime.toString() : '');
  }

  public static async setMaxOffTime(t: TestController, maxOffTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.maxOffTimeInput, maxOffTime && maxOffTime.toString());
    return t;
  }
  public static async assertMaxOffTime(t: TestController, maxOffTime: number) {
    await t.expect(AppliancePage.maxOffTimeInput.value).eql(
      maxOffTime ? maxOffTime.toString() : '');
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(AppliancePage.saveButton);
    return t;
  }
}
