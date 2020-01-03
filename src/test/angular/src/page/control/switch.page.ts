import {ControlPage} from './control.page';
import {Selector} from 'testcafe';
import {setCheckboxEnabled} from '../../shared/checkbox';

export class SwitchPage extends ControlPage {

  private static gpioInput = Selector('input[formcontrolname="gpio"]');
  private static reverseStatesInput = Selector('input[formcontrolname="reverseStates"]');

  public static async setGpio(t: TestController, id: number): Promise<TestController> {
    await t.typeText(SwitchPage.gpioInput, id.toString());
    return t;
  }

  public static async setReverseStates(t: TestController, reverseStates: boolean): Promise<TestController> {
    return setCheckboxEnabled(t, SwitchPage.reverseStatesInput, reverseStates);
  }
}
