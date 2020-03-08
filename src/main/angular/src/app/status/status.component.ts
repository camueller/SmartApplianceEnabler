import {Component, OnDestroy, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Status} from './status';
import {interval, Subscription} from 'rxjs';
import {StatusService} from './status.service';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {TrafficLight} from './traffic-light';
import {ControlService} from '../control/control-service';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css', '../global.css']
})
export class StatusComponent implements OnInit, OnDestroy {
  applianceStatuses: Status[] = [];
  typePrefix = 'ApplianceComponent.type.';
  translatedTypes = {};
  dows: DayOfWeek[] = [];
  loadApplianceStatusesSubscription: Subscription;
  applianceIdClicked: string;
  trafficLightClicked: TrafficLight;
  showLoadingIndicator: boolean;

  constructor(private statusService: StatusService,
              private controlService: ControlService,
              private translate: TranslateService) {
  }

  ngOnInit() {
    DaysOfWeek.getDows(this.translate).subscribe(dows => this.dows = dows);
    this.loadApplianceStatuses();
    this.loadApplianceStatusesSubscription = interval(60 * 1000)
      .subscribe(() => this.loadApplianceStatuses());
  }

  ngOnDestroy() {
    this.loadApplianceStatusesSubscription.unsubscribe();
  }

  loadApplianceStatuses(loadingCompletedFunction?: () => {}) {
    this.statusService.getStatus().subscribe(applianceStatuses => {
      this.applianceStatuses = applianceStatuses.filter(applianceStatus => applianceStatus.controllable);

      const types = [];
      this.applianceStatuses.forEach(applianceStatus => types.push(this.typePrefix + applianceStatus.type));
      if (types.length > 0) {
        this.translate.get(types).subscribe(translatedTypes => this.translatedTypes = translatedTypes);
      }

      loadingCompletedFunction();
    });
  }

  getApplianceStatus(applianceId: string): Status {
    return this.applianceStatuses.filter(applianceStatus => applianceStatus.id === applianceId)[0];
  }

  getTranslatedType(type: string): string {
    if (this.translatedTypes != null) {
      return this.translatedTypes[this.typePrefix + type];
    }
    return '';
  }

  isEvCharger(applianceStatus: Status): boolean {
    return applianceStatus.type === 'EVCharger';
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
    this.showLoadingIndicator = true;
    this.statusService.toggleAppliance(applianceId, false).subscribe(() => {
      this.statusService.setAcceptControlRecommendations(applianceId, false).subscribe();
      this.loadApplianceStatuses(() => this.showLoadingIndicator = false);
    });
  }

  /**
   * Immediately turn on the appliance and set remainingRunningTime in RunningtimeMonitor.
   * @param {string} applianceId
   */
  onClickGoLight(applianceId: string) {
    const status = this.getApplianceStatus(applianceId);
    // backend returns "null" if not interrupted but may return "0" right after interruption.
    if (status.interruptedSince != null) {
      // only switch on again
      this.statusService.resetAcceptControlRecommendations(applianceId).subscribe();
      this.statusService.toggleAppliance(applianceId, true).subscribe(() => this.loadApplianceStatuses());
    } else {
      // display form to request runtime parameters
      this.trafficLightClicked = TrafficLight.Green;
      this.applianceIdClicked = applianceId;
    }
  }

  onBeforeFormSubmit() {
    this.showLoadingIndicator = true;
  }

  onFormSubmitted() {
    this.trafficLightClicked = undefined;
    this.applianceIdClicked = null;
    this.loadApplianceStatuses(() => this.showLoadingIndicator = false);
  }
}
