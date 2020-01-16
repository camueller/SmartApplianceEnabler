import {
  assertInput,
  assertSelect,
  getIndexedSelectOptionValueRegExp,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName
} from '../../shared/form';
import {HttpReadValue} from '../../../../../main/angular/src/app/http-read-value/http-read-value';

export class HttpReadValuePage {

  private static selectorBase(httpReadValueIndex: number) {
    return `app-http-read-value:nth-child(${httpReadValueIndex + 1})`;
  }

  public static async setHttpReadValue(t: TestController, httpReadValue: HttpReadValue,
                                       httpReadValueIndex: number, selectorPrefix?: string) {
    await HttpReadValuePage.setName(t, httpReadValue.name, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.setData(t, httpReadValue.data, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.setPath(t, httpReadValue.path, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.setExtractionRegex(t, httpReadValue.extractionRegex, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.setFactorToValue(t, httpReadValue.factorToValue, httpReadValueIndex, selectorPrefix);
  }

  public static async assertHttpReadValue(t: TestController, httpReadValue: HttpReadValue,
                                          httpReadValueIndex: number, selectorPrefix?: string) {
    await HttpReadValuePage.assertName(t, httpReadValue.name, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.assertData(t, httpReadValue.data, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.assertPath(t, httpReadValue.path, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.assertExtractionRegex(t, httpReadValue.extractionRegex, httpReadValueIndex, selectorPrefix);
    await HttpReadValuePage.assertFactorToValue(t, httpReadValue.factorToValue, httpReadValueIndex, selectorPrefix);
  }

  public static async setName(t: TestController, name: string, httpReadValueIndex: number,
                              selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('name', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), name, true);
  }
  public static async assertName(t: TestController, name: string, httpReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('name', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), getIndexedSelectOptionValueRegExp(name));
  }

  public static async setData(t: TestController, data: string, httpReadValueIndex: number,
                              selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('data', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), data);
  }

  public static async assertData(t: TestController, data: string, httpReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('data', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), data);
  }

  public static async setPath(t: TestController, path: string, httpReadValueIndex: number,
                              selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('path', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), path);
  }

  public static async assertPath(t: TestController, path: string, httpReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('path', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), path);
  }

  public static async setExtractionRegex(t: TestController, extractionRegex: string, httpReadValueIndex: number,
                                         selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('extractionRegex', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), extractionRegex);
  }

  public static async assertExtractionRegex(t: TestController, extractionRegex: string, httpReadValueIndex: number,
                                            selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('extractionRegex', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), extractionRegex);
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, httpReadValueIndex: number,
                                       selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), factorToValue && factorToValue.toString());
  }

  public static async assertFactorToValue(t: TestController, factorToValue: number, httpReadValueIndex: number,
                                          selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      HttpReadValuePage.selectorBase(httpReadValueIndex)), factorToValue && factorToValue.toString());
  }
}
