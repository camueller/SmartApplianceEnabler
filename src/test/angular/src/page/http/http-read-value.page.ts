import {inputText} from '../../shared/form';
import {HttpReadValue} from '../../../../../main/angular/src/app/http-read-value/http-read-value';
import {Selector} from 'testcafe';

export class HttpReadValuePage {

  private static selectorBase(httpReadValueIndex: number) {
    return `app-http-read-value:nth-child(${httpReadValueIndex + 1})`;
  }

  private static extractionRegexInput(httpReadValueIndex: number, selectorPrefix?: string) {
    // tslint:disable-next-line:max-line-length
    return Selector(`${selectorPrefix || ''} ${HttpReadValuePage.selectorBase(httpReadValueIndex)} input[formcontrolname="extractionRegex"]`);
  }

  public static async setHttpReadValue(t: TestController, httpReadValue: HttpReadValue,
                                       httpReadValueIndex: number, selectorPrefix?: string) {
    HttpReadValuePage.setExtractionRegex(t, httpReadValue.extractionRegex, httpReadValueIndex, selectorPrefix);
  }

  public static async setExtractionRegex(t: TestController, extractionRegex: string, httpReadValueIndex: number,
                                         selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpReadValuePage.extractionRegexInput(httpReadValueIndex, selectorPrefix), extractionRegex);
    return t;
  }

}
