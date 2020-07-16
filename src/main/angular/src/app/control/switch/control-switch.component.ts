import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Switch} from './switch';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ControlService} from '../control-service';
import {TranslateService} from '@ngx-translate/core';
import {ControlDefaults} from '../control-defaults';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {FormHandler} from '../../shared/form-handler';
import {Logger} from '../../log/logger';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {getValidInt} from '../../shared/form-util';

@Component({
  selector: 'app-control-switch',
  templateUrl: './control-switch.component.html',
  styleUrls: ['./control-switch.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlSwitchComponent implements OnChanges, OnInit {
  @Input()
  switch_: Switch;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  applianceId: string;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private controlService: ControlService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.switch_) {
      if (changes.switch_.currentValue) {
        this.switch_ = changes.switch_.currentValue;
      } else {
        this.switch_ = new Switch();
      }
    }
    this.expandParentForm();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlSwitchComponent.error.', [
      new ErrorMessage('gpio', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('gpio', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.expandParentForm();
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'gpio', this.switch_ && this.switch_.gpio,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'reverseStates', this.switch_ && this.switch_.reverseStates);
  }

  updateModelFromForm(): Switch | undefined {
    const gpio = getValidInt(this.form.controls.gpio.value);
    const reverseStates = this.form.controls.reverseStates.value;

    if (!(gpio || reverseStates)) {
      return undefined;
    }

    this.switch_.gpio = gpio;
    this.switch_.reverseStates = reverseStates;
    return this.switch_;
  }
}
