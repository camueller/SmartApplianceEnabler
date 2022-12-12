import {Component, Input, OnInit} from '@angular/core';
import {TimeUtil} from '../../shared/time-util';
import {ElectricVehicle} from '../../control/evcharger/electric-vehicle/electric-vehicle';
import {Status} from '../status';
import {DayOfWeek} from '../../shared/days-of-week';

@Component({
  selector: 'app-status-charger-view',
  templateUrl: './status-evcharger-view.component.html',
  styleUrls: ['./status-evcharger-view.component.scss', '../status.component.scss']
})
export class StatusEvchargerViewComponent implements OnInit {

  @Input()
  status: Status;
  @Input()
  dows: DayOfWeek[] = [];
  @Input()
  electricVehicles: ElectricVehicle[];

  ngOnInit() {
  }

  get stateKey() {
    if (this.status.state) {
      return `StatusComponent.state.${this.status.state}`;
    }
  }

  getEvName(evId: number): string {
    if (this.electricVehicles && evId) {
      return this.electricVehicles.filter(ev => ev.id === evId)[0]?.name;
    }
    return undefined;
  }

  get socInitialTooltip() {
    if(!this.status.socInitialTimestamp) {
      return '';
    }
    return `${this.toWeekdayStringFromTimestamp(this.status.socInitialTimestamp)} ${this.toHHmmFromTimestamp(this.status.socInitialTimestamp)}`;
  }

  getCurrentChargePower(applianceStatus: Status): number {
    if (applianceStatus.currentChargePower) {
      return this.toKWh(applianceStatus.currentChargePower);
    }
    return 0;
  }

  toWeekdayStringFromDelta(seconds: number): string | undefined {
    if (!seconds) {
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
    return this.dows.find(dow => dow.id === weekday)?.name;
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
