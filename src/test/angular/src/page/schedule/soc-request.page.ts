import {SocRequest} from '../../../../../main/angular/src/app/schedule/request/soc/soc-request';
import {
  assertInput,
  assertSelectOption,
  inputText,
  selectOption,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';

export class SocRequestPage {

  public static async setSocRequest(t: TestController, socRequest: SocRequest, selectorPrefix: string) {
    await this.setEvId(t, socRequest.evId, selectorPrefix);
    await this.setSoc(t, socRequest.soc, selectorPrefix);
  }
  public static async assertSocRequest(t: TestController, socRequest: SocRequest, selectorPrefix: string, evName: string) {
    await this.assertEvId(t, socRequest.evId, selectorPrefix, evName);
    await this.assertSoc(t, socRequest.soc, selectorPrefix);
  }

  public static async setEvId(t: TestController, evId: number, selectorPrefix: string) {
    await selectOption(t, selectorSelectByFormControlName('evId', selectorPrefix), evId.toString());
  }
  public static async assertEvId(t: TestController, evId: number, selectorPrefix: string, evName: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('evId', selectorPrefix), evName);
  }

  public static async setSoc(t: TestController, soc: number, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('soc', selectorPrefix), soc.toString());
  }
  public static async assertSoc(t: TestController, soc: number, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('soc', selectorPrefix), soc.toString());
  }
}
