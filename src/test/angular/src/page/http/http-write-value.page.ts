import {Selector} from 'testcafe';
import {HttpWriteValue} from '../../../../../main/angular/src/app/http-write-value/http-write-value';
import {assertInput, assertSelect, getIndexedSelectOptionValueRegExp, inputText, selectOptionByAttribute} from '../../shared/form';

export class HttpWriteValuePage {

  private static selectorBase(httpWriteValueIndex: number) {
    return `app-http-write-value:nth-child(${httpWriteValueIndex + 1})`;
  }

  private static selectorInputByFormControlName(httpWriteValueIndex: number, selectorPrefix: string | undefined, formcontrolname: string) {
    return HttpWriteValuePage.selectorByFormControlName(httpWriteValueIndex, selectorPrefix, 'input', formcontrolname);
  }

  private static selectorSelectByFormControlName(httpWriteValueIndex: number, selectorPrefix: string | undefined, formcontrolname: string) {
    return HttpWriteValuePage.selectorByFormControlName(httpWriteValueIndex, selectorPrefix, 'select', formcontrolname);
  }

  private static selectorByFormControlName(httpWriteValueIndex: number, selectorPrefix: string | undefined,
                                           formcontrolType: string, formControlName: string) {
    // tslint:disable-next-line:max-line-length
    return Selector(`${selectorPrefix || ''} ${HttpWriteValuePage.selectorBase(httpWriteValueIndex)} ${formcontrolType}[formcontrolname="${formControlName}"]`);
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

  public static async setName(t: TestController, name: string, httpReadValueIndex: number,
                              selectorPrefix?: string): Promise<TestController> {
    await selectOptionByAttribute(t, HttpWriteValuePage.selectorSelectByFormControlName(httpReadValueIndex,
      selectorPrefix, 'name'), name, true);
    return t;
  }
  public static async assertName(t: TestController, name: string, httpReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertSelect(t, HttpWriteValuePage.selectorSelectByFormControlName(httpReadValueIndex, selectorPrefix, 'name'),
      getIndexedSelectOptionValueRegExp(name));
  }

  public static async setValue(t: TestController, value: string, httpWriteValueIndex: number,
                               selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpWriteValuePage.selectorInputByFormControlName(httpWriteValueIndex, selectorPrefix, 'value'), value);
    return t;
  }
  public static async assertValue(t: TestController, value: string, httpWriteValueIndex: number,
                                  selectorPrefix?: string) {
    await assertInput(t, HttpWriteValuePage.selectorInputByFormControlName(httpWriteValueIndex, selectorPrefix, 'value'), value);
  }

  public static async setMethod(t: TestController, method: string, httpWriteValueIndex: number,
                               selectorPrefix?: string): Promise<TestController> {
    await selectOptionByAttribute(t, HttpWriteValuePage.selectorSelectByFormControlName(httpWriteValueIndex,
      selectorPrefix, 'method'), method, true);
    return t;
  }
  public static async assertMethod(t: TestController, method: string, httpWriteValueIndex: number,
                                  selectorPrefix?: string) {
    await assertSelect(t, HttpWriteValuePage.selectorSelectByFormControlName(httpWriteValueIndex, selectorPrefix, 'method'),
      getIndexedSelectOptionValueRegExp(method));
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, httpWriteValueIndex: number,
                               selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpWriteValuePage.selectorInputByFormControlName(httpWriteValueIndex, selectorPrefix, 'factorToValue'),
      factorToValue && factorToValue.toString());
    return t;
  }
  public static async assertFactorToValue(t: TestController, factorToValue: number, httpWriteValueIndex: number,
                                  selectorPrefix?: string) {
    await assertInput(t, HttpWriteValuePage.selectorInputByFormControlName(httpWriteValueIndex, selectorPrefix, 'factorToValue'),
      factorToValue && factorToValue.toString());
  }
}
