import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Switch} from './switch';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ControlService} from '../control/control-service';
import {TranslateService} from '@ngx-translate/core';
import {ControlDefaults} from '../control/control-defaults';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {getValidInt} from '../shared/form-util';

@Component({
  selector: 'app-control-switch',
  templateUrl: './control-switch.component.html',
  styleUrls: ['../global.css']
})
export class ControlSwitchComponent implements OnInit, AfterViewChecked {
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

  ngOnInit() {
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.switch_);
    this.errorMessages = new ErrorMessages('ControlSwitchComponent.error.', [
      new ErrorMessage('gpio', ValidatorType.required),
      new ErrorMessage('gpio', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm(form: FormGroup, switch_: Switch) {
    this.formHandler.addFormControl(form, 'gpio', switch_ ? switch_.gpio : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'reverseStates', switch_ && switch_.reverseStates );
  }

  updateModelFromForm(): Switch | undefined {
    const gpio = getValidInt(this.form.controls['gpio'].value);
    const reverseStates = this.form.controls['reverseStates'].value;

    if (!(gpio || reverseStates)) {
      return undefined;
    }

    this.switch_.gpio = gpio;
    this.switch_.reverseStates = reverseStates;
    return this.switch_;
  }
}
