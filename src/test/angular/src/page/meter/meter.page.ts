import {
  assertSelectOption,
  clickButton,
  selectOption,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {simpleMeterType} from '../../../../../main/angular/src/app/shared/form-util';
import {Selector} from 'testcafe';
import {saeRestartTimeout} from '../../shared/timeout';

export class MeterPage {

  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';

  private static async waitForPage() {
    await Selector(selectorSelectByFormControlName('meterType'), {timeout: saeRestartTimeout}).exists;
  }

  public static async setType(t: TestController, meterType: string) {
    await this.waitForPage();
    await selectOption(t, selectorSelectByFormControlName('meterType'), simpleMeterType(meterType));
  }
  public static async assertType(t: TestController, meterType: string) {
    await this.waitForPage();
    await assertSelectOption(t, selectorSelectedByFormControlName('meterType'), meterType);
  }

  public static async clickSave(t: TestController) {
    await clickButton(t, MeterPage.SAVE_BUTTON_SELECTOR);
  }
}
