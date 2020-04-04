import {HttpWriteValue} from '../../../../../main/angular/src/app/http/http-write-value/http-write-value';
import {
  assertInput,
  assertSelect,
  getIndexedSelectOptionValueRegExp,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName
} from '../../shared/form';

export class HttpWriteValuePage {

  private static selectorBase(httpWriteValueIndex: number) {
    return `app-http-write-value:nth-child(${httpWriteValueIndex + 1})`;
  }

  public static async setHttpWriteValue(t: TestController, httpWriteValue: HttpWriteValue,
                                       httpWriteValueIndex: number, selectorPrefix?: string) {
    await HttpWriteValuePage.setName(t, httpWriteValue.name, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.setValue(t, httpWriteValue.value, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.setMethod(t, httpWriteValue.method, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.setFactorToValue(t, httpWriteValue.factorToValue, httpWriteValueIndex, selectorPrefix);
  }

  public static async assertHttpWriteValue(t: TestController, httpWriteValue: HttpWriteValue,
                                          httpWriteValueIndex: number, selectorPrefix?: string) {
    await HttpWriteValuePage.assertName(t, httpWriteValue.name, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.assertValue(t, httpWriteValue.value, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.assertMethod(t, httpWriteValue.method, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.assertFactorToValue(t, httpWriteValue.factorToValue, httpWriteValueIndex, selectorPrefix);
  }

  public static async setName(t: TestController, name: string, httpReadValueIndex: number, selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('name', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpReadValueIndex)), name, true);
  }
  public static async assertName(t: TestController, name: string, httpReadValueIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('name', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpReadValueIndex)), getIndexedSelectOptionValueRegExp(name));
  }

  public static async setValue(t: TestController, value: string, httpWriteValueIndex: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('value', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), value);
  }
  public static async assertValue(t: TestController, value: string, httpWriteValueIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('value', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), value);
  }

  public static async setMethod(t: TestController, method: string, httpWriteValueIndex: number, selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('method', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), method, true);
  }
  public static async assertMethod(t: TestController, method: string, httpWriteValueIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('method', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), getIndexedSelectOptionValueRegExp(method));
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, httpWriteValueIndex: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), factorToValue && factorToValue.toString());
  }
  public static async assertFactorToValue(t: TestController, factorToValue: number, httpWriteValueIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), factorToValue && factorToValue.toString());
  }
}
