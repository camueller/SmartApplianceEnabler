import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {
  assertCheckbox,
  assertSelectOption,
  selectOption,
  selectorCheckboxByFormControlName,
  selectorCheckboxCheckedByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {simpleRequestType, simpleTimeframeType} from '../../../../../main/angular/src/app/shared/form-util';
import {DayTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/day/day-timeframe';
import {DayTimeframePage} from './day-timeframe.page';
import {RuntimeRequest} from '../../../../../main/angular/src/app/schedule/request/runtime/runtime-request';
import {RuntimeRequestPage} from './runtime-request.page';
import {ConsecutiveDaysTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/consecutivedays/consecutive-days-timeframe';
import {ConsecutiveDaysTimeframePage} from './consecutive-days-timeframe.page';
import {SocRequest} from '../../../../../main/angular/src/app/schedule/request/soc/soc-request';
import {SocRequestPage} from './soc-request.page';
import {EnergyRequest} from '../../../../../main/angular/src/app/schedule/request/energy/energy-request';
import {EnergyRequestPage} from './energy-request.page';

export class SchedulePage {

  private static selectorBase(scheduleIndex: number) {
    return `*[formarrayname="schedules"] > div:nth-child(${scheduleIndex + 1}) > app-schedule`;
  }

  public static async setSchedule(t: TestController, schedule: Schedule, scheduleIndex: number) {
    await this.setEnabled(t, schedule.enabled, scheduleIndex);
    if (schedule.timeframeType) {
      await this.setTimeframeType(t, schedule.timeframeType, scheduleIndex);
    }
    if (schedule.requestType) {
      await this.setRequestType(t, schedule.requestType, scheduleIndex);
    }

    if (schedule.timeframeType === DayTimeframe.TYPE) {
      await DayTimeframePage.setDayTimeframe(t, schedule.timeframe as DayTimeframe, this.selectorBase(scheduleIndex));
    }
    if (schedule.timeframeType === ConsecutiveDaysTimeframe.TYPE) {
      await ConsecutiveDaysTimeframePage.setConsecutiveDaysTimeframe(t, schedule.timeframe as ConsecutiveDaysTimeframe,
        this.selectorBase(scheduleIndex));
    }

    if (schedule.requestType === RuntimeRequest.TYPE) {
      await RuntimeRequestPage.setRuntimeRequest(t, schedule.request as RuntimeRequest, this.selectorBase(scheduleIndex));
    }
    if (schedule.requestType === EnergyRequest.TYPE) {
      await EnergyRequestPage.setEnergyRequest(t, schedule.request as EnergyRequest, this.selectorBase(scheduleIndex));
    }
    if (schedule.requestType === SocRequest.TYPE) {
      await SocRequestPage.setSocRequest(t, schedule.request as SocRequest, this.selectorBase(scheduleIndex));
    }
  }
  public static async assertSchedule(t: TestController, schedule: Schedule, scheduleIndex: number, evName?: string) {
    await this.assertEnabled(t, schedule.enabled, scheduleIndex);
    if (schedule.timeframeType) {
      await this.assertTimeframeType(t, schedule.timeframeType, scheduleIndex);
    }
    if (schedule.requestType) {
      await this.assertRequestType(t, schedule.requestType, scheduleIndex);
    }

    if (schedule.timeframeType === DayTimeframe.TYPE) {
      await DayTimeframePage.assertDayTimeframe(t, schedule.timeframe as DayTimeframe, this.selectorBase(scheduleIndex));
    }
    if (schedule.timeframeType === ConsecutiveDaysTimeframe.TYPE) {
      await ConsecutiveDaysTimeframePage.assertConsecutiveDaysTimeframe(t, schedule.timeframe as ConsecutiveDaysTimeframe,
        this.selectorBase(scheduleIndex));
    }

    if (schedule.requestType === RuntimeRequest.TYPE) {
      await RuntimeRequestPage.assertRuntimeRequest(t, schedule.request as RuntimeRequest, this.selectorBase(scheduleIndex));
    }
    if (schedule.requestType === EnergyRequest.TYPE) {
      await EnergyRequestPage.assertEnergyRequest(t, schedule.request as EnergyRequest, this.selectorBase(scheduleIndex));
    }
    if (schedule.requestType === SocRequest.TYPE) {
      await SocRequestPage.assertSocRequest(t, schedule.request as SocRequest, this.selectorBase(scheduleIndex), evName);
    }
  }

  public static async setEnabled(t: TestController, enabled: boolean, scheduleIndex: number) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('enabled', undefined,
      SchedulePage.selectorBase(scheduleIndex)), enabled);
  }
  public static async assertEnabled(t: TestController, enabled: boolean, scheduleIndex: number) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('enabled', undefined,
      SchedulePage.selectorBase(scheduleIndex)), enabled);
  }

  public static async setTimeframeType(t: TestController, timeframeType: string, scheduleIndex: number) {
    await selectOption(t, selectorSelectByFormControlName('timeframeType', undefined,
      SchedulePage.selectorBase(scheduleIndex)), simpleTimeframeType(timeframeType));
  }
  public static async assertTimeframeType(t: TestController, timeframeType: string, scheduleIndex: number) {
    await assertSelectOption(t, selectorSelectedByFormControlName('timeframeType', undefined,
      SchedulePage.selectorBase(scheduleIndex)), timeframeType);
  }

  public static async setRequestType(t: TestController, requestType: string, scheduleIndex: number) {
    await selectOption(t, selectorSelectByFormControlName('requestType', undefined,
      SchedulePage.selectorBase(scheduleIndex)), simpleRequestType(requestType));
  }
  public static async assertRequestType(t: TestController, requestType: string, scheduleIndex: number) {
    await assertSelectOption(t, selectorSelectedByFormControlName('requestType', undefined,
      SchedulePage.selectorBase(scheduleIndex)), requestType);
  }
}
