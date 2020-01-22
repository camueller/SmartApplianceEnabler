import {
  AfterViewChecked,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {FormGroup, Validators} from '@angular/forms';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {SocScript} from '../control-evcharger/soc-script';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ControlDefaults} from '../control/control-defaults';
import {getValidInt, getValidString} from '../shared/form-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

@Component({
  selector: 'app-electric-vehicle',
  templateUrl: './electric-vehicle.component.html',
  styleUrls: ['../global.css'],
})
export class ElectricVehicleComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  electricVehicle: ElectricVehicle;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  form: FormGroup;
  formHandler: FormHandler;
  @Output()
  remove = new EventEmitter<any>();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.electricVehicle) {
      if (changes.electricVehicle.currentValue) {
        this.electricVehicle = changes.electricVehicle.currentValue;
      } else {
        this.electricVehicle = new ElectricVehicle();
      }
      this.updateForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ElectricVehicleComponent.error.', [
      new ErrorMessage('name', ValidatorType.required),
      new ErrorMessage('batteryCapacity', ValidatorType.required),
      new ErrorMessage('batteryCapacity', ValidatorType.pattern),
      new ErrorMessage('phases', ValidatorType.pattern),
      new ErrorMessage('maxChargePower', ValidatorType.pattern, 'maxChargePower'),
      new ErrorMessage('chargeLoss', ValidatorType.pattern),
      new ErrorMessage('defaultSocManual', ValidatorType.pattern),
      new ErrorMessage('defaultSocOptionalEnergy', ValidatorType.pattern),
      new ErrorMessage('scriptFilename', ValidatorType.required),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'id',
      this.electricVehicle && this.electricVehicle.id);
    this.formHandler.addFormControl(this.form, 'name',
      this.electricVehicle && this.electricVehicle.name,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'batteryCapacity',
      this.electricVehicle && this.electricVehicle.batteryCapacity,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'phases',
      this.electricVehicle && this.electricVehicle.phases,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'maxChargePower',
      this.electricVehicle && this.electricVehicle.maxChargePower,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'chargeLoss',
      this.electricVehicle && this.electricVehicle.chargeLoss,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'defaultSocManual',
      this.electricVehicle && this.electricVehicle.defaultSocManual,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'defaultSocOptionalEnergy',
      this.electricVehicle && this.electricVehicle.defaultSocOptionalEnergy,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    const scriptEnabled: boolean = this.electricVehicle && this.electricVehicle.socScript
      && (this.electricVehicle.socScript.script !== undefined);
    this.formHandler.addFormControl(this.form, 'scriptEnabled', scriptEnabled);
    this.formHandler.addFormControl(this.form, 'scriptFilename',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.script);
    this.formHandler.addFormControl(this.form, 'scriptExtractionRegex',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.extractionRegex);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'name', this.electricVehicle.name);
    this.formHandler.setFormControlValue(this.form, 'batteryCapacity', this.electricVehicle.batteryCapacity);
    this.formHandler.setFormControlValue(this.form, 'maxChargePower', this.electricVehicle.maxChargePower);
    this.formHandler.setFormControlValue(this.form, 'chargeLoss', this.electricVehicle.chargeLoss);
    this.formHandler.setFormControlValue(this.form, 'defaultSocManual', this.electricVehicle.defaultSocManual);
    this.formHandler.setFormControlValue(this.form, 'defaultSocOptionalEnergy',
      this.electricVehicle.defaultSocOptionalEnergy);
    if (this.electricVehicle && this.electricVehicle.socScript) {
      this.formHandler.setFormControlValue(this.form, 'scriptFilename', this.electricVehicle.socScript.script);
      this.formHandler.setFormControlValue(this.form, 'scriptExtractionRegex', this.electricVehicle.socScript.extractionRegex);
    }
  }

  updateModelFromForm(): ElectricVehicle {
    const name = getValidString(this.form.controls.name.value);
    const batteryCapacity = getValidInt(this.form.controls.batteryCapacity.value);
    const maxChargePower = getValidInt(this.form.controls.maxChargePower.value);
    const chargeLoss = getValidInt(this.form.controls.chargeLoss.value);
    const defaultSocManual = getValidInt(this.form.controls.defaultSocManual.value);
    const defaultSocOptionalEnergy = getValidInt(this.form.controls.defaultSocOptionalEnergy.value);
    const scriptFilename = this.form.controls.scriptFilename.value;
    const extractionRegex = this.form.controls.scriptExtractionRegex.value;

    this.electricVehicle.name = name;
    this.electricVehicle.batteryCapacity = batteryCapacity;
    this.electricVehicle.maxChargePower = maxChargePower;
    this.electricVehicle.chargeLoss = chargeLoss;
    this.electricVehicle.defaultSocManual = defaultSocManual;
    this.electricVehicle.defaultSocOptionalEnergy = defaultSocOptionalEnergy;

    if (!(scriptFilename || extractionRegex)) {
      this.electricVehicle.socScript = undefined;
    } else {
      if (!this.electricVehicle.socScript && scriptFilename) {
        this.electricVehicle.socScript = new SocScript();
      }
      this.electricVehicle.socScript.script = scriptFilename;
      this.electricVehicle.socScript.extractionRegex = extractionRegex;
    }
    return this.electricVehicle;
  }

  onScriptEnabledToggle(enabled: boolean) {
    const scriptFilenameControl = this.form.controls.scriptFilename;
    const scriptExtractionRegexControl = this.form.controls.scriptExtractionRegex;
    if (enabled) {
      scriptFilenameControl.enable();
      scriptExtractionRegexControl.enable();
    } else {
      scriptFilenameControl.disable();
      scriptExtractionRegexControl.disable();
    }
  }

  isScriptEnabled() {
    return this.form.controls.scriptEnabled.enabled;
  }

  removeElectricVehicle() {
    this.remove.emit();
  }
}
