import {Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {TimeUtil} from '../../shared/time-util';
import {Status} from '../status';
import {TrafficLightState} from '../traffic-light/traffic-light-state';

@Component({
    selector: 'app-status-view',
    templateUrl: './status-view.component.html',
    styleUrls: ['./status-view.component.scss', '../status.component.scss'],
    standalone: false
})
export class StatusViewComponent implements OnInit {

  @Input()
  status: Status;
  @Input()
  trafficLightStateHandler: TrafficLightState;
  translatedStrings: { [key: string]: string } = {};

  constructor(private translate: TranslateService) { }

  ngOnInit() {
    this.translate.get([
      'StatusComponent.plannedRunningTime',
      'StatusComponent.plannedMinRunningTime',
      'StatusComponent.plannedMaxRunningTime',
      'StatusComponent.remainingRunningTime',
      'StatusComponent.remainingMinRunningTime',
      'StatusComponent.remainingMaxRunningTime'
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
  }

  isTrafficLightRed(): boolean {
    return this.trafficLightStateHandler.isRed();
  }

  isTrafficLightRedBlink(): boolean {
    return this.trafficLightStateHandler.isRedBlink();
  }

  isTrafficLightYellow(): boolean {
    return this.trafficLightStateHandler.isYellow();
  }

  isTrafficLightGreen(): boolean {
    return this.trafficLightStateHandler.isGreen();
  }

  getRemainingRunningTimeLabel(applianceStatus: Status): string {
    if (this.isTrafficLightGreen()) {
      return this.translatedStrings['StatusComponent.remainingRunningTime'];
    }
    return this.translatedStrings['StatusComponent.plannedRunningTime'];
  }

  getRemainingMinRunningTimeLabel(applianceStatus: Status): string {
    if (this.isTrafficLightGreen()) {
      return this.translatedStrings['StatusComponent.remainingMinRunningTime'];
    }
    return this.translatedStrings['StatusComponent.plannedMinRunningTime'];
  }

  getRemainingMaxRunningTimeLabel(applianceStatus: Status): string {
    if (this.isTrafficLightGreen()) {
      return this.translatedStrings['StatusComponent.remainingMaxRunningTime'];
    }
    return this.translatedStrings['StatusComponent.plannedMaxRunningTime'];
  }

  getCurrentPower(applianceStatus: Status): number {
    if (applianceStatus.currentChargePower) {
      return this.toKWh(applianceStatus.currentChargePower);
    }
    return 0;
  }

  toHourMinuteWithUnits(seconds: number): string {
    if (seconds == null) {
      return '';
    }
    return TimeUtil.toHourMinuteWithUnits(seconds);
  }

  toKWh(wh: number): number {
    if (wh) {
      // limit to 1 decimal digit
      return Math.round(wh / 1000 * 10) / 10;
    }
    return 0;
  }
}
