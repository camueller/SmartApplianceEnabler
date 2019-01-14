import {AfterViewChecked, Component, OnDestroy, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Status} from './status';
import {TimeUtil} from '../shared/time-util';
import {FormControl, FormControlName, FormGroup, Validators} from '@angular/forms';
import {interval, Subscription} from 'rxjs';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {StatusService} from './status.service';
import {EvStatus} from './ev-status';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';

declare const $: any;

/**
 * The time set by clock picker is displayed in input field but not set in the form model.
 * Since there is no direct access to the native element from the form control we have to add a hook to
 * propagate time changes on the native element to the form control.
 * Inspired by https://stackoverflow.com/questions/39642547/is-it-possible-to-get-native-element-for-formcontrol
 */
const originFormControlNameNgOnChanges = FormControlName.prototype.ngOnChanges;
FormControlName.prototype.ngOnChanges = function () {
  const result = originFormControlNameNgOnChanges.apply(this, arguments);
  this.control.nativeElement = this.valueAccessor._elementRef;

  const elementRef = this.valueAccessor._elementRef;
  if (elementRef) {
    const classAttribute: string = elementRef.nativeElement.attributes.getNamedItem('class');
    if (classAttribute != null) {
      const classAttributeValues = classAttribute['nodeValue'];
      if (classAttributeValues.indexOf('clockpicker') > -1) {
        $(this.valueAccessor._elementRef.nativeElement).on('change', (event) => {
          this.control.setValue(event.target.value);
          this.control.markAsDirty();
        });
      }
    }
  }
  return result;
};

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css']
})
export class StatusComponent implements OnInit, AfterViewChecked, OnDestroy {
  applianceStatuses: Status[];
  typePrefix = 'ApplianceComponent.type.';
  translatedTypes = new Object();
  translatedStrings: string[];
  switchOnForm: FormGroup;
  startChargeForm: FormGroup;
  switchOnApplianceId: string;
  dows: DayOfWeek[] = [];
  initializeOnceAfterViewChecked = false;
  loadApplianceStatusesSubscription: Subscription;

  constructor(private statusService: StatusService,
              private translate: TranslateService) {
  }

  ngOnInit()  {
    this.translate.get([
      'StatusComponent.plannedRunningTime',
      'StatusComponent.plannedMinRunningTime',
      'StatusComponent.plannedMaxRunningTime',
      'StatusComponent.remainingRunningTime',
      'StatusComponent.remainingMinRunningTime',
      'StatusComponent.remainingMaxRunningTime'
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
    DaysOfWeek.getDows(this.translate).subscribe(dows => this.dows = dows);
    this.loadApplianceStatuses();
    this.loadApplianceStatusesSubscription = interval(60 * 1000)
      .subscribe(() => this.loadApplianceStatuses());
    this.switchOnForm = new FormGroup( {
      switchOnRunningTime: new FormControl(null, [
        Validators.required,
        Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)])
    });
    this.initializeOnceAfterViewChecked = true;
  }

  updateStartChargeForm(applianceId: string, evStatuses: EvStatus) {
    if (evStatuses) {
      const electicVehicleControl = new FormControl(evStatuses.id);
      electicVehicleControl.valueChanges.subscribe(electricVehicleSelected => {
        this.updateStartChargeForm(applianceId, this.getEvStatus(this.getStatus(applianceId), electricVehicleSelected));
      });
      this.startChargeForm = new FormGroup({
        electricVehicle: electicVehicleControl,
        stateOfChargeCurrent: new FormControl(),
        stateOfChargeRequested: new FormControl(),
        chargeEndDow: new FormControl(),
        chargeEndTime: new FormControl(),
      });
      this.initializeOnceAfterViewChecked = true;
    }
  }

  ngAfterViewChecked() {
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
    }
  }

  ngOnDestroy() {
    this.loadApplianceStatusesSubscription.unsubscribe();
  }

