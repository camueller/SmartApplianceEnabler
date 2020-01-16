import {Selector} from 'testcafe';
import {getIndexedSelectOptionValueRegExp, selectOptionByAttribute, selectorSelectByFormControlName} from '../../shared/form';

export class ControlPage {

  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, controlType: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('controlType'), controlType, true);
  }
  public static async assertType(t: TestController, controlType: string) {
    await t.expect(selectorSelectByFormControlName('controlType').value).match(getIndexedSelectOptionValueRegExp(controlType));
  }

  public static async clickSave(t: TestController) {
    await t.click(ControlPage.saveButton);
  }
}
