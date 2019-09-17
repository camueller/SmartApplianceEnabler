import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Control} from '../control/control';
import {Switch} from './switch';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ControlService} from '../control/control-service';
import {TranslateService} from '@ngx-translate/core';
import {ControlDefaults} from '../control/control-defaults';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {FormMarkerService} from '../shared/form-marker-service';
import {FormHandler} from '../shared/form-handler';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {StartingCurrentSwitch} from '../control-startingcurrent/starting-current-switch';
import {Logger} from '../log/logger';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

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
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private controlService: ControlService,
              private formMarkerService: FormMarkerService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.control.switch_);
    this.errorMessages = new ErrorMessages('ControlSwitchComponent.error.', [
      new ErrorMessage('gpio', ValidatorType.required),
      new ErrorMessage('gpio', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm(form: FormGroup, switch_: Switch) {
    this.formHandler.addFormControl(form, 'gpio', switch_ ? switch_.gpio : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'reverseStates', switch_ && switch_.reverseStates );
  }

  updateModelFromForm(form: FormGroup, switch_: Switch, startingCurrentSwitch: StartingCurrentSwitch) {
    switch_.gpio = form.controls.gpio.value;
    switch_.reverseStates = form.controls.reverseStates.value;
    // if (this.control.startingCurrentDetection) {
    //   ControlStartingcurrentComponent.updateModelFromForm(form, startingCurrentSwitch);
    // }
  }
}
