import {HttpRead} from '../../../../../main/angular/src/app/http/read/http-read';
import {assertInput, clickButton, inputText, selectorButton, selectorInputByFormControlName} from '../../shared/form';
import {HttpReadValuePage} from './http-read-value.page';

export class HttpReadPage {

  private static selectorBase(httpReadIndex?: number) {
    if (httpReadIndex) {
      return `*[formarrayname="httpReads"] > app-http-read:nth-child(${httpReadIndex + 1})`;
    }
    return `app-http-read`;
  }

  public static async setHttpRead(t: TestController, httpRead: HttpRead, httpReadIndex?: number, selectorPrefix?: string) {
    HttpReadPage.setUrl(t, httpRead.url, httpReadIndex, selectorPrefix);
    for (let i = 0; i < httpRead.readValues.length; i++) {
      const httpReadValueSelectorPrefix = `${selectorPrefix || ''} ${HttpReadPage.selectorBase(httpReadIndex)}`;
      await HttpReadValuePage.setHttpReadValue(t, httpRead.readValues[i], i, httpReadValueSelectorPrefix);
    }
  }
  public static async assertHttpRead(t: TestController, httpRead: HttpRead, httpReadIndex: number, factorToValue: boolean,
                                     selectorPrefix?: string, i18nPrefix?: string) {
    HttpReadPage.assertUrl(t, httpRead.url, httpReadIndex, selectorPrefix);
    for (let i = 0; i < httpRead.readValues.length; i++) {
      const httpReadValueSelectorPrefix = `${selectorPrefix || ''} ${HttpReadPage.selectorBase(httpReadIndex)}`;
      await HttpReadValuePage.assertHttpReadValue(t, httpRead.readValues[i], i, factorToValue, httpReadValueSelectorPrefix, i18nPrefix);
    }
  }

  public static async setUrl(t: TestController, url: string, httpReadIndex?: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpReadPage.selectorBase(httpReadIndex)), url);
  }
  public static async assertUrl(t: TestController, url: string, httpReadIndex?: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpReadPage.selectorBase(httpReadIndex)), url);
  }

  public static async clickAddHttpRead(t: TestController, selectorPrefix?: string, buttonClass?: string) {
    await clickButton(t, selectorButton(selectorPrefix, buttonClass));
  }
}
