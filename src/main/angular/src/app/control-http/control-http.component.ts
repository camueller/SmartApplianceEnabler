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
import {HttpSwitch} from '../control/http-switch';
import {ControlHttpErrorMessages} from './control-http-error-messages';
import {FormUtil} from '../shared/form-util';

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
  httpForm: FormGroup;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.errorMessages =  new ControlHttpErrorMessages(this.translate);
    this.httpForm = this.buildHttpFormGroup(this.control.httpSwitch);
    this.httpForm.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.httpForm.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.httpForm, this.errorMessages);
    });
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
}
