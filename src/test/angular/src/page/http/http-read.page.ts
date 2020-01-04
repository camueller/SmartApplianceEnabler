import {HttpRead} from '../../../../../main/angular/src/app/http-read/http-read';
import {inputText} from '../../shared/form';
import {Selector} from 'testcafe';
import {HttpReadValue} from '../../../../../main/angular/src/app/http-read-value/http-read-value';
import {HttpReadValuePage} from './http-read-value.page';

export class HttpReadPage {

  private static selectorBase(httpReadIndex: number) {
    return `app-http-read:nth-child(${httpReadIndex + 1})`;
  }

  private static addHttpReadButton(selectorPrefix?: string) {
    return Selector(`${selectorPrefix || ''}> div > button`);
  }

  private static urlInput(httpReadIndex: number, selectorPrefix?: string) {
    return Selector(`${selectorPrefix || ''} ${HttpReadPage.selectorBase(httpReadIndex)} input[formcontrolname="url"]`);
  }

  public static async setHttpRead(t: TestController, httpRead: HttpRead, httpReadIndex: number, selectorPrefix?: string) {
    HttpReadPage.setUrl(t, httpRead.url, httpReadIndex, selectorPrefix);
  }

  public static async setHttpReadValue(t: TestController, httpReadValue: HttpReadValue, httpReadIndex: number, selectorPrefix?: string) {
    const httpReadValueSelectorPrefix = `${selectorPrefix} ${HttpReadPage.selectorBase(httpReadIndex)}`;
    await HttpReadValuePage.setHttpReadValue(t, httpReadValue, 0, httpReadValueSelectorPrefix);
  }

  public static async setUrl(t: TestController, url: string, httpReadIndex: number, selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpReadPage.urlInput(httpReadIndex, selectorPrefix), url);
    return t;
  }

  public static async clickAddHttpRead(t: TestController, selectorPrefix?: string): Promise<TestController> {
    await t.click(HttpReadPage.addHttpReadButton(selectorPrefix));
    return t;
  }
}
