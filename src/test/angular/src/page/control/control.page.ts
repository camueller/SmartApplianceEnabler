import {Selector} from 'testcafe';
import {selectOptionByAttribute, selectorSelectByFormControlName, selectorSelectedByFormControlName} from '../../shared/form';
import {simpleControlType} from '../../../../../main/angular/src/app/shared/form-util';
import {getTranslation} from '../../shared/ngx-translate';

export class ControlPage {

  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, controlType: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('controlType'), simpleControlType(controlType));
  }
  public static async assertType(t: TestController, controlType: string) {
    await t.expect(selectorSelectedByFormControlName('controlType').innerText).eql(getTranslation(controlType));
  }

  public static async clickSave(t: TestController) {
    await t.click(ControlPage.saveButton);
  }
}
