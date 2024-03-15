import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {SocRequest} from './soc-request';
import {ErrorMessages} from '../../../shared/error-messages';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ElectricVehicle} from '../../../control/evcharger/electric-vehicle/electric-vehicle';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {ScheduleRequestSocModel} from './schedule-request-soc.model';
import { isRequired } from 'src/app/shared/form-util';

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
  @Input()
  enabled: boolean;
  form: FormGroup<ScheduleRequestSocModel>;
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
    if (changes.socRequest) {
      if (changes.socRequest.currentValue) {
        this.socRequest = changes.socRequest.currentValue;
      } else {
        this.socRequest = new SocRequest();
      }
      this.expandParentForm();
    }
    if (changes.enabled && !changes.enabled.firstChange) {
      this.setEnabled(changes.enabled.currentValue);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestSocComponent.error.', [
      new ErrorMessage('evId', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('soc', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('evId', new FormControl(this.evId, Validators.required));
    this.form.addControl('soc', new FormControl(this.soc,
      [Validators.required, Validators.pattern(InputValidatorPatterns.PERCENTAGE)]));
    this.form.addControl('enabledExternally', new FormControl(this.socRequest.enabled === false));
  }

  updateModelFromForm(): SocRequest | undefined {
    const enabled = !this.form.controls.enabledExternally.value;
    const evId = this.form.controls.evId.value;
    const soc = this.form.controls.soc.value;

    if (!(evId || soc)) {
      return undefined;
    }

    this.socRequest.enabled = enabled;
    this.socRequest.evId = evId;
    this.socRequest.soc = soc;
    return this.socRequest;
  }
}
