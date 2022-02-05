import {
  assertSelectOption,
  clickButton,
  selectOption,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName,
  selectorStringByFormControlNameOrNgReflectName
} from '../../shared/form';
import {simpleControlType} from '../../../../../main/angular/src/app/shared/form-util';
import {Selector} from 'testcafe';

export class ControlPage {

  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';

  public static pageSelector(t: TestController): Selector {
    const selectorStringControlType = selectorStringByFormControlNameOrNgReflectName('controlType');
    const selectorStringTemplate = selectorStringByFormControlNameOrNgReflectName('template');
    return Selector(`${selectorStringControlType},${selectorStringTemplate}`);
  }

  public static async waitForPage(t: TestController): Promise<void> {
    await t.expect(await this.pageExists(t)).ok();
  }

  public static async pageExists(t: TestController): Promise<boolean> {
    return (await this.pageSelector(t)).exists;
  }

  public static async setType(t: TestController, controlType: string) {
    await this.waitForPage(t);
    await selectOption(t, selectorSelectByFormControlName('controlType'), simpleControlType(controlType));
  }
  public static async assertType(t: TestController, controlType: string) {
    await this.waitForPage(t);
    await assertSelectOption(t, selectorSelectedByFormControlName('controlType'), controlType);
  }

  public static async clickSave(t: TestController) {
    await clickButton(t, ControlPage.SAVE_BUTTON_SELECTOR);
  }
}
