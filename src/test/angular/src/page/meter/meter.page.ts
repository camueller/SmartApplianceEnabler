import {Selector} from 'testcafe';
import {getIndexedSelectOptionValueRegExp, selectOptionByAttribute, selectorSelectByFormControlName} from '../../shared/form';

export class MeterPage {

  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, meterType: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('meterType'), meterType, true);
  }
  public static async assertType(t: TestController, meterType: string) {
    await t.expect(selectorSelectByFormControlName('meterType').value).match(getIndexedSelectOptionValueRegExp(meterType));
  }

  public static async clickSave(t: TestController) {
    await t.click(MeterPage.saveButton);
  }
}
