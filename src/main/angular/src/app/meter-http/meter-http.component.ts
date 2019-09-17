import {AfterViewChecked, Component, Input, OnInit, ViewChild} from '@angular/core';
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
  @ViewChild('powerHttpRead')
  powerHttpReadComp: HttpReadComponent;
  @ViewChild('energyHttpRead')
  energyHttpReadComp: HttpReadComponent;
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

  get powerValueNames() {
    return ['Power'];
  }

  get powerValueNameTextKeys() {
    return ['MeterHttpComponent.Power'];
  }

  get energyValueNames() {
    return ['Energy'];
  }

  get energyValueNameTextKeys() {
    return ['MeterHttpComponent.Energy'];
  }

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls['contentProtocol'];
    return (contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
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
    const powerHttpRead = this.powerHttpReadComp.updateModelFromForm();
    const energyHttpRead = this.energyHttpReadComp.updateModelFromForm();

    if (!(pollInterval || measurementInterval || contentProtocol || httpConfiguration || powerHttpRead
      || energyHttpRead)) {
      return undefined;
    }

    this.httpElectricityMeter.pollInterval = pollInterval;
    this.httpElectricityMeter.measurementInterval = measurementInterval;
    this.httpElectricityMeter.contentProtocol = contentProtocol;
    this.httpElectricityMeter.httpConfiguration = httpConfiguration;
    this.httpElectricityMeter.powerHttpRead = powerHttpRead;
    this.httpElectricityMeter.energyHttpRead = energyHttpRead;
    return this.httpElectricityMeter;
  }
}
