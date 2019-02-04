import {Component, Input, OnInit} from '@angular/core';
import {Status} from '../status/status';
import {TrafficLight} from '../status/traffic-light';
import {TimeUtil} from '../shared/time-util';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-status-view',
  templateUrl: './status-view.component.html',
  styleUrls: ['./status-view.component.css', '../status/status.component.css']
})
export class StatusViewComponent implements OnInit {

  @Input()
  status: Status;
  @Input()
  trafficLight: TrafficLight;
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
    return this.trafficLight === TrafficLight.Red;
  }

  isTrafficLightYellow(): boolean {
    return this.trafficLight === TrafficLight.Yellow;
  }

  isTrafficLightGreen(): boolean {
    return this.trafficLight === TrafficLight.Green;
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
