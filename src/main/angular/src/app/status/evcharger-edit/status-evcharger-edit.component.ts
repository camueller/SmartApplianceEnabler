import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {StatusService} from '../status.service';
import {ControlService} from '../../control/control-service';
import {TimeUtil} from '../../shared/time-util';
import {FormHandler} from '../../shared/form-handler';
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
  @ViewChild('chargeEndTimeComponent', {static: true})
  chargeEndTimeComp: TimepickerComponent;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  chargeModes: ListItem[] = [];
  chargeModeSelected = ChargeMode.FAST;
  electricVehicleSelected: ElectricVehicle;
  soc: string;
  submitButtonTextStart: string;
  submitButtonTextSave: string;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private statusService: StatusService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('StatusEvchargerEditComponent.error.', [
      new ErrorMessage('stateOfChargeCurrent', ValidatorType.pattern),
      new ErrorMessage('stateOfChargeRequested', ValidatorType.pattern),
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

  buildForm() {
    this.form = new FormGroup({
      chargeMode: new FormControl(this.chargeModeSelected),
      electricVehicle: new FormControl(this.electricVehicleSelected && this.electricVehicleSelected.id),
      stateOfChargeCurrent: new FormControl(this.soc, Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
      stateOfChargeRequested: new FormControl(this.electricVehicleSelected && this.electricVehicleSelected.defaultSocManual,
        Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
    }, socValidator);
    if (this.chargeModeSelected === ChargeMode.OPTIMIZED) {
      this.form.addControl('chargeEndDow', new FormControl(undefined, Validators.required));
    } else if (this.chargeModeSelected === ChargeMode.EXCESS_ENERGY) {
      this.form.get('stateOfChargeRequested').setValue(this.electricVehicleSelected.defaultSocOptionalEnergy);
    }
    this.form.get('electricVehicle').valueChanges.subscribe(evIdSelected => {
      this.electricVehicleSelected = this.getElectricVehicle(evIdSelected);
      this.form.get('stateOfChargeRequested').setValue(this.electricVehicleSelected.defaultSocManual);
      this.retrieveSoc();
    });
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  chargeModeChanged(newMode?: string | undefined) {
    this.chargeModeSelected = ChargeMode[newMode];
    this.buildForm();
  }

  getElectricVehicle(id: number): ElectricVehicle {
    return id && this.electricVehicles.find(ev => ev.id === id);
  }

  retrieveSoc() {
    this.statusService.getSoc(this.status.id, this.electricVehicleSelected.id).subscribe(soc => {
      if (! Number.isNaN(Number.parseInt(soc, 10))) {
        this.soc = Number.parseFloat(soc).toFixed();
        this.form.get('stateOfChargeCurrent').setValue(this.soc);
      }
    });
  }

  get isChargeModeFast() {
    return this.chargeModeSelected === ChargeMode.FAST;
  }

  get isChargeModeOptimized() {
    return this.chargeModeSelected === ChargeMode.OPTIMIZED;
  }

  get isChargeModeExcessEnergy() {
    return this.chargeModeSelected === ChargeMode.EXCESS_ENERGY;
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
    const socCurrent = this.form.value.stateOfChargeCurrent;
    const socRequested = this.form.value.stateOfChargeRequested;
    if (this.isChargeModeExcessEnergy) {
      this.statusService.updateSoc(this.status.id, socCurrent, socRequested).subscribe(() => this.formSubmitted.emit());
    } else {
      let chargeEndTime: string|undefined;
      let chargeEnd: string|undefined;
      if (this.isChargeModeOptimized) {
        chargeEndTime = this.chargeEndTimeComp.updateModelFromForm();
        chargeEnd = TimeUtil.timestringOfNextMatchingDow(this.form.value.chargeEndDow, chargeEndTime);
      }
      this.statusService.requestEvCharge(this.status.id, evid, socCurrent, socRequested, chargeEnd)
        .subscribe(() => this.formSubmitted.emit());
    }
  }
}
