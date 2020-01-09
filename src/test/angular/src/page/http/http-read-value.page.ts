import {assertInput, assertSelect, getIndexedSelectOptionValueRegExp, inputText, selectOptionByAttribute} from '../../shared/form';
import {HttpReadValue} from '../../../../../main/angular/src/app/http-read-value/http-read-value';
import {Selector} from 'testcafe';

export class HttpReadValuePage {

  private static selectorBase(httpReadValueIndex: number) {
    return `app-http-read-value:nth-child(${httpReadValueIndex + 1})`;
  }

  private static selectorInputByFormControlName(httpReadValueIndex: number, selectorPrefix: string | undefined, formcontrolname: string) {
    return HttpReadValuePage.selectorByFormControlName(httpReadValueIndex, selectorPrefix, 'input', formcontrolname);
  }

  private static selectorSelectByFormControlName(httpReadValueIndex: number, selectorPrefix: string | undefined, formcontrolname: string) {
    return HttpReadValuePage.selectorByFormControlName(httpReadValueIndex, selectorPrefix, 'select', formcontrolname);
  }

  private static selectorByFormControlName(httpReadValueIndex: number, selectorPrefix: string | undefined,
                                           formcontrolType: string, formControlName: string) {
    // tslint:disable-next-line:max-line-length
    return Selector(`${selectorPrefix || ''} ${HttpReadValuePage.selectorBase(httpReadValueIndex)} ${formcontrolType}[formcontrolname="${formControlName}"]`);
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
                              selectorPrefix?: string): Promise<TestController> {
    await selectOptionByAttribute(t, HttpReadValuePage.selectorSelectByFormControlName(httpReadValueIndex,
      selectorPrefix, 'name'), name, true);
    return t;
  }

  public static async assertName(t: TestController, name: string, httpReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertSelect(t, HttpReadValuePage.selectorSelectByFormControlName(httpReadValueIndex, selectorPrefix, 'name'),
      getIndexedSelectOptionValueRegExp(name));
  }

  public static async setData(t: TestController, data: string, httpReadValueIndex: number,
                              selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'data'), data);
    return t;
  }

  public static async assertData(t: TestController, data: string, httpReadValueIndex: number,
                                 selectorPrefix?: string): Promise<TestController> {
    await assertInput(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'data'), data);
    return t;
  }

  public static async setPath(t: TestController, path: string, httpReadValueIndex: number,
                              selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'path'), path);
    return t;
  }

  public static async assertPath(t: TestController, path: string, httpReadValueIndex: number,
                                 selectorPrefix?: string): Promise<TestController> {
    await assertInput(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'path'), path);
    return t;
  }

  public static async setExtractionRegex(t: TestController, extractionRegex: string, httpReadValueIndex: number,
                                         selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'extractionRegex'),
      extractionRegex);
    return t;
  }

  public static async assertExtractionRegex(t: TestController, extractionRegex: string, httpReadValueIndex: number,
                                            selectorPrefix?: string): Promise<TestController> {
    await assertInput(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'extractionRegex'),
      extractionRegex);
    return t;
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, httpReadValueIndex: number,
                                       selectorPrefix?: string): Promise<TestController> {
    await inputText(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'factorToValue'),
      factorToValue && factorToValue.toString());
    return t;
  }

  public static async assertFactorToValue(t: TestController, factorToValue: number, httpReadValueIndex: number,
                                          selectorPrefix?: string): Promise<TestController> {
    await assertInput(t, HttpReadValuePage.selectorInputByFormControlName(httpReadValueIndex, selectorPrefix, 'factorToValue'),
      factorToValue && factorToValue.toString());
    return t;
  }
}
