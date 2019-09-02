import {AfterViewChecked, Component, Input, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Settings} from '../settings/settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ControlContainer, FormGroup, FormGroupDirective} from '@angular/forms';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {EvHttpControl} from './ev-http-control';
import {EvModbusReadRegisterName} from '../control-evcharger-modbus/ev-modbus-read-register-name';
import {EvModbusWriteRegisterName} from '../control-evcharger-modbus/ev-modbus-write-register-name';
import {ContentProtocol} from '../shared/content-protocol';
import {FormHandler} from '../shared/form-handler';
import {HttpRead} from '../http-read/http-read';
import {HttpWrite} from '../http-write/http-write';
import {HttpReadComponent} from '../http-read/http-read.component';
import {HttpWriteComponent} from '../http-write/http-write.component';
import {HttpConfigurationComponent} from '../http-configuration/http-configuration.component';

@Component({
  selector: 'app-control-evcharger-http',
  templateUrl: './control-evcharger-http.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerHttpComponent implements OnInit, AfterViewChecked {

  @Input()
  evHttpControl: EvHttpControl;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  contentProtocols = [undefined, ContentProtocol.JSON.toUpperCase()];
  @ViewChild(HttpConfigurationComponent)
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChildren('httpReadComponents')
  httpReadComps: QueryList<HttpReadComponent>;
  @ViewChildren('httpWriteComponents')
  httpWriteComps: QueryList<HttpWriteComponent>;
  form: FormGroup;
  formHandler: FormHandler;
  // @Input()
  // translationKeys: string[];
  // translatedStrings: string[];
  // errors: { [key: string]: string } = {};
  // errorMessages: ErrorMessages;
  // errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService) {
    // this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    // this.errorMessages = new MeterHttpErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.evHttpControl, this.formHandler);
    // if (!this.evHttpControl.httpConfiguration) {
    //   this.evHttpControl.httpConfiguration = new HttpConfiguration();
    //   this.evHttpControl.httpConfiguration.contentType = 'hallo';
    // }
    // this.form.statusChanges.subscribe(() => {
    //   this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    // });
    // this.translate.get(this.translationKeys).subscribe(translatedStrings => {
    //   this.translatedStrings = translatedStrings;
    // });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls['contentProtocol'];
    return (contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
  }

  getReadFormControlPrefix(index: number) {
    return `read${index}.`;
  }

  getWriteFormControlPrefix(index: number) {
    return `write${index}.`;
  }

  get readValueNames() {
    return Object.keys(EvModbusReadRegisterName);
  }

  get writeValueNames() {
    return Object.keys(EvModbusWriteRegisterName);
  }

  get readValueNameTextKeys() {
    return Object.keys(EvModbusReadRegisterName).map(key => `ControlEvchargerComponent.${key}`);
  }

  get writeValueNameTextKeys() {
    return Object.keys(EvModbusWriteRegisterName).map(key => `ControlEvchargerComponent.${key}`);
  }

  addHttpRead() {
    const httpRead = new HttpRead();
    this.evHttpControl.httpReads.push(httpRead);
    this.form.markAsDirty();
  }

  addHttpWrite() {
    const httpWrite = new HttpWrite();
    this.evHttpControl.httpWrites.push(httpWrite);
    this.form.markAsDirty();
  }

  expandParentForm(form: FormGroup, evHttpControl: EvHttpControl, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'contentProtocol',
      evHttpControl ? evHttpControl.contentProtocol : undefined);
  }

  updateModelFromForm(): EvHttpControl | undefined {
    const contentProtocol = this.form.controls.contentProtocol.value;
    const httpConfiguration = this.httpConfigurationComp.updateModelFromForm();
    const httpReads = [];
    this.httpReadComps.forEach(httpReadComponent => {
      const httpRead = httpReadComponent.updateModelFromForm();
      if (httpRead) {
        httpReads.push(httpRead);
      }
    });
    const httpWrites = [];
    this.httpWriteComps.forEach(httpWriteComponent => {
      const httpWrite = httpWriteComponent.updateModelFromForm();
      if (httpWrite) {
        httpWrites.push(httpWrite);
      }
    });

    if (!(contentProtocol || httpConfiguration || httpReads.length > 0 || httpWrites.length > 0)) {
      return undefined;
    }

    const evHttpControl = this.evHttpControl || new EvHttpControl();
    evHttpControl.contentProtocol = contentProtocol;
    evHttpControl.httpConfiguration = httpConfiguration;
    evHttpControl.httpReads = httpReads;
    evHttpControl.httpWrites = httpWrites;
    return evHttpControl;
  }
}
