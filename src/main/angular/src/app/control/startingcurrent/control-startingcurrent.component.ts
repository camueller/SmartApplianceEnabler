import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlDefaults} from '../control-defaults';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {StartingCurrentSwitch} from './starting-current-switch';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ControlStartingcurrentModel} from './control-startingcurrent.model';
import { isRequired } from 'src/app/shared/form-util';

@Component({
    selector: 'app-control-startingcurrent',
    templateUrl: './control-startingcurrent.component.html',
    styleUrls: ['./control-startingcurrent.component.scss'],
    standalone: false
})
export class ControlStartingcurrentComponent implements OnChanges, OnInit {
  @Input()
  startingCurrentSwitch: StartingCurrentSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  form: FormGroup<ControlStartingcurrentModel>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('powerThreshold', new FormControl(this.startingCurrentSwitch?.powerThreshold,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('startingCurrentDetectionDuration',
      new FormControl(this.startingCurrentSwitch?.startingCurrentDetectionDuration,
        Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('finishedCurrentDetectionDuration',
      new FormControl(this.startingCurrentSwitch?.finishedCurrentDetectionDuration,
        Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('minRunningTime', new FormControl(this.startingCurrentSwitch?.minRunningTime,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
  }

  updateModelFromForm(): StartingCurrentSwitch {
    const powerThreshold = this.form.controls.powerThreshold.value;
    const startingCurrentDetectionDuration = this.form.controls.startingCurrentDetectionDuration.value;
    const finishedCurrentDetectionDuration = this.form.controls.finishedCurrentDetectionDuration.value;
    const minRunningTime = this.form.controls.minRunningTime.value;

    // all properties are optional; therefore we always have to return an instance

    this.startingCurrentSwitch.powerThreshold = powerThreshold ?? null;
    this.startingCurrentSwitch.startingCurrentDetectionDuration = startingCurrentDetectionDuration ?? null;
    this.startingCurrentSwitch.finishedCurrentDetectionDuration = finishedCurrentDetectionDuration ?? null;
    this.startingCurrentSwitch.minRunningTime = minRunningTime ?? null;
    return this.startingCurrentSwitch;
  }
}
