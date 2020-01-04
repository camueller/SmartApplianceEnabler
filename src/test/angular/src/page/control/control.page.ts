import {Selector} from 'testcafe';
import {selectOptionByAttribute} from '../../shared/form';

export class ControlPage {

  private static typeSelect = Selector('select[formcontrolname="controlType"]');
  private static saveButton = Selector('button[type="submit"]');

  public static async setType(t: TestController, type: string): Promise<TestController> {
    await selectOptionByAttribute(t, ControlPage.typeSelect, type);
    return t;
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(ControlPage.saveButton);
    return t;
  }
}
