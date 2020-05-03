import {
  assertInput,
  inputText,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {DayTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/day/day-timeframe';
import {isDebug} from '../../shared/helper';
import {getTranslation} from '../../shared/ngx-translate';

export class DayTimeframePage {

  public static async setDayTimeframe(t: TestController, dayTimeframe: DayTimeframe, selectorPrefix: string) {
    await this.setStartTime(t, dayTimeframe.startTime, selectorPrefix);
    await this.setEndTime(t, dayTimeframe.endTime, selectorPrefix);
    await this.setDaysOfWeek(t, dayTimeframe.daysOfWeekValues, selectorPrefix);
  }
  public static async assertDayTimeframe(t: TestController, dayTimeframe: DayTimeframe, selectorPrefix: string) {
    await this.assertStartTime(t, dayTimeframe.startTime, selectorPrefix);
    await this.assertEndTime(t, dayTimeframe.endTime, selectorPrefix);
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
    await t.click(selectorSelectByFormControlName('daysOfWeekValues', selectorPrefix));
    for (let i = 1; i <= 8; i++) {
      if (daysOfWeek.includes(i)) {
        const selectorString = `mat-option:nth-child(${i}) mat-pseudo-checkbox`;
        if (isDebug()) {
          console.log('Selector: ', selectorString);
        }
        await t.click(selectorString);
      }
    }
    await t.pressKey('esc'); // close multi select overlay
  }
  public static async assertDaysOfWeek(t: TestController, daysOfWeek: number[], selectorPrefix: string) {
    const actualDaysOfWeek = await selectorSelectedByFormControlName('daysOfWeekValues', selectorPrefix).innerText;
    const expectedDaysOfWeek = [];
    if (daysOfWeek.includes(1)) {
      expectedDaysOfWeek.push(getTranslation('monday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(2)) {
      expectedDaysOfWeek.push(getTranslation('tuesday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(3)) {
      expectedDaysOfWeek.push(getTranslation('wednesday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(4)) {
      expectedDaysOfWeek.push(getTranslation('thursday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(5)) {
      expectedDaysOfWeek.push(getTranslation('friday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(6)) {
      expectedDaysOfWeek.push(getTranslation('saturday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(7)) {
      expectedDaysOfWeek.push(getTranslation('sunday', 'daysOfWeek_'));
    }
    if (daysOfWeek.includes(8)) {
      expectedDaysOfWeek.push(getTranslation('holiday', 'daysOfWeek_'));
    }
    const expectedDaysOfWeekString = expectedDaysOfWeek.join(', ');
    await t.expect(actualDaysOfWeek).eql(expectedDaysOfWeekString);
  }
}
