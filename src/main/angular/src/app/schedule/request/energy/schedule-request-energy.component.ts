import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, UntypedFormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {EnergyRequest} from './energy-request';
import {ErrorMessages} from '../../../shared/error-messages';
import {FormHandler} from '../../../shared/form-handler';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {getValidInt} from '../../../shared/form-util';

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
  form: UntypedFormGroup;
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
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestEnergyComponent.error.', [
      new ErrorMessage('minEnergy', ValidatorType.pattern),
      new ErrorMessage('maxEnergy', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
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

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'minEnergy', this.minEnergy,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'maxEnergy', this.maxEnergy,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(): EnergyRequest | undefined {
    const minEnergy = getValidInt(this.form.controls.minEnergy.value);
    const maxEnergy = getValidInt(this.form.controls.maxEnergy.value);

    if (!(minEnergy || maxEnergy)) {
      return undefined;
    }

    this.energyRequest.min = minEnergy;
    this.energyRequest.max = maxEnergy;
    return this.energyRequest;
  }
}
