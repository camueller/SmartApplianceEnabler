import {Component, Input, OnInit} from '@angular/core';
import {Status} from '../status/status';
import {TimeUtil} from '../shared/time-util';
import {DayOfWeek} from '../shared/days-of-week';

@Component({
  selector: 'app-status-charger-view',
  templateUrl: './status-evcharger-view.component.html',
  styleUrls: ['./status-evcharger-view.component.css', '../status/status.component.css']
})
export class StatusEvchargerViewComponent implements OnInit {

  @Input()
  status: Status;
  @Input()
  dows: DayOfWeek[] = [];

  constructor() { }

  ngOnInit() {
  }

  getEvNameCharging(status: Status): string {
    if (status && status.evIdCharging !== null) {
      return status.evStatuses.filter(evStatus => evStatus.id === status.evIdCharging)[0].name;
    }
    return undefined;
  }

  getCurrentChargePower(applianceStatus: Status): number {
    if (applianceStatus.currentChargePower != null) {
      return this.toKWh(applianceStatus.currentChargePower);
    }
    return 0;
  }

  toWeekdayString(seconds: number): string | undefined {
    if (! seconds) {
      return undefined;
    }
    const weekday = this.toWeekday(seconds);
    const dowMatches = this.dows.filter(dow => dow.id === weekday);
    if (dowMatches && dowMatches.length > 0) {
      return dowMatches[0].name;
    }
    return undefined;
  }

  toKWh(wh: number): number {
    if (wh) {
      return wh / 1000;
    }
    return undefined;
  }

  toHHmm(seconds: number): string {
    return TimeUtil.timestringFromDelta(seconds);
  }

  toWeekday(seconds: number): number {
    return TimeUtil.toWeekdayFromDelta(seconds);
  }
}
