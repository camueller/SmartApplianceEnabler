import {Component, OnDestroy, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Status} from './status';
import {interval, Subscription, Subject} from 'rxjs';
import {StatusService} from './status.service';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {ControlService} from '../control/control-service';
import {TrafficLightState} from '../traffic-light/traffic-light-state';
import {TrafficLightClick} from '../traffic-light/traffic-light-click';

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

  getApplianceStatus(applianceId: string): Status {
    return this.applianceStatuses.filter(applianceStatus => applianceStatus.id === applianceId)[0];
  }

  getTrafficLightStateHandler(applianceStatus: Status): TrafficLightState {
    return {
      isRed(): boolean {
        return applianceStatus.planningRequested && applianceStatus.earliestStart > 0 && !applianceStatus.on;
      },

      isYellow(): boolean {
        return applianceStatus.earliestStart === 0 && !applianceStatus.on;
      },

      isGreen(): boolean {
        return applianceStatus.on;
      },

      isGreenBlink(): boolean {
        return applianceStatus.on && applianceStatus.optionalEnergy;
      }
    };
  }

  getTrafficLightClickHandler(applianceStatus: Status): TrafficLightClick {
    const stateHandler = this.getTrafficLightStateHandler(applianceStatus);
    return {
      isRedClickable(): boolean {
        return ! stateHandler.isRed();
      },

      onRedClicked(applianceId: string, onActionCompleted: Subject<any>) {
        this.applianceIdClicked = applianceId;
        this.statusService.toggleAppliance(applianceId, false).subscribe(() => {
          this.statusService.setAcceptControlRecommendations(applianceId, false).subscribe();
          this.loadApplianceStatuses(() => this.showLoadingIndicator = false);
          onActionCompleted.next();
        });
      },

      isGreenClickable(): boolean {
        return ! stateHandler.isGreen();
      },

      onGreenClicked(applianceId: string, onActionCompleted: Subject<any>) {
        this.applianceIdClicked = applianceId;
        const status = this.getApplianceStatus(applianceId);
        // backend returns "null" if not interrupted but may return "0" right after interruption.
        if (status.interruptedSince != null) {
          // only switch on again
          this.statusService.resetAcceptControlRecommendations(applianceId).subscribe(() => {
            this.statusService.toggleAppliance(applianceId, true).subscribe(() => {
              this.loadApplianceStatuses();
              onActionCompleted.next();
            });
          });
        } else {
          // display form to request runtime parameters
          onActionCompleted.next();
        }
      }
    };
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

  isTrafficLightGreenClicked(applianceStatus: Status): boolean {
    return this.getTrafficLightStateHandler(applianceStatus).isGreen() && applianceStatus.id === this.applianceIdClicked;
  }

  onBeforeFormSubmit() {
    // this.showLoadingIndicator = true;
  }

  onFormSubmitted() {
    // this.loadApplianceStatuses(() => this.showLoadingIndicator = false);
    this.loadApplianceStatuses();
    this.applianceIdClicked = null;
  }
}
