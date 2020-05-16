import {EnergyRequest} from '../../../../../main/angular/src/app/schedule/request/energy/energy-request';
import {assertInput, inputText, selectorInputByFormControlName} from '../../shared/form';

export class EnergyRequestPage {

  public static async setEnergyRequest(t: TestController, runtimeRequest: EnergyRequest, selectorPrefix: string) {
    if (runtimeRequest.min) {
      await this.setMinEnergy(t, runtimeRequest.min.toString(), selectorPrefix);
    }
    await this.setMaxEnergy(t, runtimeRequest.max.toString(), selectorPrefix);
  }
  public static async assertEnergyRequest(t: TestController, runtimeRequest: EnergyRequest, selectorPrefix: string) {
    if (runtimeRequest.min) {
      await this.assertMinEnergy(t, runtimeRequest.min.toString(), selectorPrefix);
    }
    await this.assertMaxEnergy(t, runtimeRequest.max.toString(), selectorPrefix);
  }

  public static async setMinEnergy(t: TestController, minEnergy: string, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('minEnergy', selectorPrefix), minEnergy);
  }
  public static async assertMinEnergy(t: TestController, minEnergy: string, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('minEnergy', selectorPrefix), minEnergy);
  }

  public static async setMaxEnergy(t: TestController, maxEnergy: string, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('maxEnergy', selectorPrefix), maxEnergy);
  }
  public static async assertMaxEnergy(t: TestController, maxEnergy: string, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('maxEnergy', selectorPrefix), maxEnergy);
  }
}
