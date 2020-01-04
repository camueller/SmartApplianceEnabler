import {Selector} from 'testcafe';
import {inputText, setCheckboxEnabled} from '../shared/form';
import {Appliance} from '../../../../main/angular/src/app/appliance/appliance';

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

  public static async setId(t: TestController, id: string): Promise<TestController> {
    await inputText(t, AppliancePage.idInput, id);
    return t;
  }

  public static async setVendor(t: TestController, vendor: string): Promise<TestController> {
    await inputText(t, AppliancePage.vendorInput, vendor);
    return t;
  }

  public static async setName(t: TestController, name: string): Promise<TestController> {
    await inputText(t, AppliancePage.nameInput, name);
    return t;
  }

  public static async setType(t: TestController, type: string): Promise<TestController> {
    await t
      .click(AppliancePage.typeSelect)
      .click(AppliancePage.typeSelect.find('option').withAttribute('value', type));
    return t;
  }

  public static async setSerial(t: TestController, serial: string): Promise<TestController> {
    await inputText(t, AppliancePage.serialInput, serial);
    return t;
  }

  public static async setMinPowerConsumption(t: TestController, minPowerConsumption: number): Promise<TestController> {
    await inputText(t, AppliancePage.minPowerConsumptionInput, minPowerConsumption && minPowerConsumption.toString());
    return t;
  }

  public static async setMaxPowerConsumption(t: TestController, maxPowerConsumption: number): Promise<TestController> {
    await inputText(t, AppliancePage.maxPowerConsumptionInput, maxPowerConsumption && maxPowerConsumption.toString());
    return t;
  }

  public static async setInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean): Promise<TestController> {
    await setCheckboxEnabled(t, AppliancePage.interruptionsAllowedInput, interruptionsAllowed);
    return t;
  }

  public static async setMinOnTime(t: TestController, minOnTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.minOnTimeInput, minOnTime && minOnTime.toString());
    return t;
  }

  public static async setMaxOnTime(t: TestController, maxOnTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.maxOnTimeInput, maxOnTime && maxOnTime.toString());
    return t;
  }

  public static async setMinOffTime(t: TestController, minOffTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.minOffTimeInput, minOffTime && minOffTime.toString());
    return t;
  }

  public static async setMaxOffTime(t: TestController, maxOffTime: number): Promise<TestController> {
    await inputText(t, AppliancePage.maxOffTimeInput, maxOffTime && maxOffTime.toString());
    return t;
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(AppliancePage.saveButton);
    return t;
  }
}
