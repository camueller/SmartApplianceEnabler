import {ControlPage} from './control.page';
import {
  assertCheckbox,
  assertInput,
  inputText,
  selectorCheckboxByFormControlName,
  selectorCheckboxCheckedByFormControlName,
  selectorInputByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {Switch} from '../../../../../main/angular/src/app/control/switch/switch';

export class SwitchPage extends ControlPage {

  public static async setSwitch(t: TestController, switch_: Switch, selectorPrefix = 'app-control-switch', setType = true) {
    if(setType) {
      await SwitchPage.setType(t, Switch.TYPE);
    }
    await SwitchPage.setGpio(t, switch_.gpio, selectorPrefix);
    await SwitchPage.setReverseStates(t, switch_.reverseStates, selectorPrefix);
  }
  public static async assertSwitch(t: TestController, switch_: Switch, selectorPrefix = 'app-control-switch', setType = true) {
    if(setType) {
      await SwitchPage.assertType(t, Switch.TYPE);
    }
    await SwitchPage.assertGpio(t, switch_.gpio, selectorPrefix);
    await SwitchPage.assertReverseStates(t, switch_.reverseStates, selectorPrefix);
  }

  public static async setGpio(t: TestController, gpio: number, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('gpio', selectorPrefix), gpio && gpio.toString());
  }
  public static async assertGpio(t: TestController, gpio: number, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('gpio', selectorPrefix), gpio && gpio.toString());
  }

  public static async setReverseStates(t: TestController, reverseStates: boolean, selectorPrefix: string) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('reverseStates', selectorPrefix), reverseStates);
  }
  public static async assertReverseStates(t: TestController, reverseStates: boolean, selectorPrefix: string) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('reverseStates', selectorPrefix), reverseStates);
  }
}
