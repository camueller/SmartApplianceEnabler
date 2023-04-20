import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {Settings} from '../../../settings/settings';
import {SettingsDefaults} from '../../../settings/settings-defaults';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, ValidatorFn} from '@angular/forms';
import {Logger} from '../../../log/logger';
import {EvHttpControl} from './ev-http-control';
import {ContentProtocol} from '../../../shared/content-protocol';
import {HttpRead} from '../../../http/read/http-read';
import {HttpReadComponent} from '../../../http/read/http-read.component';
import {HttpConfigurationComponent} from '../../../http/configuration/http-configuration.component';
import {HttpWriteComponent} from '../../../http/write/http-write.component';
import {HttpWrite} from '../../../http/write/http-write';
import {EvReadValueName} from '../ev-read-value-name';
import {EvWriteValueName} from '../ev-write-value-name';
import {ValueNameChangedEvent} from '../../../meter/value-name-changed-event';
import {MessageBoxLevel} from 'src/app/material/messagebox/messagebox.component';
import {TranslateService} from '@ngx-translate/core';
import {getValueNamesNotConfigured} from '../../../shared/get-value-names-not-configured';
import {ControlEvchargerHttpModel} from './control-evcharger-http.model';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../../../shared/form-util';
import {HttpReadModel} from '../../../http/read/http-read.model';
import {HttpWriteModel} from '../../../http/write/http-write.model';

@Component({
  selector: 'app-control-evcharger-http',
  templateUrl: './control-evcharger-http.component.html',
  styleUrls: ['./control-evcharger-http.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ControlEvchargerHttpComponent implements OnChanges, OnInit {
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
  form: FormGroup<ControlEvchargerHttpModel>;
  @Input()
  translationKeys: string[];
  translatedStrings: string[];
  MessageBoxLevel = MessageBoxLevel;
  private readonly valueNameMissingError = 'valueNameMissingError';

  constructor(private logger: Logger,
              private translate: TranslateService,
              private parent: FormGroupDirective) {
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
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls.contentProtocol;
    return (contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
  }

  get readValueNamesNotConfigured() {
    const valueNamesNotConfigured = getValueNamesNotConfigured(
      this.httpReadsFormArray, 'httpReadValues', Object.keys(EvReadValueName));
    return this.translatedStrings
      ? valueNamesNotConfigured.map(name => this.translatedStrings[`ControlEvchargerComponent.${name}`]) : undefined;
  }

  get writeValueNamesNotConfigured() {
    const valueNamesNotConfigured = getValueNamesNotConfigured(
      this.httpWritesFormArray, 'httpWriteValues', Object.keys(EvWriteValueName));
    return this.translatedStrings
      ? valueNamesNotConfigured.map(name => this.translatedStrings[`ControlEvchargerComponent.${name}`]) : undefined;
  }

  onValueNameChanged(index: number, event: ValueNameChangedEvent) {
    this.form.updateValueAndValidity();
  }

  isAllValueNamesConfigured(): ValidatorFn {
    return () => {
      if ((this.readValueNamesNotConfigured && this.readValueNamesNotConfigured.length)
        || (this.writeValueNamesNotConfigured && this.writeValueNamesNotConfigured.length)) {
        return {[this.valueNameMissingError]: true};
      }
    };
  }

  get readValueNames() {
    return Object.keys(EvReadValueName);
  }

  get writeValueNames() {
    return Object.keys(EvWriteValueName);
  }

  get readValueNameTextKeys() {
    return Object.keys(EvReadValueName).map(key => `ControlEvchargerComponent.${key}`);
  }

  get writeValueNameTextKeys() {
    return Object.keys(EvWriteValueName).map(key => `ControlEvchargerComponent.${key}`);
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
    this.httpReadsFormArray.push(new FormGroup({} as HttpReadModel));
    this.form.markAsDirty();
  }

  get httpReadsFormArray() {
    return this.form.controls.httpReads;
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
    this.httpWritesFormArray.push(new FormGroup({} as HttpWriteModel));
    this.form.markAsDirty();
  }

  get httpWritesFormArray() {
    return this.form.controls.httpWrites;
  }

  onHttpWriteRemove(index: number) {
    this.evHttpControl.httpWrites.splice(index, 1);
    this.httpWritesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('contentProtocol', new FormControl(this.evHttpControl?.contentProtocol));
    this.form.addControl('httpReads', buildFormArrayWithEmptyFormGroups(this.evHttpControl.httpReads));
    this.form.addControl('httpWrites', buildFormArrayWithEmptyFormGroups(this.evHttpControl.httpWrites));
    this.form.setValidators(this.isAllValueNamesConfigured());
  }

  updateForm() {
    this.form.removeControl('httpReads');
    this.form.removeControl('httpWrites');
    this.expandParentForm();
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
