import {MeterPage} from './meter.page';
import {S0ElectricityMeter} from '../../../../../main/angular/src/app/meter/s0/s0-electricity-meter';
import {
  assertInput,
  assertSelectOption,
  inputText,
  selectOption,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';

export class S0MeterPage extends MeterPage {

  public static async setS0ElectricityMeter(t: TestController, s0ElectricityMeter: S0ElectricityMeter) {
    await S0MeterPage.setType(t, S0ElectricityMeter.TYPE);
    await S0MeterPage.setGpio(t, s0ElectricityMeter.gpio);
    await S0MeterPage.setPinPullResistance(t, s0ElectricityMeter.pinPullResistance);
    await S0MeterPage.setImpulsesPerKwh(t, s0ElectricityMeter.impulsesPerKwh);
    await S0MeterPage.setMinPulseDuration(t, s0ElectricityMeter.minPulseDuration);
  }
  public static async assertS0ElectricityMeter(t: TestController, s0ElectricityMeter: S0ElectricityMeter) {
    await S0MeterPage.assertType(t, S0ElectricityMeter.TYPE);
    await S0MeterPage.assertGpio(t, s0ElectricityMeter.gpio);
    await S0MeterPage.assertPinPullResistance(t, s0ElectricityMeter.pinPullResistance);
    await S0MeterPage.assertImpulsesPerKwh(t, s0ElectricityMeter.impulsesPerKwh);
    await S0MeterPage.assertMinPulseDuration(t, s0ElectricityMeter.minPulseDuration);
  }

  public static async setGpio(t: TestController, gpio: number) {
    await inputText(t, selectorInputByFormControlName('gpio'), gpio && gpio.toString());
  }
  public static async assertGpio(t: TestController, gpio: number) {
    await assertInput(t, selectorInputByFormControlName('gpio'), gpio && gpio.toString());
  }

  public static async setPinPullResistance(t: TestController, pinPullResistance: string) {
    await selectOption(t, selectorSelectByFormControlName('pinPullResistance'), pinPullResistance);
  }
  public static async assertPinPullResistance(t: TestController, pinPullResistance: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('pinPullResistance'), pinPullResistance,
      'MeterS0Component.pinPullResistance.');
  }

  public static async setImpulsesPerKwh(t: TestController, impulsesPerKwh: number) {
    await inputText(t, selectorInputByFormControlName('impulsesPerKwh'), impulsesPerKwh && impulsesPerKwh.toString());
  }
  public static async assertImpulsesPerKwh(t: TestController, impulsesPerKwh: number) {
    await assertInput(t, selectorInputByFormControlName('impulsesPerKwh'), impulsesPerKwh && impulsesPerKwh.toString());
  }

  public static async setMinPulseDuration(t: TestController, minPulseDuration: number) {
    await inputText(t, selectorInputByFormControlName('minPulseDuration'), minPulseDuration && minPulseDuration.toString());
  }
  public static async assertMinPulseDuration(t: TestController, minPulseDuration: number) {
    await assertInput(t, selectorInputByFormControlName('minPulseDuration'), minPulseDuration && minPulseDuration.toString());
  }
}
