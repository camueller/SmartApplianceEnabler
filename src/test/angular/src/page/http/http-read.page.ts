import {HttpRead} from '../../../../../main/angular/src/app/http/read/http-read';
import {assertInput, inputText, selectorInputByFormControlName} from '../../shared/form';
import {Selector} from 'testcafe';
import {HttpReadValue} from '../../../../../main/angular/src/app/http/read-value/http-read-value';
import {HttpReadValuePage} from './http-read-value.page';

export class HttpReadPage {

  private static selectorBase(httpReadIndex: number) {
    return `app-http-read:nth-child(${httpReadIndex + 1})`;
  }

  private static addHttpReadButton(selectorPrefix?: string) {
    return Selector(`${selectorPrefix || ''}> div > button`);
  }

  public static async setHttpRead(t: TestController, httpRead: HttpRead, httpReadIndex: number, selectorPrefix?: string) {
    HttpReadPage.setUrl(t, httpRead.url, httpReadIndex, selectorPrefix);
  }
  public static async assertHttpRead(t: TestController, httpRead: HttpRead, httpReadIndex: number, selectorPrefix?: string) {
    HttpReadPage.assertUrl(t, httpRead.url, httpReadIndex, selectorPrefix);
  }

  public static async setHttpReadValue(t: TestController, httpReadValue: HttpReadValue, httpReadIndex: number, selectorPrefix?: string) {
    const httpReadValueSelectorPrefix = `${selectorPrefix} ${HttpReadPage.selectorBase(httpReadIndex)}`;
    await HttpReadValuePage.setHttpReadValue(t, httpReadValue, 0, httpReadValueSelectorPrefix);
  }
  public static async assertHttpReadValue(t: TestController, httpReadValue: HttpReadValue, httpReadIndex: number, selectorPrefix?: string) {
    const httpReadValueSelectorPrefix = `${selectorPrefix} ${HttpReadPage.selectorBase(httpReadIndex)}`;
    await HttpReadValuePage.assertHttpReadValue(t, httpReadValue, 0, httpReadValueSelectorPrefix);
  }

  public static async setUrl(t: TestController, url: string, httpReadIndex: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpReadPage.selectorBase(httpReadIndex)), url);
  }
  public static async assertUrl(t: TestController, url: string, httpReadIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpReadPage.selectorBase(httpReadIndex)), url);
  }

  public static async clickAddHttpRead(t: TestController, selectorPrefix?: string) {
    await t.click(HttpReadPage.addHttpReadButton(selectorPrefix));
  }
}
