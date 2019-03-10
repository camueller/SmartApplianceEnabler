import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AbstractControl, FormControl, FormControlName, FormGroup, Validators} from '@angular/forms';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {TimeUtil} from '../shared/time-util';
import {Status} from '../status/status';
import {StatusService} from '../status/status.service';
import {DayOfWeek} from '../shared/days-of-week';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {ControlService} from '../control/control-service';

const socValidator = (control: AbstractControl): { [key: string]: boolean } => {
  const stateOfChargeCurrent = control.get('stateOfChargeCurrent');
  const stateOfChargeRequested = control.get('stateOfChargeRequested');
  if (!stateOfChargeCurrent || !stateOfChargeRequested
    || !stateOfChargeCurrent.value || !stateOfChargeRequested.value) {
    return null;
  }
  return Number.parseInt(stateOfChargeCurrent.value, 10)
  < Number.parseInt(stateOfChargeRequested.value, 10) ? null : {nomatch: true};
};

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
  selector: 'app-status-evcharger-edit',
  templateUrl: './status-evcharger-edit.component.html',
  styleUrls: ['./status-evcharger-edit.component.css', '../status/status.component.css', '../global.css']
})
export class StatusEvchargerEditComponent implements OnInit, AfterViewChecked {
  @Input()
  applianceId: string;
  @Input()
  status: Status;
  @Input()
  electricVehicles: ElectricVehicle[];
  @Input()
  dows: DayOfWeek[] = [];
  @Output()
  formSubmitted = new EventEmitter<any>();
  startChargeForm: FormGroup;
  initializeOnceAfterViewChecked = false;

  constructor(private statusService: StatusService) { }

  ngOnInit() {
    if (this.electricVehicles.length > 0) {
      this.updateStartChargeForm(this.electricVehicles[0]);
    }
    this.initializeOnceAfterViewChecked = true;
  }

  ngAfterViewChecked() {
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
    }
  }

  initializeClockPicker() {
    $('.clockpicker').clockpicker({ autoclose: true });
  }

  updateStartChargeForm(ev: ElectricVehicle) {
    if (ev) {
      const electicVehicleControl = new FormControl(ev.id);
      electicVehicleControl.valueChanges.subscribe(evIdSelected => {
        const selectedElectricVehicle = this.getElectricVehicle(evIdSelected);
        this.updateStartChargeForm(selectedElectricVehicle);
      });
      this.startChargeForm = new FormGroup({
        electricVehicle: electicVehicleControl,
        stateOfChargeCurrent: new FormControl(null, Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
        stateOfChargeRequested: new FormControl(ev.defaultSocManual, Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
        chargeEndDow: new FormControl(),
        chargeEndTime: new FormControl(),
      }, socValidator);
      this.statusService.getSoc(this.applianceId, ev.id).subscribe(soc => {
        if (! Number.isNaN(Number.parseInt(soc, 10))) {
          this.startChargeForm.setControl('stateOfChargeCurrent', new FormControl(Number.parseFloat(soc).toFixed()));
        }
      });
      this.initializeOnceAfterViewChecked = true;
    }
  }

  getElectricVehicle(id: number): ElectricVehicle {
    return id && this.electricVehicles.filter(ev => ev.id === id)[0];
  }

  submitForm() {
    const evid = this.startChargeForm.value.electricVehicle;
    const socCurrent = this.startChargeForm.value.stateOfChargeCurrent;
    const socRequested = this.startChargeForm.value.stateOfChargeRequested;
    const chargeEnd = TimeUtil.timestringOfNextMatchingDow(
      this.startChargeForm.value.chargeEndDow,
      this.startChargeForm.value.chargeEndTime);
    this.statusService.requestEvCharge(this.applianceId, evid, socCurrent, socRequested, chargeEnd)
      .subscribe(() => this.formSubmitted.emit());
  }
}
