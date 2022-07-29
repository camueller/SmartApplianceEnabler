import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {UntypedFormGroup, Validators} from '@angular/forms';
import {Logger} from '../../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {FormHandler} from '../../../shared/form-handler';
import {ErrorMessages} from '../../../shared/error-messages';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ControlDefaults} from '../../control-defaults';
import {getValidInt, getValidString} from '../../../shared/form-util';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {SocScript} from './soc-script';
import {ElectricVehicle} from './electric-vehicle';
import {TimeUtil} from '../../../shared/time-util';
import {TimepickerComponent} from '../../../material/timepicker/timepicker.component';
import { ViewChild } from '@angular/core';
import {FileMode} from '../../../material/filenameinput/file-mode';
import {FilenameInputComponent} from '../../../material/filenameinput/filename-input.component';

@Component({
  selector: 'app-electric-vehicle',
  templateUrl: './electric-vehicle.component.html',
  styleUrls: ['./electric-vehicle.component.scss'],
})
export class ElectricVehicleComponent implements OnChanges, OnInit {
  @Input()
  electricVehicle: ElectricVehicle;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  form: UntypedFormGroup;
  formHandler: FormHandler;
  @Output()
  remove = new EventEmitter<any>();
  @ViewChild('updateAfterSecondsComponent', {static: true})
  updateAfterSecondsComponent: TimepickerComponent;
  @ViewChild(FilenameInputComponent, {static: true})
  socScriptFilenameInput: FilenameInputComponent;
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
      new ErrorMessage('name', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('batteryCapacity', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('batteryCapacity', ValidatorType.pattern),
      new ErrorMessage('phases', ValidatorType.pattern),
      new ErrorMessage('maxChargePower', ValidatorType.pattern, 'maxChargePower'),
      new ErrorMessage('chargeLoss', ValidatorType.pattern),
      new ErrorMessage('defaultSocManual', ValidatorType.pattern),
      new ErrorMessage('defaultSocOptionalEnergy', ValidatorType.pattern),
      new ErrorMessage('scriptUpdateSocAfterIncrease', ValidatorType.pattern),
      new ErrorMessage('scriptUpdateSocAfterTime', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get updateAfterTime() {
    return this.electricVehicle.socScript && this.electricVehicle.socScript.updateAfterSeconds
      && TimeUtil.toHourMinute(this.electricVehicle.socScript.updateAfterSeconds);
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'id',
      this.electricVehicle && this.electricVehicle.id);
    this.formHandler.addFormControl(this.form, 'name',
      this.electricVehicle && this.electricVehicle.name,
      Validators.required);
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
      [Validators.pattern(InputValidatorPatterns.PERCENTAGE)]);
    this.formHandler.addFormControl(this.form, 'defaultSocOptionalEnergy',
      this.electricVehicle && this.electricVehicle.defaultSocOptionalEnergy,
      [Validators.pattern(InputValidatorPatterns.PERCENTAGE)]);

    this.formHandler.addFormControl(this.form, 'scriptExtractionRegex',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.extractionRegex);
    this.formHandler.addFormControl(this.form, 'pluginStatusExtractionRegex',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.pluginStatusExtractionRegex);
    this.formHandler.addFormControl(this.form, 'pluginTimeExtractionRegex',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.pluginTimeExtractionRegex);
    this.formHandler.addFormControl(this.form, 'latitudeExtractionRegex',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.latitudeExtractionRegex);
    this.formHandler.addFormControl(this.form, 'longitudeExtractionRegex',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.longitudeExtractionRegex);

    this.formHandler.addFormControl(this.form, 'scriptUpdateSocAfterIncrease',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.updateAfterIncrease,
      Validators.pattern(InputValidatorPatterns.PERCENTAGE));
    this.formHandler.addFormControl(this.form, 'scriptUpdateSocAfterSeconds',
      this.electricVehicle && this.electricVehicle.socScript && this.electricVehicle.socScript.updateAfterSeconds,
      Validators.pattern(InputValidatorPatterns.INTEGER));
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
      this.formHandler.setFormControlValue(this.form, 'scriptExtractionRegex', this.electricVehicle.socScript.extractionRegex);
      this.formHandler.setFormControlValue(this.form, 'pluginStatusExtractionRegex', this.electricVehicle.socScript.pluginStatusExtractionRegex);
      this.formHandler.setFormControlValue(this.form, 'pluginTimeExtractionRegex', this.electricVehicle.socScript.pluginTimeExtractionRegex);
      this.formHandler.setFormControlValue(this.form, 'latitudeExtractionRegex', this.electricVehicle.socScript.latitudeExtractionRegex);
      this.formHandler.setFormControlValue(this.form, 'longitudeExtractionRegex', this.electricVehicle.socScript.longitudeExtractionRegex);

      this.formHandler.setFormControlValue(this.form, 'scriptUpdateSocAfterIncrease', this.electricVehicle.socScript.updateAfterIncrease);
      this.formHandler.setFormControlValue(this.form, 'scriptUpdateSocAfterSeconds', this.electricVehicle.socScript.updateAfterSeconds);
    }
  }

