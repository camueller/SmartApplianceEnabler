import {Component, OnDestroy, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Status} from './status';
import {interval, Subscription} from 'rxjs';
import {StatusService} from './status.service';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {TrafficLight} from './traffic-light';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css']
})
export class StatusComponent implements OnInit, OnDestroy {
  applianceStatuses: Status[];
  typePrefix = 'ApplianceComponent.type.';
  translatedTypes = {};
  dows: DayOfWeek[] = [];
  loadApplianceStatusesSubscription: Subscription;
  applianceIdClicked: string;
  trafficLightClicked: TrafficLight;

  constructor(private statusService: StatusService,
              private translate: TranslateService) {
  }

  ngOnInit()  {
    DaysOfWeek.getDows(this.translate).subscribe(dows => this.dows = dows);
    this.loadApplianceStatuses();
    this.loadApplianceStatusesSubscription = interval(60 * 1000)
      .subscribe(() => this.loadApplianceStatuses());
  }

  ngOnDestroy() {
    this.loadApplianceStatusesSubscription.unsubscribe();
  }

  loadApplianceStatuses() {
    this.statusService.getStatus().subscribe(applianceStatuses => {
      this.applianceStatuses = applianceStatuses.filter(applianceStatus => applianceStatus.controllable);

      const types = [];
      this.applianceStatuses.forEach(applianceStatus => types.push(this.typePrefix + applianceStatus.type));
      if (types.length > 0) {
        this.translate.get(types).subscribe(translatedTypes => this.translatedTypes = translatedTypes);
      }
    });
  }

  getTranslatedType(type: string): string {
    if (this.translatedTypes != null) {
      return this.translatedTypes[this.typePrefix + type];
    }
    return '';
  }

  isEvCharger(applianceStatus: Status): boolean {
    return applianceStatus.evStatuses ? true : false;
  }

  getTrafficLightState(applianceStatus: Status): TrafficLight {
    if (applianceStatus.planningRequested && applianceStatus.earliestStart > 0 && !applianceStatus.on) {
      return TrafficLight.Red;
    }
    if (applianceStatus.earliestStart === 0 && !applianceStatus.on) {
      return TrafficLight.Yellow;
    }
    if (applianceStatus.on) {
      if (applianceStatus.optionalEnergy) {
        return TrafficLight.GreenBlink;
      }
      return TrafficLight.Green;
    }
  }

  isTrafficLightRed(applianceStatus: Status): boolean {
    return this.getTrafficLightState(applianceStatus) === TrafficLight.Red;
  }

  isTrafficLightYellow(applianceStatus: Status): boolean {
    return this.getTrafficLightState(applianceStatus) === TrafficLight.Yellow;
  }

  isTrafficLightGreen(applianceStatus: Status): boolean {
    return this.getTrafficLightState(applianceStatus) === TrafficLight.Green;
  }

  isTrafficLightGreenClicked(applianceStatus: Status): boolean {
    return this.trafficLightClicked === TrafficLight.Green && applianceStatus.id === this.applianceIdClicked;
  }

  isTrafficLightGreenBlink(applianceStatus: Status): boolean {
    return this.getTrafficLightState(applianceStatus) === TrafficLight.GreenBlink;
  }

  onClickStopLight(applianceId: string) {
    // console.log('CLICK STOP=' + applianceId);
  }

  /**
   * Immediately turn on the appliance and set remainingRunningTime in RunningtimeMonitor.
   * @param {string} applianceId
   */
  onClickGoLight(applianceId: string) {
    this.trafficLightClicked = TrafficLight.Green;
    this.applianceIdClicked = applianceId;
  }

  onFormSubmitted() {
    this.trafficLightClicked = undefined;
    this.applianceIdClicked = null;
    this.loadApplianceStatuses();
  }
}
