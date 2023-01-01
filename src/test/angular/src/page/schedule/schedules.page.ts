import {clickButton, selectorButton} from '../../shared/form';
import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {SchedulePage} from './schedule.page';
import {Selector} from 'testcafe';

export class SchedulesPage {

  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';

  public static pageSelector(t: TestController): Selector {
    return Selector('form.SchedulesComponent');
  }

  public static async waitForPage(t: TestController): Promise<void> {
    await t.expect(await this.pageExists(t)).ok();
  }

  public static async pageExists(t: TestController): Promise<boolean> {
    return (await this.pageSelector(t)).exists;
  }

  public static async setSchedules(t: TestController, schedules: Schedule[]) {
    await this.waitForPage(t);
    for (let i = 0; i < schedules.length; i++) {
      await this.clickAddSchedule(t);
      await SchedulePage.setSchedule(t, schedules[i], i);
    }
  }
  public static async assertSchedules(t: TestController, schedules: Schedule[], evName?: string) {
    await this.waitForPage(t);
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
