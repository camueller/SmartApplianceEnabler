import {ControlPage} from './control.page';
import {AlwaysOnSwitch} from '../../../../../main/angular/src/app/control/alwayson/always-on-switch';

export class AlwaysOnSwitchPage extends ControlPage {

  public static async setAlwaysOnSwitch(t: TestController, alwaysOnSwitch: AlwaysOnSwitch) {
    await AlwaysOnSwitchPage.setType(t, AlwaysOnSwitch.TYPE);
  }
  public static async assertAlwaysOnSwitch(t: TestController, alwaysOnSwitch: AlwaysOnSwitch) {
    await AlwaysOnSwitchPage.assertType(t, AlwaysOnSwitch.TYPE);
  }
}
