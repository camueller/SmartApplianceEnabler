import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
import {FormMarkerService} from '../shared/form-marker-service';
import {FormHandler} from '../shared/form-handler';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {StartingCurrentSwitch} from '../control-startingcurrent/starting-current-switch';
import {ControlStartingcurrentComponent} from '../control-startingcurrent/control-startingcurrent.component';

@Component({
  selector: 'app-control-http',
  templateUrl: './control-http.component.html',
  styleUrls: ['../global.css']
})
export class ControlHttpComponent implements OnInit, AfterViewChecked {
  @Input()
  control: Control;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
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
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
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

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  buildHttpFormGroup(httpSwitch: HttpSwitch): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'onUrl', httpSwitch ? httpSwitch.onUrl : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
    this.formHandler.addFormControl(fg, 'onData', httpSwitch ? httpSwitch.onData : undefined);
    this.formHandler.addFormControl(fg, 'offUrl', httpSwitch ? httpSwitch.offUrl : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
    this.formHandler.addFormControl(fg, 'offData', httpSwitch ? httpSwitch.offData : undefined);
    this.formHandler.addFormControl(fg, 'username', httpSwitch ? httpSwitch.username : undefined);
    this.formHandler.addFormControl(fg, 'password', httpSwitch ? httpSwitch.password : undefined);
    this.formHandler.addFormControl(fg, 'contentType', httpSwitch ? httpSwitch.contentType : undefined);
    return fg;
  }

  updateHttpSwitch(form: FormGroup, httpSwitch: HttpSwitch, startingCurrentSwitch: StartingCurrentSwitch) {
    httpSwitch.onUrl = form.controls.onUrl.value;
    httpSwitch.onData = form.controls.onData.value;
    httpSwitch.offUrl = form.controls.offUrl.value;
    httpSwitch.offData = form.controls.offData.value;
    httpSwitch.username = form.controls.username.value;
    httpSwitch.password = form.controls.password.value;
    httpSwitch.contentType = form.controls.contentType.value;
    if (this.control.startingCurrentDetection) {
      ControlStartingcurrentComponent.updateStartingCurrentSwitch(form, startingCurrentSwitch);
    }
  }

  submitForm() {
    this.updateHttpSwitch(this.form, this.control.httpSwitch, this.control.startingCurrentSwitch);
    this.controlService.updateControl(this.control, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
    this.childFormChanged.emit(this.form.valid);
  }
}
