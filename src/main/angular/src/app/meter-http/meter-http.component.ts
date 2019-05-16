import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {HttpElectricityMeter} from './http-electricity-meter';
import {MeterDefaults} from '../meter/meter-defaults';
import {FormHandler} from '../shared/form-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {MeterHttpErrorMessages} from './meter-http-error-messages';

@Component({
  selector: 'app-meter-http',
  templateUrl: './meter-http.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterHttpComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input()
  httpElectricityMeter: HttpElectricityMeter;
  @Input()
  meterDefaults: MeterDefaults;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
    // this.translationKeys = [].concat(this.powerValueNameTextKeys, this.energyValueNameTextKeys);
  }

  ngOnInit() {
    this.errorMessages = new MeterHttpErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpElectricityMeter, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
      console.log('errors=', this.errors);
    });
    this.nestedFormService.submitted.subscribe(
      () => this.updateHttpElectricityMeter(this.httpElectricityMeter, this.form));
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    // FIXME: erzeugt Fehler bei Wechsel des Zählertypes
    // this.nestedFormService.submitted.unsubscribe();
  }

  expandParentForm(form: FormGroup, httpElectricityMeter: HttpElectricityMeter, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'pollInterval',
      httpElectricityMeter ? httpElectricityMeter.pollInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'measurementInterval',
      httpElectricityMeter ? httpElectricityMeter.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateHttpElectricityMeter(httpElectricityMeter: HttpElectricityMeter, form: FormGroup) {
    // modbusElectricityMeter.idref = form.controls.idref.value;
    // modbusElectricityMeter.slaveAddress = form.controls.slaveAddress.value;
    // modbusElectricityMeter.pollInterval = form.controls.pollInterval.value;
    // modbusElectricityMeter.measurementInterval = form.controls.measurementInterval.value;
    this.nestedFormService.complete();
  }
}
