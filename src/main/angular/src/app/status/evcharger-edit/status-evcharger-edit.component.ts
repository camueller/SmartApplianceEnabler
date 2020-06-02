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
  electricVehicleSelected: ElectricVehicle;

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
    this.buildForm();
  }

  buildForm() {
    if (this.electricVehicles.length > 0) {
      this.electricVehicleSelected = this.electricVehicles[0];
      this.retrieveSoc();
    }
    this.form = new FormGroup({
      electricVehicle: new FormControl(this.electricVehicleSelected && this.electricVehicleSelected.id),
      stateOfChargeCurrent: new FormControl(undefined, Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
      stateOfChargeRequested: new FormControl(this.electricVehicleSelected && this.electricVehicleSelected.defaultSocManual,
        Validators.pattern(InputValidatorPatterns.PERCENTAGE)),
      chargeEndDow: new FormControl(),
    }, socValidator);
    this.form.get('electricVehicle').valueChanges.subscribe(evIdSelected => {
      this.electricVehicleSelected = this.getElectricVehicle(evIdSelected);
      this.form.get('stateOfChargeRequested').setValue(this.electricVehicleSelected.defaultSocManual);
      this.retrieveSoc();
    });
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  getElectricVehicle(id: number): ElectricVehicle {
    return id && this.electricVehicles.find(ev => ev.id === id);
  }

  retrieveSoc() {
    this.statusService.getSoc(this.status.id, this.electricVehicleSelected.id).subscribe(soc => {
      if (! Number.isNaN(Number.parseInt(soc, 10))) {
        this.form.get('stateOfChargeCurrent').setValue(Number.parseFloat(soc).toFixed());
      }
    });
  }

  get hasErrors(): boolean {
    return Object.keys(this.errors).length > 0;
  }

  get error(): string {
    const errors = Object.values(this.errors);
    return errors.length > 0 ? errors[0] : undefined;
  }

  cancelForm() {
    this.formCancelled.emit();
  }

  submitForm() {
    this.beforeFormSubmit.emit();
    const evid = this.form.value.electricVehicle;
    const socCurrent = this.form.value.stateOfChargeCurrent;
    const socRequested = this.form.value.stateOfChargeRequested;
    const chargeEndTime = this.chargeEndTimeComp.updateModelFromForm();
    const chargeEnd = TimeUtil.timestringOfNextMatchingDow(
      this.form.value.chargeEndDow,
      chargeEndTime);
    this.statusService.requestEvCharge(this.status.id, evid, socCurrent, socRequested, chargeEnd)
      .subscribe(() => this.formSubmitted.emit());
  }
}
