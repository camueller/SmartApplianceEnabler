import {AfterViewChecked, Component, Input, OnInit, QueryList, ViewChild} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {HttpElectricityMeter} from './http-electricity-meter';
import {MeterDefaults} from '../meter/meter-defaults';
import {FormHandler} from '../shared/form-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ContentProtocol} from '../shared/content-protocol';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {getValidInt} from '../shared/form-util';
import {HttpConfigurationComponent} from '../http-configuration/http-configuration.component';
import {HttpReadComponent} from '../http-read/http-read.component';
import {HttpRead} from '../http-read/http-read';
import {HttpReadValue} from '../http-read-value/http-read-value';
import {MeterValueName} from '../meter/meter-value-name';

@Component({
  selector: 'app-meter-http',
  templateUrl: './meter-http.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterHttpComponent implements OnInit, AfterViewChecked {
  @Input()
  httpElectricityMeter: HttpElectricityMeter;
  @ViewChild(HttpConfigurationComponent)
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChild('httpReadComponents')
  httpReadComps: QueryList<HttpReadComponent>;
  @Input()
  meterDefaults: MeterDefaults;
  contentProtocols = [undefined, ContentProtocol.JSON.toUpperCase()];
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
    this.httpElectricityMeter = this.httpElectricityMeter || new HttpElectricityMeter();
    if (!this.httpElectricityMeter.httpReads) {
      this.httpElectricityMeter.httpReads = [this.createHttpRead()];
    }
    this.errorMessages = new ErrorMessages('MeterHttpComponent.error.', [
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('measurementInterval', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpElectricityMeter, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get valueNames() {
    return [MeterValueName.Power, MeterValueName.Energy];
  }

  get valueNameTextKeys() {
    return ['MeterHttpComponent.Power', 'MeterHttpComponent.Energy'];
  }

  getReadFormControlPrefix(index: number) {
    return `read${index}.`;
  }

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls['contentProtocol'];
    return (contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
  }

  get isAddHttpReadPossible() {
    if (this.httpElectricityMeter.httpReads.length === 1) {
      return this.httpElectricityMeter.httpReads[0].readValues.length < 2;
    }
    return this.httpElectricityMeter.httpReads.length < 2;
  }

  get maxValues() {
    return this.httpElectricityMeter.httpReads.length === 2 ? 1 : 2;
  }

  addHttpRead() {
    this.httpElectricityMeter.httpReads.push(this.createHttpRead());
    this.form.markAsDirty();
  }

  onHttpReadRemove(index: number) {
    this.httpElectricityMeter.httpReads.splice(index, 1);
  }

  createHttpRead() {
    const httpRead = new HttpRead();
    httpRead.readValues = [new HttpReadValue()];
    return httpRead;
  }

  expandParentForm(form: FormGroup, httpElectricityMeter: HttpElectricityMeter, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'pollInterval',
      httpElectricityMeter ? httpElectricityMeter.pollInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'measurementInterval',
      httpElectricityMeter ? httpElectricityMeter.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'contentProtocol',
      httpElectricityMeter ? httpElectricityMeter.contentProtocol : undefined);
  }

  updateModelFromForm(): HttpElectricityMeter | undefined {
    const pollInterval = getValidInt(this.form.controls.pollInterval.value);
    const measurementInterval = getValidInt(this.form.controls.measurementInterval.value);
    const contentProtocol = this.form.controls.contentProtocol.value;
    const httpConfiguration = this.httpConfigurationComp.updateModelFromForm();
    const httpReads = [];
    this.httpReadComps.forEach(httpReadComponent => {
      const httpRead = httpReadComponent.updateModelFromForm();
      if (httpRead) {
        httpReads.push(httpRead);
      }
    });

    if (!(pollInterval || measurementInterval || contentProtocol || httpConfiguration || httpReads.length > 0)) {
      return undefined;
    }

    this.httpElectricityMeter.pollInterval = pollInterval;
    this.httpElectricityMeter.measurementInterval = measurementInterval;
    this.httpElectricityMeter.contentProtocol = contentProtocol;
    this.httpElectricityMeter.httpConfiguration = httpConfiguration;
    this.httpElectricityMeter.httpReads = httpReads;
    return this.httpElectricityMeter;
  }
}
