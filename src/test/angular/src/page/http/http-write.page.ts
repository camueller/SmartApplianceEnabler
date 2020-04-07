import {HttpWrite} from '../../../../../main/angular/src/app/http/write/http-write';
import {Selector} from 'testcafe';
import {assertInput, inputText, selectorInputByFormControlName} from '../../shared/form';
import {HttpWriteValue} from '../../../../../main/angular/src/app/http/write-value/http-write-value';
import {HttpWriteValuePage} from './http-write-value.page';

export class HttpWritePage {

  private static selectorBase(httpWriteIndex: number) {
    return `app-http-write:nth-child(${httpWriteIndex + 1})`;
  }

  private static addHttpWriteButton(selectorPrefix?: string) {
    return Selector(`${selectorPrefix || ''}> div > button`);
  }

  public static async setHttpWrite(t: TestController, httpWrite: HttpWrite, httpWriteIndex: number, selectorPrefix?: string) {
    HttpWritePage.setUrl(t, httpWrite.url, httpWriteIndex, selectorPrefix);
  }
  public static async assertHttpWrite(t: TestController, httpWrite: HttpWrite, httpWriteIndex: number, selectorPrefix?: string) {
    HttpWritePage.assertUrl(t, httpWrite.url, httpWriteIndex, selectorPrefix);
  }

  public static async setHttpWriteValue(t: TestController, httpWriteValue: HttpWriteValue, httpWriteIndex: number,
                                        selectorPrefix?: string) {
    const httpWriteValueSelectorPrefix = `${selectorPrefix} ${HttpWritePage.selectorBase(httpWriteIndex)}`;
    await HttpWriteValuePage.setHttpWriteValue(t, httpWriteValue, 0, httpWriteValueSelectorPrefix);
  }
  public static async assertHttpWriteValue(t: TestController, httpWriteValue: HttpWriteValue, httpWriteIndex: number,
                                           selectorPrefix?: string) {
    const httpWriteValueSelectorPrefix = `${selectorPrefix} ${HttpWritePage.selectorBase(httpWriteIndex)}`;
    await HttpWriteValuePage.assertHttpWriteValue(t, httpWriteValue, 0, httpWriteValueSelectorPrefix);
  }

  public static async setUrl(t: TestController, url: string, httpWriteIndex: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpWritePage.selectorBase(httpWriteIndex)), url);
  }
  public static async assertUrl(t: TestController, url: string, httpWriteIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpWritePage.selectorBase(httpWriteIndex)), url);
  }

  public static async clickAddHttpWrite(t: TestController, selectorPrefix?: string) {
    await t.click(HttpWritePage.addHttpWriteButton(selectorPrefix));
  }
}
