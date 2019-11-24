import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {SocRequest} from './soc-request';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Schedule} from '../schedule/schedule';

@Component({
  selector: 'app-schedule-request-soc',
  templateUrl: './schedule-request-soc.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestSocComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  socRequest: SocRequest;
  @Input()
  electricVehicles: ElectricVehicle[];
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
    if (changes.socRequest) {
      if (changes.socRequest.currentValue) {
        this.socRequest = changes.socRequest.currentValue;
      } else {
        this.socRequest = new SocRequest();
      }
      this.updateForm(this.parent.form, this.socRequest, this.formHandler);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestSocComponent.error.', [
      new ErrorMessage(this.getFormControlName('soc'), ValidatorType.required, 'soc'),
      new ErrorMessage(this.getFormControlName('soc'), ValidatorType.pattern, 'soc'),
    ], this.translate);
    this.expandParentForm(this.form, this.socRequest, this.formHandler);
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

  expandParentForm(form: FormGroup, socRequest: SocRequest, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('evId'),
      socRequest && socRequest.evId);
    formHandler.addFormControl(form, this.getFormControlName('soc'),
      socRequest && socRequest.soc,
      [Validators.required, Validators.pattern(InputValidatorPatterns.PERCENTAGE)]);
  }

  updateForm(form: FormGroup, socRequest: SocRequest, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('evId'), socRequest.evId);
    formHandler.setFormControlValue(form, this.getFormControlName('soc'), socRequest.soc);
  }

  updateModelFromForm(): SocRequest | undefined {
    const evId = this.form.controls[this.getFormControlName('evId')].value;
    const soc = this.form.controls[this.getFormControlName('soc')].value;

    if (!(evId || soc)) {
      return undefined;
    }

    this.socRequest.evId = evId;
    this.socRequest.soc = soc;
    return this.socRequest;
  }
}
