import {
  assertInput, assertSelectOptionMulti,
  inputText, selectOptionMulti,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {DayTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/day/day-timeframe';
import {isDebug} from '../../shared/helper';
import {getTranslation} from '../../shared/ngx-translate';
import {TimeUtil} from '../../../../../main/angular/src/app/shared/time-util';

export class DayTimeframePage {

  public static async setDayTimeframe(t: TestController, dayTimeframe: DayTimeframe, selectorPrefix: string) {
    await this.setStartTime(t, TimeUtil.timestringFromTimeOfDay(dayTimeframe.start), selectorPrefix);
    await this.setEndTime(t, TimeUtil.timestringFromTimeOfDay(dayTimeframe.end), selectorPrefix);
    await this.setDaysOfWeek(t, dayTimeframe.daysOfWeekValues, selectorPrefix);
  }
  public static async assertDayTimeframe(t: TestController, dayTimeframe: DayTimeframe, selectorPrefix: string) {
    await this.assertStartTime(t, TimeUtil.timestringFromTimeOfDay(dayTimeframe.start), selectorPrefix);
    await this.assertEndTime(t, TimeUtil.timestringFromTimeOfDay(dayTimeframe.end), selectorPrefix);
    await this.assertDaysOfWeek(t, dayTimeframe.daysOfWeekValues, selectorPrefix);
  }


  public static async setStartTime(t: TestController, startTime: string, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('startTime', selectorPrefix), startTime);
    await t.pressKey('esc'); // close multi select overlay
  }
  public static async assertStartTime(t: TestController, startTime: string, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('startTime', selectorPrefix), startTime);
  }

  public static async setEndTime(t: TestController, endTime: string, selectorPrefix: string) {
    await inputText(t, selectorInputByFormControlName('endTime', selectorPrefix), endTime);
    await t.pressKey('esc'); // close multi select overlay
  }
  public static async assertEndTime(t: TestController, endTime: string, selectorPrefix: string) {
    await assertInput(t, selectorInputByFormControlName('endTime', selectorPrefix), endTime);
  }

  public static async setDaysOfWeek(t: TestController, daysOfWeek: number[], selectorPrefix: string) {
    await selectOptionMulti(t, selectorSelectByFormControlName('daysOfWeekValues', selectorPrefix), daysOfWeek);
  }
  public static async assertDaysOfWeek(t: TestController, daysOfWeek: number[], selectorPrefix: string) {
    const dayOfWeekNames = [];
    if (daysOfWeek.includes(1)) {
      dayOfWeekNames.push('monday');
    }
    if (daysOfWeek.includes(2)) {
      dayOfWeekNames.push('tuesday');
    }
    if (daysOfWeek.includes(3)) {
      dayOfWeekNames.push('wednesday');
    }
    if (daysOfWeek.includes(4)) {
      dayOfWeekNames.push('thursday');
    }
    if (daysOfWeek.includes(5)) {
      dayOfWeekNames.push('friday');
    }
    if (daysOfWeek.includes(6)) {
      dayOfWeekNames.push('saturday');
    }
    if (daysOfWeek.includes(7)) {
      dayOfWeekNames.push('sunday');
    }
    if (daysOfWeek.includes(8)) {
      dayOfWeekNames.push('holiday');
    }
    await assertSelectOptionMulti(t, selectorSelectByFormControlName('daysOfWeekValues', selectorPrefix), dayOfWeekNames, 'daysOfWeek_');
  }
}
