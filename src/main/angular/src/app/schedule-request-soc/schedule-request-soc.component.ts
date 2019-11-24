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
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get evId() {
    return this.socRequest && this.socRequest.evId;
  }

  get soc() {
    return this.socRequest && this.socRequest.soc;
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
