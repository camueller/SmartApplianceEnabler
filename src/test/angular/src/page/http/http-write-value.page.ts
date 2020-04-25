import {HttpWriteValue} from '../../../../../main/angular/src/app/http/write-value/http-write-value';
import {
  assertInput,
  assertSelect,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
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
                                          httpWriteValueIndex: number, selectorPrefix?: string, i18nPrefix?: string) {
    await HttpWriteValuePage.assertName(t, httpWriteValue.name, httpWriteValueIndex, selectorPrefix, i18nPrefix);
    await HttpWriteValuePage.assertValue(t, httpWriteValue.value, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.assertMethod(t, httpWriteValue.method, httpWriteValueIndex, selectorPrefix);
    await HttpWriteValuePage.assertFactorToValue(t, httpWriteValue.factorToValue, httpWriteValueIndex, selectorPrefix);
  }

  public static async setName(t: TestController, name: string, httpWriteValueIndex: number, selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('name', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), name);
  }
  public static async assertName(t: TestController, name: string, httpWriteValueIndex: number, selectorPrefix?: string,
                                 i18nPrefix?: string) {
    await assertSelect(t, selectorSelectedByFormControlName('name', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), name, i18nPrefix);
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
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), method);
  }
  public static async assertMethod(t: TestController, method: string, httpWriteValueIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectedByFormControlName('method', selectorPrefix,
      HttpWriteValuePage.selectorBase(httpWriteValueIndex)), method);
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
