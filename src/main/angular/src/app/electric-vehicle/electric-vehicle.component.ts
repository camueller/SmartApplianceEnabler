import {AfterViewChecked, Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {Subscription} from 'rxjs';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {SocScript} from '../control-evcharger/soc-script';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ControlDefaults} from '../control/control-defaults';
import {getValidInt, getValidString} from '../shared/form-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

@Component({
  selector: 'app-electric-vehicle',
  templateUrl: './electric-vehicle.component.html',
  styles: []
})
export class ElectricVehicleComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input()
  electricVehicle: ElectricVehicle;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  formControlNamePrefix = '';
  form: FormGroup;
  formHandler: FormHandler;
  @Output()
  remove = new EventEmitter<any>();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  nestedFormServiceSubscription: Subscription;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ElectricVehicleComponent.error.', [
      new ErrorMessage(this.getFormControlName('name'), ValidatorType.required, 'name'),
      new ErrorMessage(this.getFormControlName('batteryCapacity'), ValidatorType.required, 'batteryCapacity'),
      new ErrorMessage(this.getFormControlName('batteryCapacity'), ValidatorType.pattern, 'batteryCapacity'),
      new ErrorMessage(this.getFormControlName('phases'), ValidatorType.pattern, 'phases'),
      new ErrorMessage(this.getFormControlName('maxChargePower'), ValidatorType.pattern, 'maxChargePower'),
      new ErrorMessage(this.getFormControlName('chargeLoss'), ValidatorType.pattern, 'chargeLoss'),
      new ErrorMessage(this.getFormControlName('defaultSocManual'), ValidatorType.pattern, 'defaultSocManual'),
      new ErrorMessage(this.getFormControlName('defaultSocOptionalEnergy'), ValidatorType.pattern, 'defaultSocOptionalEnergy'),
      new ErrorMessage(this.getFormControlName('scriptFilename'), ValidatorType.required, 'scriptFilename'),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.electricVehicle);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.nestedFormServiceSubscription = this.nestedFormService.submitted.subscribe(
      () => this.updateModelFromForm(this.electricVehicle, this.form));
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    this.nestedFormServiceSubscription.unsubscribe();
  }

  expandParentForm(form: FormGroup, ev: ElectricVehicle) {
    this.formHandler.addFormControl(form, this.getFormControlName('id'),
      ev && ev.id);
    this.formHandler.addFormControl(form, this.getFormControlName('name'),
      ev && ev.name,
      [Validators.required]);
    this.formHandler.addFormControl(form, this.getFormControlName('batteryCapacity'),
      ev && ev.batteryCapacity,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, this.getFormControlName('phases'),
      ev && ev.phases,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, this.getFormControlName('maxChargePower'),
      ev && ev.maxChargePower,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, this.getFormControlName('chargeLoss'),
      ev && ev.chargeLoss,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, this.getFormControlName('defaultSocManual'),
      ev && ev.defaultSocManual,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, this.getFormControlName('defaultSocOptionalEnergy'),
      ev && ev.defaultSocOptionalEnergy,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    const scriptEnabled: boolean = ev && ev.socScript && (ev.socScript.script !== undefined);
    this.formHandler.addFormControl(form, this.getFormControlName('scriptEnabled'), scriptEnabled);
    this.formHandler.addFormControl(form, this.getFormControlName('scriptFilename'),
      ev && ev.socScript && ev.socScript.script);
    this.formHandler.addFormControl(form, this.getFormControlName('scriptExtractionRegex'),
      ev && ev.socScript && ev.socScript.extractionRegex);
  }

  updateModelFromForm(electricVehicle: ElectricVehicle, evFormControl: FormGroup) {
    electricVehicle.name = getValidString(
      evFormControl.controls[this.getFormControlName('name')].value);
    electricVehicle.batteryCapacity = getValidInt(
      evFormControl.controls[this.getFormControlName('batteryCapacity')].value);
    electricVehicle.maxChargePower = getValidInt(
      evFormControl.controls[this.getFormControlName('maxChargePower')].value);
    electricVehicle.chargeLoss = getValidInt(
      evFormControl.controls[this.getFormControlName('chargeLoss')].value);
    electricVehicle.defaultSocManual = getValidInt(
      evFormControl.controls[this.getFormControlName('defaultSocManual')].value);
    electricVehicle.defaultSocOptionalEnergy = getValidInt(
      evFormControl.controls[this.getFormControlName('defaultSocOptionalEnergy')].value);

    const scriptFilename = evFormControl.controls[this.getFormControlName('scriptFilename')].value;
    const extractionRegex = evFormControl.controls[this.getFormControlName('scriptExtractionRegex')].value;
    if (!electricVehicle.socScript && scriptFilename) {
      electricVehicle.socScript = new SocScript();
    }
    if (electricVehicle.socScript) {
      electricVehicle.socScript.script = scriptFilename;
      electricVehicle.socScript.extractionRegex = extractionRegex;
    }
    this.nestedFormService.complete();
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  onScriptEnabledToggle(enabled: boolean) {
    const scriptFilenameControl = this.form.controls[this.getFormControlName('scriptFilename')];
    const scriptExtractionRegexControl = this.form.controls[this.getFormControlName('scriptExtractionRegex')];
    if (enabled) {
      scriptFilenameControl.enable();
      scriptExtractionRegexControl.enable();
    } else {
      scriptFilenameControl.disable();
      scriptExtractionRegexControl.disable();
    }
  }

  isScriptEnabled() {
    return this.form.controls[this.getFormControlName('scriptEnabled')].enabled;
  }

  removeElectricVehicle() {
    this.remove.emit();
  }
}
