import {AfterViewChecked, Component, Input, OnInit, ViewChild} from '@angular/core';
import {ControlDefaults} from '../control/control-defaults';
import {FormGroup, FormGroupDirective} from '@angular/forms';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {HttpSwitch} from './http-switch';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {HttpWriteComponent} from '../http-write/http-write.component';
import {ControlValueName} from '../control/control-value-name';
import {HttpWrite} from '../http-write/http-write';
import {HttpWriteValue} from '../http-write-value/http-write-value';

@Component({
  selector: 'app-control-http',
  templateUrl: './control-http.component.html',
  styleUrls: ['../global.css']
})
export class ControlHttpComponent implements OnInit, AfterViewChecked {
  @Input()
  httpSwitch: HttpSwitch;
  @ViewChild('onHttpWrite')
  onHttpWriteComp: HttpWriteComponent;
  @ViewChild('offHttpWrite')
  offHttpWriteComp: HttpWriteComponent;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  form: FormGroup;
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
    this.httpSwitch = this.httpSwitch || new HttpSwitch();
    if (!this.httpSwitch.httpWrites) {
      this.httpSwitch.httpWrites = [new HttpWrite()];
    }
    if (!this.httpSwitch.httpWrites[0].writeValues) {
      this.httpSwitch.httpWrites[0].writeValues = [new HttpWriteValue()];
    }
    this.errorMessages = new ErrorMessages('ControlHttpComponent.error.', [
      new ErrorMessage('onUrl', ValidatorType.required),
      new ErrorMessage('onUrl', ValidatorType.pattern),
      new ErrorMessage('offUrl', ValidatorType.required),
      new ErrorMessage('offUrl', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpSwitch, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get valueNames() {
    return [ControlValueName.On, ControlValueName.Off];
  }

  get valueNameTextKeys() {
    return ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];
  }

  expandParentForm(form: FormGroup, httpSwitch: HttpSwitch, formHandler: FormHandler) {
  }

  updateModelFromForm(): HttpSwitch | undefined {
    const onHttpWrite = this.onHttpWriteComp.updateModelFromForm();
    const offHttpWrite = this.offHttpWriteComp.updateModelFromForm();

    if (!(onHttpWrite || offHttpWrite)) {
      return undefined;
    }

    // this.httpSwitch.onHttpWrite = onHttpWrite;
    // this.httpSwitch.offHttpWrite = offHttpWrite;
    return this.httpSwitch;
  }
}
