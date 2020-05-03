import {clickButton, selectorButton} from '../../shared/form';
import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {SchedulePage} from './schedule.page';

export class SchedulesPage {

  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';

  public static async setSchedules(t: TestController, schedules: Schedule[]) {
    for (let i = 0; i < schedules.length; i++) {
      await this.clickAddSchedule(t);
      await SchedulePage.setSchedule(t, schedules[i], i);
    }
    await this.clickSave(t);
  }
  public static async assertSchedules(t: TestController, schedules: Schedule[]) {
    for (let i = 0; i < schedules.length; i++) {
      await SchedulePage.assertSchedule(t, schedules[i], i);
    }
  }

  public static async clickAddSchedule(t: TestController) {
    await clickButton(t, selectorButton(undefined, 'SchedulesComponent__addSchedule'));
  }

  public static async clickSave(t: TestController) {
    await clickButton(t, SchedulesPage.SAVE_BUTTON_SELECTOR);
  }
}
