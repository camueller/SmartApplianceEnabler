import {AfterViewChecked, Component, Input, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
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
import {fixExpressionChangedAfterItHasBeenCheckedError} from '../shared/form-util';
import {HttpConfigurationComponent} from '../http-configuration/http-configuration.component';
import {HttpReadComponent} from '../http-read/http-read.component';

@Component({
  selector: 'app-control-http',
  templateUrl: './control-http.component.html',
  styleUrls: ['../global.css']
})
export class ControlHttpComponent implements OnInit, AfterViewChecked {
  @Input()
  httpSwitch: HttpSwitch;
  @ViewChild(HttpConfigurationComponent)
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChildren('httpWriteComponents')
  httpWriteComps: QueryList<HttpWriteComponent>;
  @ViewChild(HttpReadComponent)
  httpReadComp: HttpReadComponent;
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
      this.httpSwitch.httpWrites = [this.createHttpWrite()];
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

  get readValueNames() {
    return [ControlValueName.On];
  }

  get valueNameTextKeys() {
    return ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];
  }

  get readValueNameTextKeys() {
    return ['ControlHttpComponent.read.On'];
  }

  getWriteFormControlPrefix(index: number) {
    return `write${index}.`;
  }

  get isAddHttpWritePossible() {
    if (this.httpSwitch.httpWrites.length === 1) {
      return this.httpSwitch.httpWrites[0].writeValues.length < 2;
    }
    return this.httpSwitch.httpWrites.length < 2;
  }

  get maxValues() {
    return this.httpSwitch.httpWrites.length === 2 ? 1 : 2;
  }

  addHttpWrite() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    this.httpSwitch.httpWrites.push(this.createHttpWrite());
    this.form.markAsDirty();
  }

  onHttpWriteRemove(index: number) {
    this.httpSwitch.httpWrites.splice(index, 1);
  }

  createHttpWrite() {
    const httpWrite = new HttpWrite();
    httpWrite.writeValues = [new HttpWriteValue()];
    return httpWrite;
  }

  expandParentForm(form: FormGroup, httpSwitch: HttpSwitch, formHandler: FormHandler) {
  }

  updateModelFromForm(): HttpSwitch | undefined {
    const httpConfiguration = this.httpConfigurationComp.updateModelFromForm();
    const httpWrites = [];
    this.httpWriteComps.forEach(httpWriteComponent => {
      const httpWrite = httpWriteComponent.updateModelFromForm();
      if (httpWrite) {
        httpWrites.push(httpWrite);
      }
    });
    const httpRead = this.httpReadComp.updateModelFromForm();

    if (!(httpConfiguration || httpWrites.length > 0 || httpRead)) {
      return undefined;
    }

    this.httpSwitch.httpConfiguration = httpConfiguration;
    this.httpSwitch.httpWrites = httpWrites;
    this.httpSwitch.httpRead = httpRead;
    return this.httpSwitch;
  }
}
