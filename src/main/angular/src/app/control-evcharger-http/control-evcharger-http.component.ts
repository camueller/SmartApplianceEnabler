import {AfterViewChecked, Component, Input, OnChanges, OnInit, QueryList, SimpleChanges, ViewChild, ViewChildren} from '@angular/core';
import {Settings} from '../settings/settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective} from '@angular/forms';
import {Logger} from '../log/logger';
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
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerHttpComponent implements OnChanges, OnInit, AfterViewChecked {

  @Input()
  evHttpControl: EvHttpControl;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  contentProtocols = [undefined, ContentProtocol.JSON];
  @ViewChild(HttpConfigurationComponent, { static: true })
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChildren('httpReadComponents')
  httpReadComps: QueryList<HttpReadComponent>;
  @ViewChildren('httpWriteComponents')
  httpWriteComps: QueryList<HttpWriteComponent>;
  form: FormGroup;
  formHandler: FormHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective) {
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.evHttpControl) {
      if (changes.evHttpControl.currentValue) {
        this.evHttpControl = changes.evHttpControl.currentValue;
      } else {
        this.evHttpControl = new EvHttpControl();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.expandParentForm();
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls.contentProtocol;
    return (contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
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

  getHttpReadFormGroup(index: number) {
    return this.httpReadsFormArray.controls[index];
  }

  addHttpRead() {
    const httpRead = HttpRead.createWithSingleChild();
    if (!this.evHttpControl.httpReads) {
      this.evHttpControl.httpReads = [];
    }
    this.evHttpControl.httpReads.push(httpRead);
    this.httpReadsFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  get httpReadsFormArray() {
    return this.form.controls.httpReads as FormArray;
  }

  onHttpReadRemove(index: number) {
    this.evHttpControl.httpReads.splice(index, 1);
    this.httpReadsFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  getHttpWriteFormGroup(index: number) {
    return this.httpWritesFormArray.controls[index];
  }

  addHttpWrite() {
    const httpWrite = HttpWrite.createWithSingleChild();
    if (!this.evHttpControl.httpWrites) {
      this.evHttpControl.httpWrites = [];
    }
    this.evHttpControl.httpWrites.push(httpWrite);
    this.httpWritesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  get httpWritesFormArray() {
    return this.form.controls.httpWrites as FormArray;
  }

  onHttpWriteRemove(index: number) {
    this.evHttpControl.httpWrites.splice(index, 1);
    this.httpWritesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'contentProtocol',
      this.evHttpControl && this.evHttpControl.contentProtocol);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'httpReads',
      this.evHttpControl.httpReads);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'httpWrites',
      this.evHttpControl.httpWrites);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'contentProtocol', this.evHttpControl.contentProtocol);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'httpReads',
      this.evHttpControl.httpReads);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'httpWrites',
      this.evHttpControl.httpWrites);
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

    this.evHttpControl.contentProtocol = contentProtocol;
    this.evHttpControl.httpConfiguration = httpConfiguration;
    this.evHttpControl.httpReads = httpReads;
    this.evHttpControl.httpWrites = httpWrites;
    return this.evHttpControl;
  }
}
