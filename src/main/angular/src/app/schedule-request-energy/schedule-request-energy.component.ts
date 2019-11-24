import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {EnergyRequest} from './energy-request';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Schedule} from '../schedule/schedule';

@Component({
  selector: 'app-schedule-request-energy',
  templateUrl: './schedule-request-energy.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestEnergyComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  energyRequest: EnergyRequest;
  @Input()
  formControlNamePrefix = '';
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
      this.updateForm(this.parent.form, this.energyRequest, this.formHandler);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestEnergyComponent.error.', [
      new ErrorMessage(this.getFormControlName('minEnergy'), ValidatorType.pattern, 'minEnergy'),
      new ErrorMessage(this.getFormControlName('maxEnergy'), ValidatorType.required, 'maxEnergy'),
      new ErrorMessage(this.getFormControlName('maxEnergy'), ValidatorType.pattern, 'maxEnergy'),
    ], this.translate);
    this.expandParentForm(this.form, this.energyRequest, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  expandParentForm(form: FormGroup, energyRequest: EnergyRequest, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('minEnergy'),
      energyRequest && energyRequest.min,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, this.getFormControlName('maxEnergy'),
      energyRequest && energyRequest.max,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateForm(form: FormGroup, energyRequest: EnergyRequest, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('minEnergy'), energyRequest.min);
    formHandler.setFormControlValue(form, this.getFormControlName('maxEnergy'), energyRequest.max);
  }

  updateModelFromForm(): EnergyRequest | undefined {
    const minEnergy = this.form.controls[this.getFormControlName('minEnergy')].value;
    const maxEnergy = this.form.controls[this.getFormControlName('maxEnergy')].value;

    if (!(minEnergy || maxEnergy)) {
      return undefined;
    }

    this.energyRequest.min = minEnergy;
    this.energyRequest.max = maxEnergy;
    return this.energyRequest;
  }
}
