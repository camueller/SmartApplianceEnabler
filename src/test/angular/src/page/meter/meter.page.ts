import {Selector} from 'testcafe';
import {getIndexedSelectOptionValueRegExp, selectOptionByAttribute} from '../../shared/form';

export class MeterPage {

  private static typeSelect = Selector('select[formcontrolname="meterType"]');
  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, type: string): Promise<TestController> {
    await selectOptionByAttribute(t, MeterPage.typeSelect, type, true);
    return t;
  }
  public static async assertType(t: TestController, type: string) {
    await t.expect(MeterPage.typeSelect.value).match(getIndexedSelectOptionValueRegExp(type));
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(MeterPage.saveButton);
    return t;
  }
}
