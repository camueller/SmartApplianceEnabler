import {Component, Input, OnInit} from '@angular/core';
import {Status} from '../status/status';
import {TimeUtil} from '../shared/time-util';
import {DayOfWeek} from '../shared/days-of-week';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';

@Component({
  selector: 'app-status-charger-view',
  templateUrl: './status-evcharger-view.component.html',
  styleUrls: ['./status-evcharger-view.component.css', '../status/status.component.css']
})
export class StatusEvchargerViewComponent implements OnInit {

  @Input()
  status: Status;
  @Input()
  electricVehicles: ElectricVehicle[];
  @Input()
  dows: DayOfWeek[] = [];

  constructor() { }

  ngOnInit() {
  }

  get stateKey() {
    return `StatusComponent.state.${this.status.state}`;
  }

  getEvNameCharging(evId: number): string {
    if (this.electricVehicles && evId) {
      return this.electricVehicles.filter(ev => ev.id === evId)[0].name;
    }
    return undefined;
  }

  getCurrentChargePower(applianceStatus: Status): number {
    if (applianceStatus.currentChargePower) {
      return this.toKWh(applianceStatus.currentChargePower);
    }
    return 0;
  }

  toWeekdayStringFromDelta(seconds: number): string | undefined {
    if (! seconds) {
      return undefined;
    }
    const dow = this.toWeekdayFromDelta(seconds);
    return this.toWeekdayString(dow);
  }

  toWeekdayStringFromTimestamp(timestamp: number): string | undefined {
    const dow = TimeUtil.toWeekdayFromTimestamp(timestamp);
    return this.toWeekdayString(dow);
  }

  toWeekdayString(weekday: number): string | undefined {
    const dowMatches = this.dows.filter(dow => dow.id === weekday);
    if (dowMatches && dowMatches.length > 0) {
      return dowMatches[0].name;
    }
    return undefined;
  }

  toKWh(wh: number): number {
    if (wh) {
      // limit to 1 decimal digit
      return Math.round(wh / 1000 * 10) / 10;
    }
    return 0;
  }

  toHHmmFromDelta(seconds: number): string {
    return TimeUtil.timestringFromDelta(seconds);
  }

  toHHmmFromTimestamp(timestamp: number): string {
    return TimeUtil.timestringFromTimestamp(timestamp);
  }

  toWeekdayFromDelta(seconds: number): number {
    return TimeUtil.toWeekdayFromDelta(seconds);
  }
}
