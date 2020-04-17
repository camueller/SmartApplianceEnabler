import {Selector} from 'testcafe';
import {selectOptionByAttribute, selectorSelectByFormControlName, selectorSelectedByFormControlName} from '../../shared/form';
import {simpleMeterType} from '../../../../../main/angular/src/app/shared/form-util';
import {getTranslation} from '../../shared/ngx-translate';

export class MeterPage {

  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, meterType: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('meterType'), simpleMeterType(meterType));
  }
  public static async assertType(t: TestController, meterType: string) {
    await t.expect(selectorSelectedByFormControlName('meterType').innerText).eql(getTranslation(meterType));
  }

  public static async clickSave(t: TestController) {
    await t.click(MeterPage.saveButton);
  }
}
