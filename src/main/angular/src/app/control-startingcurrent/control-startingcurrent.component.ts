import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {ControlDefaults} from '../control/control-defaults';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {StartingCurrentSwitch} from './starting-current-switch';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {getValidInt} from '../shared/form-util';

@Component({
  selector: 'app-control-startingcurrent',
  templateUrl: './control-startingcurrent.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlStartingcurrentComponent implements OnInit, AfterViewChecked {
  @Input()
  startingCurrentSwitch: StartingCurrentSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  private form: FormGroup;
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

  ngOnInit() {
    this.startingCurrentSwitch = this.startingCurrentSwitch || new StartingCurrentSwitch();
    this.errorMessages = new ErrorMessages('ControlStartingcurrentComponent.error.', [
      new ErrorMessage('powerThreshold', ValidatorType.pattern),
      new ErrorMessage('startingCurrentDetectionDuration', ValidatorType.pattern),
      new ErrorMessage('finishedCurrentDetectionDuration', ValidatorType.pattern),
      new ErrorMessage('minRunningTime', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.startingCurrentSwitch, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm(form: FormGroup, startingCurrentSwitch: StartingCurrentSwitch, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'powerThreshold',
      startingCurrentSwitch ? startingCurrentSwitch.powerThreshold : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'startingCurrentDetectionDuration',
      startingCurrentSwitch ? startingCurrentSwitch.startingCurrentDetectionDuration : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'finishedCurrentDetectionDuration',
      startingCurrentSwitch ? startingCurrentSwitch.finishedCurrentDetectionDuration : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'minRunningTime',
      startingCurrentSwitch ? startingCurrentSwitch.minRunningTime : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(): StartingCurrentSwitch | undefined {
    const powerThreshold = getValidInt(this.form.controls.powerThreshold.value);
    const startingCurrentDetectionDuration = getValidInt(this.form.controls.startingCurrentDetectionDuration.value);
    const finishedCurrentDetectionDuration = getValidInt(this.form.controls.finishedCurrentDetectionDuration.value);
    const minRunningTime = getValidInt(this.form.controls.minRunningTime.value);

    if (!(powerThreshold || startingCurrentDetectionDuration || finishedCurrentDetectionDuration || minRunningTime)) {
      return undefined;
    }

    this.startingCurrentSwitch.powerThreshold = powerThreshold;
    this.startingCurrentSwitch.startingCurrentDetectionDuration = startingCurrentDetectionDuration;
    this.startingCurrentSwitch.finishedCurrentDetectionDuration = finishedCurrentDetectionDuration;
    this.startingCurrentSwitch.minRunningTime = minRunningTime;
    return this.startingCurrentSwitch;
  }
}
