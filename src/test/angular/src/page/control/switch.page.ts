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

  public static async setGpio(t: TestController, gpio: number) {
    await inputText(t, selectorInputByFormControlName('gpio'), gpio && gpio.toString());
  }
  public static async assertGpio(t: TestController, gpio: number) {
    await assertInput(t, selectorInputByFormControlName('gpio'), gpio && gpio.toString());
  }

  public static async setReverseStates(t: TestController, reverseStates: boolean) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('reverseStates'), reverseStates);
  }
  public static async assertReverseStates(t: TestController, reverseStates: boolean) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('reverseStates'), reverseStates);
  }
}
