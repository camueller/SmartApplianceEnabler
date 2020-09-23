import {ControlPage} from './control.page';
import {PartnerSwitch} from '../../../../../main/angular/src/app/control/partner/partner-switch';

export class PartnerSwitchPage extends ControlPage {

  public static async setPartnerSwitch(t: TestController, partnerSwitch: PartnerSwitch) {
    await PartnerSwitchPage.setType(t, PartnerSwitch.TYPE);
  }
  public static async assertPartnerSwitch(t: TestController, partnerSwitch: PartnerSwitch) {
    await PartnerSwitchPage.assertType(t, PartnerSwitch.TYPE);
  }
}
