import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlDefaults} from '../control-defaults';
import {FormGroup, Validators} from '@angular/forms';
import {StartingCurrentSwitch} from './starting-current-switch';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {getValidInt} from '../../shared/form-util';

@Component({
  selector: 'app-control-startingcurrent',
  templateUrl: './control-startingcurrent.component.html',
  styleUrls: ['./control-startingcurrent.component.scss']
})
export class ControlStartingcurrentComponent implements OnChanges, OnInit {
  @Input()
  startingCurrentSwitch: StartingCurrentSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.startingCurrentSwitch) {
      if (changes.startingCurrentSwitch.currentValue) {
        this.startingCurrentSwitch = changes.startingCurrentSwitch.currentValue;
      } else {
        this.startingCurrentSwitch = new StartingCurrentSwitch();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlStartingcurrentComponent.error.', [
      new ErrorMessage('powerThreshold', ValidatorType.pattern),
      new ErrorMessage('startingCurrentDetectionDuration', ValidatorType.pattern),
      new ErrorMessage('finishedCurrentDetectionDuration', ValidatorType.pattern),
      new ErrorMessage('minRunningTime', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'powerThreshold',
      this.startingCurrentSwitch && this.startingCurrentSwitch.powerThreshold,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'startingCurrentDetectionDuration',
      this.startingCurrentSwitch && this.startingCurrentSwitch.startingCurrentDetectionDuration,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'finishedCurrentDetectionDuration',
      this.startingCurrentSwitch && this.startingCurrentSwitch.finishedCurrentDetectionDuration,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'minRunningTime',
      this.startingCurrentSwitch && this.startingCurrentSwitch.minRunningTime,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(): StartingCurrentSwitch {
    const powerThreshold = getValidInt(this.form.controls.powerThreshold.value);
    const startingCurrentDetectionDuration = getValidInt(this.form.controls.startingCurrentDetectionDuration.value);
    const finishedCurrentDetectionDuration = getValidInt(this.form.controls.finishedCurrentDetectionDuration.value);
    const minRunningTime = getValidInt(this.form.controls.minRunningTime.value);

    // all properties are optional; therefore we always have to return an instance

    this.startingCurrentSwitch.powerThreshold = powerThreshold;
    this.startingCurrentSwitch.startingCurrentDetectionDuration = startingCurrentDetectionDuration;
    this.startingCurrentSwitch.finishedCurrentDetectionDuration = finishedCurrentDetectionDuration;
    this.startingCurrentSwitch.minRunningTime = minRunningTime;
    return this.startingCurrentSwitch;
  }
}
