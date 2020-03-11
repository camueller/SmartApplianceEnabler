import {Component, Input, OnInit} from '@angular/core';
import {Status} from '../status/status';
import {TimeUtil} from '../shared/time-util';
import {TranslateService} from '@ngx-translate/core';
import {TrafficLightState} from '../traffic-light/traffic-light-state';

@Component({
  selector: 'app-status-view',
  templateUrl: './status-view.component.html',
  styleUrls: ['./status-view.component.css', '../status/status.component.css']
})
export class StatusViewComponent implements OnInit {

  @Input()
  status: Status;
  @Input()
  trafficLightStateHandler: TrafficLightState;
  translatedStrings: string[];

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

  toHourMinuteWithUnits(seconds: number): string {
    if (seconds == null) {
      return '';
    }
    return TimeUtil.toHourMinuteWithUnits(seconds);
  }
}
