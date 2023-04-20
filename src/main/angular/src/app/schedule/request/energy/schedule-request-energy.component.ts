import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {EnergyRequest} from './energy-request';
import {ErrorMessages} from '../../../shared/error-messages';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {ScheduleRequestEnergyModel} from './schedule-request-energy.model';
import { isRequired } from 'src/app/shared/form-util';

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
  form: FormGroup<ScheduleRequestEnergyModel>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('minEnergy', new FormControl(this.minEnergy, Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('maxEnergy', new FormControl(this.maxEnergy,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]));
    this.form.addControl('enabledExternally', new FormControl(!this.energyRequest.enabled));
  }

  updateModelFromForm(): EnergyRequest | undefined {
    const enabled = !this.form.controls.enabledExternally.value;
    const minEnergy = this.form.controls.minEnergy.value;
    const maxEnergy = this.form.controls.maxEnergy.value;

    if (!(minEnergy || maxEnergy)) {
      return undefined;
    }

    this.energyRequest.enabled = enabled;
    this.energyRequest.min = minEnergy;
    this.energyRequest.max = maxEnergy;
    return this.energyRequest;
  }
}