  updateModelFromForm(): ElectricVehicle {
    const name = getValidString(this.form.controls.name.value);
    const batteryCapacity = getValidInt(this.form.controls.batteryCapacity.value);
    const phases = getValidInt(this.form.controls.phases.value);
    const maxChargePower = getValidInt(this.form.controls.maxChargePower.value);
    const chargeLoss = getValidInt(this.form.controls.chargeLoss.value);
    const defaultSocManual = getValidInt(this.form.controls.defaultSocManual.value);
    const defaultSocOptionalEnergy = getValidInt(this.form.controls.defaultSocOptionalEnergy.value);
    const scriptFilename = this.socScriptFilenameInput.updateModelFromForm();
    const extractionRegex = getValidString(this.form.controls.scriptExtractionRegex.value);
    const pluginStatusExtractionRegex = getValidString(this.form.controls.pluginStatusExtractionRegex.value);
    const pluginTimeExtractionRegex = getValidString(this.form.controls.pluginTimeExtractionRegex.value);
    const latitudeExtractionRegex = getValidString(this.form.controls.latitudeExtractionRegex.value);
    const longitudeExtractionRegex = getValidString(this.form.controls.longitudeExtractionRegex.value);
    const updateSocAfterIncrease = this.form.controls.scriptUpdateSocAfterIncrease.value;
    const updateSocAfterTime = this.updateAfterSecondsComponent.updateModelFromForm();

    this.electricVehicle.name = name;
    this.electricVehicle.batteryCapacity = batteryCapacity;
    this.electricVehicle.phases = phases;
    this.electricVehicle.maxChargePower = maxChargePower;
    this.electricVehicle.chargeLoss = chargeLoss;
    this.electricVehicle.defaultSocManual = defaultSocManual;
    this.electricVehicle.defaultSocOptionalEnergy = defaultSocOptionalEnergy;

    if (!scriptFilename) {
      this.electricVehicle.socScript = undefined;
    } else {
      if (!this.electricVehicle.socScript && scriptFilename) {
        this.electricVehicle.socScript = new SocScript();
      }
      this.electricVehicle.socScript.script = scriptFilename;
      this.electricVehicle.socScript.extractionRegex = extractionRegex;
      this.electricVehicle.socScript.pluginStatusExtractionRegex = pluginStatusExtractionRegex;
      this.electricVehicle.socScript.pluginTimeExtractionRegex = pluginTimeExtractionRegex;
      this.electricVehicle.socScript.latitudeExtractionRegex = latitudeExtractionRegex;
      this.electricVehicle.socScript.longitudeExtractionRegex = longitudeExtractionRegex;
      this.electricVehicle.socScript.updateAfterIncrease = updateSocAfterIncrease;
      this.electricVehicle.socScript.updateAfterSeconds = updateSocAfterTime
        && updateSocAfterTime.length > 0 ? TimeUtil.toSeconds(updateSocAfterTime) : undefined;
    }
    return this.electricVehicle;
  }

  public get socScriptFilename() {
    return this.electricVehicle?.socScript?.script;
  }

  public get socScriptFileModes() {
    return [FileMode.read, FileMode.execute];
  }

  removeElectricVehicle() {
    this.remove.emit();
  }
}
