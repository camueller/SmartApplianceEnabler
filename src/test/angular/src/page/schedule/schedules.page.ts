import {clickButton, selectorButton} from '../../shared/form';
import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {SchedulePage} from './schedule.page';
import {Selector} from 'testcafe';
import {saeRestartTimeout} from '../../shared/timeout';

export class SchedulesPage {

  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';

  private static async waitForPage() {
    await Selector('form.SchedulesComponent', {timeout: saeRestartTimeout}).exists;
  }

  public static async setSchedules(t: TestController, schedules: Schedule[]) {
    await this.waitForPage();
    for (let i = 0; i < schedules.length; i++) {
      await this.clickAddSchedule(t);
      await SchedulePage.setSchedule(t, schedules[i], i);
    }
    await this.clickSave(t);
  }
  public static async assertSchedules(t: TestController, schedules: Schedule[], evName?: string) {
    await this.waitForPage();
    for (let i = 0; i < schedules.length; i++) {
      await SchedulePage.assertSchedule(t, schedules[i], i, evName);
    }
  }

  public static async clickAddSchedule(t: TestController) {
    await clickButton(t, selectorButton(undefined, 'SchedulesComponent__addSchedule'));
  }

  public static async clickSave(t: TestController) {
    await clickButton(t, SchedulesPage.SAVE_BUTTON_SELECTOR);
  }
}
