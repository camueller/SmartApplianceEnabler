import {Selector} from 'testcafe';
import {S0ElectricityMeter} from '../../../../../main/angular/src/app/meter-s0/s0-electricity-meter';

export class MeterPage {

  private static typeSelect = Selector('select[formcontrolname="meterType"]');
  private static saveButton = Selector('button[type="submit"]');

  public static async setTypeS0(t: TestController) {
    return MeterPage.setType(t, S0ElectricityMeter.TYPE);
  }

  private static async setType(t: TestController, type: string): Promise<TestController> {
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
