import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {SocRequest} from './soc-request';
import {ErrorMessages} from '../../../shared/error-messages';
import {FormHandler} from '../../../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ElectricVehicle} from '../../../control/evcharger/electric-vehicle/electric-vehicle';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';

@Component({
  selector: 'app-schedule-request-soc',
  templateUrl: './schedule-request-soc.component.html',
  styleUrls: ['./schedule-request-soc.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestSocComponent implements OnChanges, OnInit {
  @Input()
  socRequest: SocRequest;
  @Input()
  electricVehicles: ElectricVehicle[];
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.socRequest) {
      if (changes.socRequest.currentValue) {
        this.socRequest = changes.socRequest.currentValue;
      } else {
        this.socRequest = new SocRequest();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestSocComponent.error.', [
      new ErrorMessage('soc', ValidatorType.required),
      new ErrorMessage('soc', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get evId() {
    return this.socRequest && this.socRequest.evId;
  }

  get soc() {
    return this.socRequest && this.socRequest.soc;
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.evId.enable();
      this.form.controls.soc.enable();
    } else {
      this.form.controls.evId.disable();
      this.form.controls.soc.disable();
    }
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'evId', this.evId);
    this.formHandler.addFormControl(this.form, 'soc', this.soc,
      [Validators.required, Validators.pattern(InputValidatorPatterns.PERCENTAGE)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'evId', this.evId);
    this.formHandler.setFormControlValue(this.form, 'soc', this.soc);
  }

  updateModelFromForm(): SocRequest | undefined {
    const evId = this.form.controls.evId.value;
    const soc = this.form.controls.soc.value;

    if (!(evId || soc)) {
      return undefined;
    }

    this.socRequest.evId = evId;
    this.socRequest.soc = soc;
    return this.socRequest;
  }
}
