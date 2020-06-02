import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {EnergyRequest} from './energy-request';
import {ErrorMessages} from '../../../shared/error-messages';
import {FormHandler} from '../../../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';

@Component({
  selector: 'app-schedule-request-energy',
  templateUrl: './schedule-request-energy.component.html',
  styleUrls: ['./schedule-request-energy.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestEnergyComponent implements OnChanges, OnInit {
  @Input()
  energyRequest: EnergyRequest;
  @Input()
  enabled: boolean;
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
    if (changes.energyRequest) {
      if (changes.energyRequest.currentValue) {
        this.energyRequest = changes.energyRequest.currentValue;
      } else {
        this.energyRequest = new EnergyRequest();
      }
      this.updateForm();
    }
    if (changes.enabled && !changes.enabled.firstChange) {
      this.setEnabled(changes.enabled.currentValue);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestEnergyComponent.error.', [
      new ErrorMessage('minEnergy', ValidatorType.pattern),
      new ErrorMessage('maxEnergy', ValidatorType.required),
      new ErrorMessage('maxEnergy', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get minEnergy() {
    return this.energyRequest && this.energyRequest.min;
  }

  get maxEnergy() {
    return this.energyRequest && this.energyRequest.max;
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.minEnergy.enable();
      this.form.controls.maxEnergy.enable();
    } else {
      this.form.controls.minEnergy.disable();
      this.form.controls.maxEnergy.disable();
    }
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'minEnergy', this.minEnergy,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'maxEnergy', this.maxEnergy,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'minEnergy', this.minEnergy);
    this.formHandler.setFormControlValue(this.form, 'maxEnergy', this.maxEnergy);
  }

  updateModelFromForm(): EnergyRequest | undefined {
    const minEnergy = this.form.controls.minEnergy.value;
    const maxEnergy = this.form.controls.maxEnergy.value;

    if (!(minEnergy || maxEnergy)) {
      return undefined;
    }

    this.energyRequest.min = minEnergy;
    this.energyRequest.max = maxEnergy;
    return this.energyRequest;
  }
}
