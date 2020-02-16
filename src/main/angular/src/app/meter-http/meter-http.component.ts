import {
  AfterViewChecked,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
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
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidInt} from '../shared/form-util';
import {HttpConfigurationComponent} from '../http-configuration/http-configuration.component';
import {HttpReadComponent} from '../http-read/http-read.component';
import {HttpRead} from '../http-read/http-read';
import {MeterValueName} from '../meter/meter-value-name';

@Component({
  selector: 'app-meter-http',
  templateUrl: './meter-http.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterHttpComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  httpElectricityMeter: HttpElectricityMeter;
  @ViewChild(HttpConfigurationComponent, {static: true})
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChildren('httpReadComponents')
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

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.httpElectricityMeter) {
      if (changes.httpElectricityMeter.currentValue) {
        this.httpElectricityMeter = changes.httpElectricityMeter.currentValue;
      } else {
        this.httpElectricityMeter = new HttpElectricityMeter();
        this.httpElectricityMeter.httpReads = [HttpRead.createWithSingleChild()];
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterHttpComponent.error.', [
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('measurementInterval', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
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

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls.contentProtocol;
    return (contentProtocolControl && contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
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
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    this.httpElectricityMeter.httpReads.push(HttpRead.createWithSingleChild());
    this.httpReadsFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  onHttpReadRemove(index: number) {
    this.httpElectricityMeter.httpReads.splice(index, 1);
    this.httpReadsFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get httpReadsFormArray() {
    return this.form.controls.httpReads as FormArray;
  }

  getHttpReadFormGroup(index: number) {
    return this.httpReadsFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'pollInterval', this.httpElectricityMeter.pollInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'measurementInterval', this.httpElectricityMeter.measurementInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'contentProtocol', this.httpElectricityMeter.contentProtocol);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'httpReads',
      this.httpElectricityMeter.httpReads);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'pollInterval', this.httpElectricityMeter.pollInterval);
    this.formHandler.setFormControlValue(this.form, 'measurementInterval', this.httpElectricityMeter.measurementInterval);
    this.formHandler.setFormControlValue(this.form, 'contentProtocol', this.httpElectricityMeter.contentProtocol);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'httpReads',
      this.httpElectricityMeter.httpReads);
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
