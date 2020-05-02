import {HttpWrite} from '../../../../../main/angular/src/app/http/write/http-write';
import {assertInput, clickButton, inputText, selectorButton, selectorInputByFormControlName} from '../../shared/form';
import {HttpWriteValuePage} from './http-write-value.page';

export class HttpWritePage {

  private static selectorBase(httpWriteIndex: number) {
    return `*[formarrayname="httpWrites"] > app-http-write:nth-child(${httpWriteIndex + 1})`;
  }

  public static async setHttpWrite(t: TestController, httpWrite: HttpWrite, httpWriteIndex: number, selectorPrefix?: string) {
    HttpWritePage.setUrl(t, httpWrite.url, httpWriteIndex, selectorPrefix);
    for (let i = 0; i < httpWrite.writeValues.length; i++) {
      const httpWriteValueSelectorPrefix = `${selectorPrefix} ${HttpWritePage.selectorBase(httpWriteIndex)}`;
      await HttpWriteValuePage.setHttpWriteValue(t, httpWrite.writeValues[i], i, httpWriteValueSelectorPrefix);
    }
  }
  public static async assertHttpWrite(t: TestController, httpWrite: HttpWrite, httpWriteIndex: number, selectorPrefix?: string,
                                      i18nPrefix?: string) {
    HttpWritePage.assertUrl(t, httpWrite.url, httpWriteIndex, selectorPrefix);
    for (let i = 0; i < httpWrite.writeValues.length; i++) {
      const httpWriteValueSelectorPrefix = `${selectorPrefix} ${HttpWritePage.selectorBase(httpWriteIndex)}`;
      await HttpWriteValuePage.assertHttpWriteValue(t, httpWrite.writeValues[i], i, httpWriteValueSelectorPrefix, i18nPrefix);
    }
  }

  public static async setUrl(t: TestController, url: string, httpWriteIndex: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpWritePage.selectorBase(httpWriteIndex)), url);
  }
  public static async assertUrl(t: TestController, url: string, httpWriteIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('url', selectorPrefix,
      HttpWritePage.selectorBase(httpWriteIndex)), url);
  }

  public static async clickAddHttpWrite(t: TestController, selectorPrefix?: string, buttonClass?: string) {
    await clickButton(t, selectorButton(selectorPrefix, buttonClass));
  }
}
