import {StartingCurrentSwitch} from '../../../../../main/angular/src/app/control/startingcurrent/starting-current-switch';
import {
  assertCheckbox,
  assertInput,
  inputText,
  selectorCheckboxByFormControlName, selectorCheckboxCheckedByFormControlName,
  selectorInputByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';

export class StartingCurrentSwitchPage {

  public static async setStartingCurrentSwitch(t: TestController, startingCurrentDetection: boolean,
                                               startingCurrentSwitch: StartingCurrentSwitch) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('startingCurrentDetection'), startingCurrentDetection);
    await StartingCurrentSwitchPage.setPowerThreshold(t, startingCurrentSwitch.powerThreshold);
    await StartingCurrentSwitchPage.setStartingCurrentDetectionDuration(t, startingCurrentSwitch.startingCurrentDetectionDuration);
    await StartingCurrentSwitchPage.setFinishedCurrentDetectionDuration(t, startingCurrentSwitch.finishedCurrentDetectionDuration);
    await StartingCurrentSwitchPage.setMinRunningTime(t, startingCurrentSwitch.minRunningTime);
  }

  public static async assertStartingCurrentSwitch(t: TestController, startingCurrentDetection: boolean,
                                                  startingCurrentSwitch: StartingCurrentSwitch) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('startingCurrentDetection'), startingCurrentDetection);
    await StartingCurrentSwitchPage.assertPowerThreshold(t, startingCurrentSwitch.powerThreshold);
    await StartingCurrentSwitchPage.assertStartingCurrentDetectionDuration(t, startingCurrentSwitch.startingCurrentDetectionDuration);
    await StartingCurrentSwitchPage.assertFinishedCurrentDetectionDuration(t, startingCurrentSwitch.finishedCurrentDetectionDuration);
    await StartingCurrentSwitchPage.assertMinRunningTime(t, startingCurrentSwitch.minRunningTime);
  }

  public static async setPowerThreshold(t: TestController, powerThreshold: number) {
    await inputText(t, selectorInputByFormControlName('powerThreshold'), powerThreshold && powerThreshold.toString());
  }
  public static async assertPowerThreshold(t: TestController, powerThreshold: number) {
    await assertInput(t, selectorInputByFormControlName('powerThreshold'), powerThreshold && powerThreshold.toString());
  }

  public static async setStartingCurrentDetectionDuration(t: TestController, startingCurrentDetectionDuration: number) {
    await inputText(t, selectorInputByFormControlName('startingCurrentDetectionDuration'),
      startingCurrentDetectionDuration && startingCurrentDetectionDuration.toString());
  }
  public static async assertStartingCurrentDetectionDuration(t: TestController, startingCurrentDetectionDuration: number) {
    await assertInput(t, selectorInputByFormControlName('startingCurrentDetectionDuration'),
      startingCurrentDetectionDuration && startingCurrentDetectionDuration.toString());
  }

  public static async setFinishedCurrentDetectionDuration(t: TestController, finishedCurrentDetectionDuration: number) {
    await inputText(t, selectorInputByFormControlName('finishedCurrentDetectionDuration'),
      finishedCurrentDetectionDuration && finishedCurrentDetectionDuration.toString());
  }
  public static async assertFinishedCurrentDetectionDuration(t: TestController, finishedCurrentDetectionDuration: number) {
    await assertInput(t, selectorInputByFormControlName('finishedCurrentDetectionDuration'),
      finishedCurrentDetectionDuration && finishedCurrentDetectionDuration.toString());
  }

  public static async setMinRunningTime(t: TestController, minRunningTime: number) {
    await inputText(t, selectorInputByFormControlName('minRunningTime'), minRunningTime && minRunningTime.toString());
  }
  public static async assertMinRunningTime(t: TestController, minRunningTime: number) {
    await assertInput(t, selectorInputByFormControlName('minRunningTime'), minRunningTime && minRunningTime.toString());
  }
}
