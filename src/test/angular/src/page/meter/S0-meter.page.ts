import {Selector} from 'testcafe';
import {MeterPage} from './meter.page';

export class S0MeterPage extends MeterPage {

  private static gpioInput = Selector('input[formcontrolname="gpio"]');
  private static pinPullResistanceSelect = Selector('select[formcontrolname="pinPullResistance"]');
  private static impulsesPerKwhInput = Selector('input[formcontrolname="impulsesPerKwh"]');
  private static measurementIntervalInput = Selector('input[formcontrolname="measurementInterval"]');

  public static async setGpio(t: TestController, id: number): Promise<TestController> {
    await t.typeText(S0MeterPage.gpioInput, id.toString());
    return t;
  }

  public static async setPinPullResistance(t: TestController, type: string): Promise<TestController> {
    await t
      .click(S0MeterPage.pinPullResistanceSelect)
      .click(S0MeterPage.pinPullResistanceSelect.find('option').withAttribute('value', type));
    return t;
  }

  public static async setImpulsesPerKwh(t: TestController, impulsesPerKwh: number): Promise<TestController> {
    await t.typeText(S0MeterPage.impulsesPerKwhInput, impulsesPerKwh.toString());
    return t;
  }

  public static async setMeasurementInterval(t: TestController, measurementInterval: number): Promise<TestController> {
    await t.typeText(S0MeterPage.measurementIntervalInput, measurementInterval.toString());
    return t;
  }
}
