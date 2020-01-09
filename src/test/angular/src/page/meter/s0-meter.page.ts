import {Selector} from 'testcafe';
import {MeterPage} from './meter.page';
import {S0ElectricityMeter} from '../../../../../main/angular/src/app/meter-s0/s0-electricity-meter';
import {assertInput, assertSelect, inputText, selectOptionByAttribute} from '../../shared/form';

export class S0MeterPage extends MeterPage {

  private static gpioInput = Selector('input[formcontrolname="gpio"]');
  private static pinPullResistanceSelect = Selector('select[formcontrolname="pinPullResistance"]');
  private static impulsesPerKwhInput = Selector('input[formcontrolname="impulsesPerKwh"]');
  private static measurementIntervalInput = Selector('input[formcontrolname="measurementInterval"]');

  public static async setS0ElectricityMeter(t: TestController, s0ElectricityMeter: S0ElectricityMeter) {
    await S0MeterPage.setType(t, S0ElectricityMeter.TYPE);
    await S0MeterPage.setGpio(t, s0ElectricityMeter.gpio);
    await S0MeterPage.setPinPullResistance(t, s0ElectricityMeter.pinPullResistance);
    await S0MeterPage.setImpulsesPerKwh(t, s0ElectricityMeter.impulsesPerKwh);
    await S0MeterPage.setMeasurementInterval(t, s0ElectricityMeter.measurementInterval);
  }
  public static async assertS0ElectricityMeter(t: TestController, s0ElectricityMeter: S0ElectricityMeter) {
    await S0MeterPage.assertType(t, S0ElectricityMeter.TYPE);
    await S0MeterPage.assertGpio(t, s0ElectricityMeter.gpio);
    await S0MeterPage.assertPinPullResistance(t, s0ElectricityMeter.pinPullResistance);
    await S0MeterPage.assertImpulsesPerKwh(t, s0ElectricityMeter.impulsesPerKwh);
    await S0MeterPage.assertMeasurementInterval(t, s0ElectricityMeter.measurementInterval);
  }

  public static async setGpio(t: TestController, gpio: number): Promise<TestController> {
    await inputText(t, S0MeterPage.gpioInput, gpio && gpio.toString());
    return t;
  }
  public static async assertGpio(t: TestController, gpio: number): Promise<TestController> {
    await assertInput(t, S0MeterPage.gpioInput, gpio && gpio.toString());
    return t;
  }

  public static async setPinPullResistance(t: TestController, type: string): Promise<TestController> {
    await selectOptionByAttribute(t, S0MeterPage.pinPullResistanceSelect, type);
    return t;
  }
  public static async assertPinPullResistance(t: TestController, type: string): Promise<TestController> {
    await assertSelect(t, S0MeterPage.pinPullResistanceSelect, new RegExp(type));
    return t;
  }

  public static async setImpulsesPerKwh(t: TestController, impulsesPerKwh: number): Promise<TestController> {
    await inputText(t, S0MeterPage.impulsesPerKwhInput, impulsesPerKwh && impulsesPerKwh.toString());
    return t;
  }
  public static async assertImpulsesPerKwh(t: TestController, impulsesPerKwh: number): Promise<TestController> {
    await assertInput(t, S0MeterPage.impulsesPerKwhInput, impulsesPerKwh && impulsesPerKwh.toString());
    return t;
  }

  public static async setMeasurementInterval(t: TestController, measurementInterval: number): Promise<TestController> {
    await inputText(t, S0MeterPage.measurementIntervalInput, measurementInterval && measurementInterval.toString());
    return t;
  }
  public static async assertMeasurementInterval(t: TestController, measurementInterval: number): Promise<TestController> {
    await assertInput(t, S0MeterPage.measurementIntervalInput, measurementInterval && measurementInterval.toString());
    return t;
  }
}
