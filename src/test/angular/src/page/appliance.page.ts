import {Selector} from 'testcafe';
import {setCheckboxEnabled} from '../shared/checkbox';

export class Appliance {

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

  public static async setId(t: TestController, id: string): Promise<TestController> {
    await t.typeText(Appliance.idInput, id);
    return t;
  }

  public static async setVendor(t: TestController, vendor: string): Promise<TestController> {
    await t.typeText(Appliance.vendorInput, vendor);
    return t;
  }

  public static async setName(t: TestController, name: string): Promise<TestController> {
    await t.typeText(Appliance.nameInput, name);
    return t;
  }

  public static async setType(t: TestController, type: string): Promise<TestController> {
    await t
      .click(Appliance.typeSelect)
      .click(Appliance.typeSelect.find('option').withAttribute('value', type));
    return t;
  }

  public static async setSerial(t: TestController, serial: string): Promise<TestController> {
    await t.typeText(Appliance.serialInput, serial);
    return t;
  }

  public static async setMinPowerConsumption(t: TestController, minPowerConsumption: number): Promise<TestController> {
    await t.typeText(Appliance.minPowerConsumptionInput, minPowerConsumption.toString());
    return t;
  }

  public static async setMaxPowerConsumption(t: TestController, maxPowerConsumption: number): Promise<TestController> {
    await t.typeText(Appliance.maxPowerConsumptionInput, maxPowerConsumption.toString());
    return t;
  }

  public static async setInterruptionsAllowed(t: TestController, interruptionsAllowed: boolean): Promise<TestController> {
    return setCheckboxEnabled(t, Appliance.interruptionsAllowedInput, interruptionsAllowed);
  }

  public static async setMinOnTime(t: TestController, minOnTime: number): Promise<TestController> {
    await t.typeText(Appliance.minOnTimeInput, minOnTime.toString());
    return t;
  }

  public static async setMaxOnTime(t: TestController, maxOnTime: number): Promise<TestController> {
    await t.typeText(Appliance.maxOnTimeInput, maxOnTime.toString());
    return t;
  }

  public static async setMinOffTime(t: TestController, minOffTime: number): Promise<TestController> {
    await t.typeText(Appliance.minOffTimeInput, minOffTime.toString());
    return t;
  }

  public static async setMaxOffTime(t: TestController, maxOffTime: number): Promise<TestController> {
    await t.typeText(Appliance.maxOffTimeInput, maxOffTime.toString());
    return t;
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(Appliance.saveButton);
    return t;
  }
}
