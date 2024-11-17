import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {StatusService} from '../status.service';
import {ControlService} from '../../control/control-service';
import {TimeUtil} from '../../shared/time-util';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ElectricVehicle} from '../../control/evcharger/electric-vehicle/electric-vehicle';
import {Status} from '../status';
import {DayOfWeek} from '../../shared/days-of-week';
import {Logger} from '../../log/logger';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {TranslateService} from '@ngx-translate/core';
import {TimepickerComponent} from '../../material/timepicker/timepicker.component';
import {ChargeMode} from './charge-mode';
import {ListItem} from '../../shared/list-item';
import {StatusEvchargerEditModel} from './status-evcharger-edit.model';
import {getValidInt, isRequired} from '../../shared/form-util';

const socValidator = (control: AbstractControl): { [key: string]: boolean } => {
  const socCurrent = control.get('socCurrent');
  const socTarget = control.get('socTarget');
  if (!socCurrent || !socTarget
    || !socCurrent.value || !socTarget.value) {
    return null;
  }
  return Number.parseInt(socCurrent.value, 10)
  < Number.parseInt(socTarget.value, 10) ? null : {nomatch: true};
};

@Component({
  selector: 'app-status-evcharger-edit',
  templateUrl: './status-evcharger-edit.component.html',
  styleUrls: ['./status-evcharger-edit.component.scss', '../status.component.scss']
})
export class StatusEvchargerEditComponent implements OnInit {
  @Input()
  status: Status;
  @Input()
  dows: DayOfWeek[] = [];
  @Input()
  electricVehicles: ElectricVehicle[];
  @Output()
  beforeFormSubmit = new EventEmitter<any>();
  @Output()
  formSubmitted = new EventEmitter<any>();
  @Output()
  formCancelled = new EventEmitter<any>();
  @ViewChild('chargeStartTimeComponent', {static: true})
  chargeStartTimeComp: TimepickerComponent;
  @ViewChild('chargeEndTimeComponent', {static: true})
  chargeEndTimeComp: TimepickerComponent;
  form: FormGroup<StatusEvchargerEditModel>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  chargeModes: ListItem[] = [];
  chargeModeSelected = ChargeMode.FAST;
  electricVehicleSelected: ElectricVehicle;
  socString: string;
  submitButtonTextStart: string;
  submitButtonTextSave: string;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private statusService: StatusService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('StatusEvchargerEditComponent.error.', [
      new ErrorMessage('socCurrent', ValidatorType.pattern),
      new ErrorMessage('socTarget', ValidatorType.pattern),
      new ErrorMessage('chargeStartTime', ValidatorType.pattern),
      new ErrorMessage('chargeEndTime', ValidatorType.pattern),
    ], this.translate);
    this.translate.get('StatusComponent.buttonStart').subscribe(translated => this.submitButtonTextStart = translated);
    this.translate.get('button.save').subscribe(translated => this.submitButtonTextSave = translated);
    const chargeModeKeys = Object.keys(ChargeMode).map(key => `StatusComponent.chargeMode.${key}`);
    this.translate.get(chargeModeKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.chargeModes.push({value: key.split('.')[2], viewValue: translatedStrings[key] });
      });
    });
    if (this.electricVehicles.length > 0) {
      this.electricVehicleSelected = this.electricVehicles[0];
      this.retrieveSoc();
    }
    this.buildForm();
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  buildForm() {
    this.form = new FormGroup({
      chargeMode: new FormControl(this.chargeModeSelected),
      electricVehicle: new FormControl(this.electricVehicleSelected?.id),
      socCurrent: new FormControl(this.socString, Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
      socTarget: new FormControl(this.electricVehicleSelected && this.electricVehicleSelected.defaultSocManual,
        Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
    }, socValidator);
    if (this.isChargeModeTimed) {
      this.form.addControl('chargeStartDow', new FormControl(undefined, Validators.required));
    }
    if (this.isChargeModeOptimized || this.isChargeModeTimed) {
      this.form.addControl('chargeEndDow', new FormControl(undefined, this.isChargeModeTimed ? undefined : Validators.required));
    }
    this.form.get('electricVehicle').valueChanges.subscribe(evIdSelected => {
      this.electricVehicleSelected = this.getElectricVehicle(evIdSelected);
      this.form.get('socTarget').setValue(this.electricVehicleSelected.defaultSocManual);
      this.retrieveSoc();
    });
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  @ViewChild('chargeStartTimeComponent')
  set chargeStartTimeComponent(chargeStartTimeComponent: ElementRef) {
    if (chargeStartTimeComponent instanceof TimepickerComponent) {
      this.chargeStartTimeComp = chargeStartTimeComponent;
    }
  }

  @ViewChild('chargeEndTimeComponent')
  set chargeEndTimeComponent(chargeEndTimeComponent: ElementRef) {
    if (chargeEndTimeComponent instanceof TimepickerComponent) {
      this.chargeEndTimeComp = chargeEndTimeComponent;
    }
  }

  chargeModeChanged(newMode?: string | undefined) {
    this.chargeModeSelected = ChargeMode[newMode];
    this.buildForm();
  }

  getElectricVehicle(id: number): ElectricVehicle {
    return id && this.electricVehicles.find(ev => ev.id === id);
  }

  retrieveSoc() {
    this.statusService.getSoc(this.status.id, this.electricVehicleSelected.id).subscribe(socString => {
      if (! Number.isNaN(Number.parseInt(socString, 10))) {
        const soc = Number.parseFloat(socString);
        if (soc > 0) {
          this.socString = soc.toFixed();
        } else {
          this.socString = undefined;
        }
        if (this.form.controls.socCurrent) {
          this.form.controls.socCurrent.setValue(this.socString);
        }
      }
    });
  }

  get isChargeModeFast() {
    return this.chargeModeSelected === ChargeMode.FAST;
  }

  get isChargeModeTimed() {
    return this.chargeModeSelected === ChargeMode.TIMED;
  }

  get isChargeModeOptimized() {
    return this.chargeModeSelected === ChargeMode.OPTIMIZED;
  }

  get isChargeModeExcessEnergy() {
    return this.chargeModeSelected === ChargeMode.EXCESS_ENERGY;
  }

  get socTargetPlaceholder() {
    if(this.isChargeModeExcessEnergy) {
      return `${this.electricVehicleSelected?.defaultSocOptionalEnergy ?? 100}`;
    }
    return '100';
  }

  get hasErrors(): boolean {
    return Object.keys(this.errors).length > 0;
  }

  get error(): string {
    const errors = Object.values(this.errors);
    return errors.length > 0 ? errors[0] : undefined;
  }

  get submitButtonText() {
    if (this.isChargeModeExcessEnergy) {
      return this.submitButtonTextSave;
    }
    return this.submitButtonTextStart;
  }

  cancelForm() {
    this.formCancelled.emit();
  }

  submitForm() {
    this.beforeFormSubmit.emit();
    const evid = this.form.value.electricVehicle;
    const socCurrent = getValidInt(this.form.value.socCurrent);
    const socTarget = this.form.value.socTarget;
    if (this.isChargeModeExcessEnergy) {
      this.statusService.updateSoc(this.status.id, evid?.toString(), socCurrent, socTarget).subscribe(() => this.formSubmitted.emit());
    } else {
      let chargeStart: string|undefined;
      let chargeEnd: string|undefined;
      if (this.isChargeModeTimed) {
        const chargeStartTime = this.chargeStartTimeComp.updateModelFromForm();
        chargeStart = TimeUtil.timestringOfNextMatchingDow(this.form.value.chargeStartDow, chargeStartTime);
      }
      if (this.isChargeModeOptimized || this.isChargeModeTimed) {
        const chargeEndTime = this.chargeEndTimeComp.updateModelFromForm();
        chargeEnd = TimeUtil.timestringOfNextMatchingDow(this.form.value.chargeEndDow, chargeEndTime);
      }
      this.statusService.requestEvCharge(this.status.id, evid?.toString(), socCurrent, socTarget, chargeStart, chargeEnd)
        .subscribe(() => this.formSubmitted.emit());
    }
  }
}
