import {RuntimeRequest} from '../../../../../main/angular/src/app/schedule/request/runtime/runtime-request';
import {assertInput, inputText, selectorInputByFormControlName} from '../../shared/form';
import {TimeUtil} from '../../../../../main/angular/src/app/shared/time-util';

export class RuntimeRequestPage {

  public static async setRuntimeRequest(t: TestController, runtimeRequest: RuntimeRequest, selectorPrefix: string) {
    if (runtimeRequest.min) {
      await this.setMinRuntime(t, runtimeRequest.min.toString(), selectorPrefix);
    }
    await this.setMaxRuntime(t, runtimeRequest.max && TimeUtil.toHourMinute(runtimeRequest.max), selectorPrefix);
  }
  public static async assertRuntimeRequest(t: TestController, runtimeRequest: RuntimeRequest, selectorPrefix: string) {
    if (runtimeRequest.min) {
      await this.assertMinRuntime(t, runtimeRequest.min.toString(), selectorPrefix);
    }
    await this.assertMaxRuntime(t, runtimeRequest.max && TimeUtil.toHourMinute(runtimeRequest.max), selectorPrefix);
  }

  public static async setMinRuntime(t: TestController, minRuntime: string, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('minRuntime', selectorPrefix), minRuntime);
    await t.pressKey('esc'); // close multi select overlay

  }
  public static async assertMinRuntime(t: TestController, minRuntime: string, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('minRuntime', selectorPrefix), minRuntime);
  }

  public static async setMaxRuntime(t: TestController, maxRuntime: string, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('maxRuntime', selectorPrefix), maxRuntime);
    await t.pressKey('esc'); // close multi select overlay
  }
  public static async assertMaxRuntime(t: TestController, maxRuntime: string, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('maxRuntime', selectorPrefix), maxRuntime);
  }
}