  initializeClockPicker() {
    $('.clockpicker').clockpicker({ autoclose: true });
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

  getStatus(id: string): Status {
    return this.applianceStatuses.filter(status => status.id === id)[0];
  }

  getEvStatus(status: Status, name: string): EvStatus {
    if (status && name) {
      return status.evStatuses.filter(evStatuses => evStatuses.name === name)[0];
    }
    return undefined;
  }

  getTranslatedType(type: string): string {
    if (this.translatedTypes != null) {
      return this.translatedTypes[this.typePrefix + type];
    }
    return '';
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

  isEvCharger(applianceStatus: Status): boolean {
    return applianceStatus.evStatuses ? true : false;
  }

  isStopLightOn(applianceStatus: Status): boolean {
    return applianceStatus.planningRequested && applianceStatus.earliestStart > 0 && !applianceStatus.on;
  }

  isSlowLightOn(applianceStatus: Status): boolean {
    return applianceStatus.earliestStart === 0 && !applianceStatus.on;
  }

  isGoLightOn(applianceStatus: Status): boolean {
    return applianceStatus.on;
  }

  isUsingOptionalEnergy(applianceStatus: Status): boolean {
    return applianceStatus.optionalEnergy;
  }

  onClickStopLight(applianceId: string) {
    // console.log('CLICK STOP=' + applianceId);
  }

  /**
   * Immediately turn on the appliance and set remainingRunningTime in RunningtimeMonitor.
   * @param {string} applianceId
   */
  onClickGoLight(applianceId: string) {
    const status = this.getStatus(applianceId);
    if (this.isEvCharger(status)) {
      this.updateStartChargeForm(applianceId, status.evStatuses[0]);
    } else {
      this.statusService.suggestRuntime(applianceId).subscribe(suggestedRuntime => {
        const hourMinute = TimeUtil.toHourMinute(Number.parseInt(suggestedRuntime));
        this.switchOnForm.controls['switchOnRunningTime'].setValue(hourMinute);
      });
    }
    this.switchOnApplianceId = applianceId;
    this.initializeOnceAfterViewChecked = true;
  }

  getRemainingRunningTimeLabel(applianceStatus: Status): string {
    if (this.isGoLightOn(applianceStatus)) {
      return this.translatedStrings['StatusComponent.remainingRunningTime'];
    }
    return this.translatedStrings['StatusComponent.plannedRunningTime'];
  }

  getRemainingMinRunningTimeLabel(applianceStatus: Status): string {
    if (this.isGoLightOn(applianceStatus)) {
      return this.translatedStrings['StatusComponent.remainingMinRunningTime'];
    }
    return this.translatedStrings['StatusComponent.plannedMinRunningTime'];
  }

  getRemainingMaxRunningTimeLabel(applianceStatus: Status): string {
    if (this.isGoLightOn(applianceStatus)) {
      return this.translatedStrings['StatusComponent.remainingMaxRunningTime'];
    }
    return this.translatedStrings['StatusComponent.plannedMaxRunningTime'];
  }

  submitSwitchOnForm() {
    const switchOnRunningTime = this.switchOnForm.value.switchOnRunningTime;
    const seconds = TimeUtil.toSeconds(switchOnRunningTime);
    this.statusService.setRuntime(this.switchOnApplianceId, seconds).subscribe();
    this.statusService.toggleAppliance(this.switchOnApplianceId, true).subscribe(() => this.loadApplianceStatuses());
    this.switchOnApplianceId = null;
  }

  submitStartChargeForm() {
    const evid = this.startChargeForm.value.electricVehicle;
    const socCurrent = this.startChargeForm.value.stateOfChargeCurrent;
    const socRequested = this.startChargeForm.value.stateOfChargeRequested;
    const chargeEnd = TimeUtil.timestringOfNextMatchingDow(
      this.startChargeForm.value.chargeEndDow,
      this.startChargeForm.value.chargeEndTime);
    this.statusService.requestEvCharge(this.switchOnApplianceId, evid, socCurrent, socRequested, chargeEnd)
      .subscribe(() => this.loadApplianceStatuses());
    this.switchOnApplianceId = null;
  }

  toHourMinuteWithUnits(seconds: number): string {
    if (seconds == null) {
      return '';
    }
    return TimeUtil.toHourMinuteWithUnits(seconds);
  }

  toHHmm(seconds: number): string {
    return TimeUtil.timestringFromDelta(seconds);
  }

  toWeekday(seconds: number): number {
    return TimeUtil.toWeekdayFromDelta(seconds);
  }

  toWeekdayString(seconds: number): string {
    const weekday = this.toWeekday(seconds);
    return this.dows.filter(dow => dow.id === weekday)[0].name;
  }

  toKWh(wh: number): number {
    if (wh) {
      return wh / 1000;
    }
    return undefined;
  }
}
