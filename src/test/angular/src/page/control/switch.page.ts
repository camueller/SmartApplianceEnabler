import {ControlPage} from './control.page';
import {Selector} from 'testcafe';
import {assertCheckbox, assertInput, inputText, setCheckboxEnabled} from '../../shared/form';
import {Switch} from '../../../../../main/angular/src/app/control-switch/switch';

export class SwitchPage extends ControlPage {

  private static gpioInput = Selector('input[formcontrolname="gpio"]');
  private static reverseStatesInput = Selector('input[formcontrolname="reverseStates"]');

  public static async setSwitch(t: TestController, switch_: Switch) {
    await SwitchPage.setType(t, Switch.TYPE);
    await SwitchPage.setGpio(t, switch_.gpio);
    await SwitchPage.setReverseStates(t, switch_.reverseStates);
  }
  public static async assertSwitch(t: TestController, switch_: Switch) {
    await SwitchPage.assertType(t, Switch.TYPE);
    await SwitchPage.assertGpio(t, switch_.gpio);
    await SwitchPage.assertReverseStates(t, switch_.reverseStates);
  }

  public static async setGpio(t: TestController, gpio: number): Promise<TestController> {
    await inputText(t, SwitchPage.gpioInput, gpio && gpio.toString());
    return t;
  }
  public static async assertGpio(t: TestController, gpio: number): Promise<TestController> {
    await assertInput(t, SwitchPage.gpioInput, gpio && gpio.toString());
    return t;
  }

  public static async setReverseStates(t: TestController, reverseStates: boolean): Promise<TestController> {
    await setCheckboxEnabled(t, SwitchPage.reverseStatesInput, reverseStates);
    return t;
  }
  public static async assertReverseStates(t: TestController, reverseStates: boolean): Promise<TestController> {
    await assertCheckbox(t, SwitchPage.gpioInput, reverseStates);
    return t;
  }
}
