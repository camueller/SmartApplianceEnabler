import {Selector} from 'testcafe';

export class MeterPage {

  private static typeSelect = Selector('select[formcontrolname="meterType"]');
  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, type: string): Promise<TestController> {
    const pattern = `...${type.substr(0, 47)}`; // <option value="0: de.avanux.smartapplianceenabler.meter.S0Electri" ...
    await t
      .click(MeterPage.typeSelect)
      .click(MeterPage.typeSelect.find('option').withAttribute('value', new RegExp(pattern)));
    return t;
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(MeterPage.saveButton);
    return t;
  }
}
