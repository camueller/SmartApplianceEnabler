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
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestEnergyComponent implements OnChanges, OnInit {
  @Input()
  energyRequest: EnergyRequest;
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
