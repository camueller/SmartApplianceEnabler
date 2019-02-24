import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Control} from '../control/control';
import {ControlDefaults} from '../control/control-defaults';
import {FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {ControlService} from '../control/control-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpSwitch} from './http-switch';
import {ControlHttpErrorMessages} from './control-http-error-messages';
import {FormUtil} from '../shared/form-util';
import {FormMarkerService} from '../shared/form-marker-service';

@Component({
  selector: 'app-control-http',
  templateUrl: './control-http.component.html',
  styleUrls: ['../app.component.css']
})
export class ControlHttpComponent implements OnInit {
  @Input()
  control: Control;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  form: FormGroup;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.errorMessages =  new ControlHttpErrorMessages(this.translate);
    this.form = this.buildHttpFormGroup(this.control.httpSwitch);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  buildHttpFormGroup(httpSwitch: HttpSwitch): FormGroup {
    const fg =  new FormGroup({});
    FormUtil.addFormControl(fg, 'onUrl', httpSwitch ? httpSwitch.onUrl : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
    FormUtil.addFormControl(fg, 'onData', httpSwitch ? httpSwitch.onData : undefined);
    FormUtil.addFormControl(fg, 'offUrl', httpSwitch ? httpSwitch.offUrl : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
    FormUtil.addFormControl(fg, 'offData', httpSwitch ? httpSwitch.offData : undefined);
    FormUtil.addFormControl(fg, 'username', httpSwitch ? httpSwitch.username : undefined);
    FormUtil.addFormControl(fg, 'password', httpSwitch ? httpSwitch.password : undefined);
    FormUtil.addFormControl(fg, 'contentType', httpSwitch ? httpSwitch.contentType : undefined);
    return fg;
  }

  updateHttpSwitch(form: FormGroup, httpSwitch: HttpSwitch) {
    httpSwitch.onUrl = form.controls.onUrl.value;
    httpSwitch.onData = form.controls.onData.value;
    httpSwitch.offUrl = form.controls.offUrl.value;
    httpSwitch.offData = form.controls.offData.value;
    httpSwitch.username = form.controls.username.value;
    httpSwitch.password = form.controls.password.value;
    httpSwitch.contentType = form.controls.contentType.value;
  }

  submitForm() {
    this.updateHttpSwitch(this.form, this.control.httpSwitch);
    this.controlService.updateControl(this.control, this.applianceId).subscribe();
    this.form.markAsPristine();
  }
}
