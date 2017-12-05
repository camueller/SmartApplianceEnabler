import {AfterViewChecked, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ApplianceService} from '../appliance/appliance.service';
import {TranslateService} from '@ngx-translate/core';
import {ApplianceStatus} from '../appliance/appliance-status';
import {TimeUtil} from '../shared/time-util';
import {FormBuilder, FormControl, FormControlName, FormGroup, NgForm, Validators} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Subscription} from 'rxjs/Subscription';

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
  this.control.nativeElement = this.valueAccessor._elementRef.nativeElement;

  const classAttribute: string = this.valueAccessor._elementRef.nativeElement.attributes.getNamedItem('class');
  if (classAttribute != null) {
    const classAttributeValues = classAttribute['nodeValue'];
    if (classAttributeValues.indexOf('clockpicker') > -1) {
      $(this.valueAccessor._elementRef.nativeElement).on('change', (event) => {
        this._control.setValue(event.target.value);
        this.control.markAsDirty();
      });
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
  applianceStatuses: ApplianceStatus[];
  typePrefix = 'ApplianceComponent.type.';
  translatedTypes = new Object();
  switchOnForm: FormGroup;
  switchOnApplianceId: string;
  initializeClockPicker: boolean;
  loadApplianceStatusesSubscription: Subscription;

  constructor(private applianceService: ApplianceService,
              private translate: TranslateService) {
  }

  ngOnInit()  {
    this.loadApplianceStatuses();
    this.loadApplianceStatusesSubscription = Observable.interval(60 * 1000)
      .subscribe(() => this.loadApplianceStatuses());
    this.switchOnForm = new FormGroup( {
      switchOnRunningTime: new FormControl(null, [
        Validators.required,
        Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)])
    });
  }

  ngAfterViewChecked() {
    if (this.initializeClockPicker) {
      $('.clockpicker').clockpicker({ autoclose: true });
      this.initializeClockPicker = false;
    }
  }

  ngOnDestroy() {
    this.loadApplianceStatusesSubscription.unsubscribe();
  }

  loadApplianceStatuses() {
    this.applianceService.getApplianceStatus().subscribe(applianceStatuses => {
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

  isStopLightOn(applianceStatus: ApplianceStatus): boolean {
    return applianceStatus.planningRequested && !applianceStatus.earliestStartPassed && !applianceStatus.on;
  }

  isSlowLightOn(applianceStatus: ApplianceStatus): boolean {
    return applianceStatus.earliestStartPassed && !applianceStatus.on;
  }

  isGoLightOn(applianceStatus: ApplianceStatus): boolean {
    return applianceStatus.on;
  }

  onClickStopLight(applianceId: string) {
    // console.log('CLICK STOP=' + applianceId);
  }

  /**
   * Immediately turn on the appliance and set remainingRunningTime in RunningtimeMonitor.
   * @param {string} applianceId
   */
  onClickGoLight(applianceId: string) {
    // console.log('CLICK GO=' + applianceId);
    this.applianceService.suggestRuntime(applianceId).subscribe(suggestedRuntime => {
      const hourMinute = TimeUtil.toHourMinute(suggestedRuntime);
      this.switchOnForm.controls['switchOnRunningTime'].setValue(hourMinute);
    });
    this.switchOnApplianceId = applianceId;
    this.initializeClockPicker = true;
  }

  submitSwitchOnForm() {
    const switchOnRunningTime = this.switchOnForm.value.switchOnRunningTime;
    // console.log('SWITCH ON=' + this.switchOnForm.value.switchOnRunningTime);
    const seconds = TimeUtil.toSeconds(switchOnRunningTime);
    this.applianceService.setRuntime(this.switchOnApplianceId, seconds);
    this.applianceService.toggleAppliance(this.switchOnApplianceId, true).subscribe(() => this.loadApplianceStatuses());
    this.switchOnApplianceId = null;
  }

  getFormattedRuntime(seconds: number): string {
    return TimeUtil.toHourMinuteWithUnits(seconds);
  }
}
