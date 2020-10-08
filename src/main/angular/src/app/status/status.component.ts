import {Component, OnDestroy, OnInit, QueryList, ViewChildren} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Status} from './status';
import {interval, Subject, Subscription} from 'rxjs';
import {StatusService} from './status.service';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {ControlService} from '../control/control-service';
import { MessageBoxLevel } from '../material/messagebox/messagebox.component';
import {TrafficLightClick} from './traffic-light/traffic-light-click';
import {TrafficLightState} from './traffic-light/traffic-light-state';
import {TrafficLightComponent} from './traffic-light/traffic-light.component';
import {ApplianceType} from '../appliance/appliance-type';
import {ElectricVehicle} from '../control/evcharger/electric-vehicle/electric-vehicle';
import {EvChargerState} from '../control/evcharger/ev-charger-state';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss']
})
export class StatusComponent implements OnInit, OnDestroy {

  constructor(private statusService: StatusService,
              private controlService: ControlService,
              private translate: TranslateService) {
  }
  applianceStatuses: Status[] = [];
  typePrefix = 'ApplianceComponent.type.';
  translatedTypes = {};
  dows: DayOfWeek[] = [];
  loadApplianceStatusesSubscription: Subscription;
  @ViewChildren('trafficLights')
  trafficLightComps: QueryList<TrafficLightComponent>;
  applianceIdClicked: string;
  editMode = false;
  MessageBoxLevel = MessageBoxLevel;
  electricVehicles = new Map<string, ElectricVehicle[]>();

  ngOnInit() {
    DaysOfWeek.getDows(this.translate).subscribe(dows => this.dows = dows);
    this.loadApplianceStatuses(() => {
      this.applianceStatuses.filter(status => status.type === ApplianceType.EV_CHARGER).forEach(status => {
        this.controlService.getElectricVehicles(status.id).subscribe(electricVehicles => {
          this.electricVehicles.set(status.id, electricVehicles);
        });
      });
    });
    this.loadApplianceStatusesSubscription = interval(60 * 1000)
      .subscribe(() => this.loadApplianceStatuses(() => {}));
  }

  ngOnDestroy() {
    this.loadApplianceStatusesSubscription.unsubscribe();
  }

  loadApplianceStatuses(onComplete: () => void) {
    this.statusService.getStatus().subscribe(applianceStatuses => {
      this.applianceStatuses = applianceStatuses.filter(applianceStatus => applianceStatus.controllable);

      const types = [];
      this.applianceStatuses.forEach(applianceStatus => types.push(this.typePrefix + applianceStatus.type));
      if (types.length > 0) {
        this.translate.get(types).subscribe(translatedTypes => this.translatedTypes = translatedTypes);
      }
      onComplete();
    });
  }

  get hasControllableAppliances() {
    return this.applianceStatuses.length > 0;
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
        return applianceStatus.on && !applianceStatus.optionalEnergy;
      },

      isGreenBlink(): boolean {
        return applianceStatus.on && applianceStatus.optionalEnergy;
      }
    };
  }

  getTrafficLightClickHandler(applianceStatus: Status): TrafficLightClick {
    const stateHandler = this.getTrafficLightStateHandler(applianceStatus);
    const this_ = this;
    return {
      isRedClickable(): boolean {
        return applianceStatus.on && ! this_.editMode && ! stateHandler.isRed();
      },

      onRedClicked(status: Status, onActionCompleted: Subject<any>) {
        this_.applianceIdClicked = status.id;
        this_.statusService.toggleAppliance(status.id, false).subscribe(() => {
          this_.statusService.setAcceptControlRecommendations(status.id, false).subscribe();
          this_.loadApplianceStatuses(() => onActionCompleted.next());
        });
      },

      isGreenClickable(): boolean {
        return applianceStatus.state !== EvChargerState.VEHICLE_NOT_CONNECTED && ! this_.editMode && ! stateHandler.isGreen();
      },

      onGreenClicked(status: Status, onActionCompleted: Subject<any>) {
        this_.applianceIdClicked = status.id;
        // backend returns "null" if not interrupted but may return "0" right after interruption.
        if (status.interruptedSince != null) {
          // only switch on again
          this_.statusService.resetAcceptControlRecommendations(status.id).subscribe(() => {
            this_.statusService.toggleAppliance(status.id, true).subscribe(() => {
              this_.loadApplianceStatuses(() => onActionCompleted.next());
            });
          });
        } else {
          // display form to request runtime parameters
          this_.editMode = true;
          onActionCompleted.next();
        }
      }
    };
  }

  getTrafficLightComponent(applianceId: string): TrafficLightComponent {
    return this.trafficLightComps.find(comp => (comp.key as Status).id === applianceId);
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

  isEditMode(applianceStatus: Status): boolean {
    return this.editMode && applianceStatus.id === this.applianceIdClicked;
  }

  onBeforeFormSubmit() {
    this.getTrafficLightComponent(this.applianceIdClicked).showLoadingIndicator = true;
  }

  onFormSubmitted() {
    const applianceIdClicked = this.applianceIdClicked;
    this.loadApplianceStatuses(() => this.getTrafficLightComponent(applianceIdClicked).showLoadingIndicator = false);
    this.applianceIdClicked = null;
    this.editMode = false;
  }

  onFormCancel() {
    this.editMode = false;
  }

  getTrafficLightStateHandlerForExplanation(red: boolean, yellow: boolean, green: boolean): TrafficLightState {
    return {
      isRed(): boolean {
        return red;
      },

      isYellow(): boolean {
        return yellow;
      },

      isGreen(): boolean {
        return green;
      },

      isGreenBlink(): boolean {
        return false;
      }
    };
  }
}
