import {
  assertSelectOption,
  clickButton,
  selectOption,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {simpleMeterType} from '../../../../../main/angular/src/app/shared/form-util';

export class MeterPage {

  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';

  public static pageSelector(t: TestController): Selector {
    return selectorSelectByFormControlName('meterType');
  }

  public static async waitForPage(t: TestController): Promise<void> {
    await t.expect(await this.pageExists(t)).ok();
  }

  public static async pageExists(t: TestController): Promise<boolean> {
    return (await this.pageSelector(t)).exists;
  }

  public static async setType(t: TestController, meterType: string) {
    await this.waitForPage(t);
    await selectOption(t, selectorSelectByFormControlName('meterType'), simpleMeterType(meterType));
  }
  public static async assertType(t: TestController, meterType: string) {
    await this.waitForPage(t);
    await assertSelectOption(t, selectorSelectedByFormControlName('meterType'), meterType);
  }

  public static async clickSave(t: TestController) {
    await clickButton(t, MeterPage.SAVE_BUTTON_SELECTOR);
  }
}
