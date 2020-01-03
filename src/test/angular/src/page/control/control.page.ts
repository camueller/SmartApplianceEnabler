import {Selector} from 'testcafe';
import {Switch} from '../../../../../main/angular/src/app/control-switch/switch';

export class ControlPage {

  private static typeSelect = Selector('select[formcontrolname="controlType"]');
  private static saveButton = Selector('button[type="submit"]');

  public static async setTypeSwitch(t: TestController) {
    return ControlPage.setType(t, Switch.TYPE);
  }

  private static async setType(t: TestController, type: string): Promise<TestController> {
    const pattern = `...${type.substr(0, 47)}`; // <option value="0: de.avanux.smartapplianceenabler.meter.S0Electri" ...
    await t
      .click(ControlPage.typeSelect)
      .click(ControlPage.typeSelect.find('option').withAttribute('value', new RegExp(pattern)));
    return t;
  }

  public static async clickSave(t: TestController): Promise<TestController> {
    await t.click(ControlPage.saveButton);
    return t;
  }
}
