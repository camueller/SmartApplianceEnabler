import {Selector} from 'testcafe';
import {
  assertCheckbox,
  assertSelect,
  clickButton,
  selectOptionByAttribute,
  selectorCheckboxByFormControlName,
  selectorCheckboxCheckedByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {simpleControlType} from '../../../../../main/angular/src/app/shared/form-util';

export class ControlPage {

  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, controlType: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('controlType'), simpleControlType(controlType));
  }
  public static async assertType(t: TestController, controlType: string) {
    await assertSelect(t, selectorSelectedByFormControlName('controlType'), controlType);
  }

  public static async setStartingCurrentDetection(t: TestController, startingCurrentDetection: boolean) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('startingCurrentDetection'), startingCurrentDetection);
  }
  public static async assertStartingCurrentDetection(t: TestController, startingCurrentDetection: boolean) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('startingCurrentDetection'), startingCurrentDetection);
  }

  public static async clickSave(t: TestController) {
    await clickButton(t, ControlPage.saveButton);
  }
}
