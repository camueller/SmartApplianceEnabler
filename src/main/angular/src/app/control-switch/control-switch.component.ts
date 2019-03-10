import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup, Validators} from '@angular/forms';
import {Control} from '../control/control';
import {Switch} from './switch';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ControlService} from '../control/control-service';
import {TranslateService} from '@ngx-translate/core';
import {ControlDefaults} from '../control/control-defaults';
import {ControlSwitchErrorMessages} from './control-switch-error-messages';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Logger} from '../log/logger';
import {FormMarkerService} from '../shared/form-marker-service';
import {FormHandler} from '../shared/form-handler';

@Component({
  selector: 'app-control-switch',
  templateUrl: './control-switch.component.html',
  styleUrls: ['../global.css']
})
export class ControlSwitchComponent implements OnInit, AfterViewChecked {
  @Input()
  control: Control;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  applianceId: string;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages =  new ControlSwitchErrorMessages(this.translate);
    this.form = this.buildSwitchFormGroup(this.control.switch_);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  buildSwitchFormGroup(switch_: Switch): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'gpio', switch_ ? switch_.gpio : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'reverseStates', switch_ && switch_.reverseStates );
    return fg;
  }

  updateSwitch(form: FormGroup, switch_: Switch) {
    switch_.gpio = form.controls.gpio.value;
    switch_.reverseStates = form.controls.reverseStates.value;
  }

  submitForm() {
    this.updateSwitch(this.form, this.control.switch_);
    this.controlService.updateControl(this.control, this.applianceId).subscribe();
    this.form.markAsPristine();
  }
}
