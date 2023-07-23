import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Logger} from '../../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {ErrorMessages} from '../../../shared/error-messages';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ControlDefaults} from '../../control-defaults';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {SocScript} from './soc-script';
import {ElectricVehicle} from './electric-vehicle';
import {TimeUtil} from '../../../shared/time-util';
import {TimepickerComponent} from '../../../material/timepicker/timepicker.component';
import {FileMode} from '../../../material/filenameinput/file-mode';
import {FilenameInputComponent} from '../../../material/filenameinput/filename-input.component';
import {ElectricVehicleModel} from './electric-vehicle.model';
import {isRequired} from 'src/app/shared/form-util';

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
  form: FormGroup<ElectricVehicleModel>;
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
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.electricVehicle) {
      if (changes.electricVehicle.currentValue) {
        this.electricVehicle = changes.electricVehicle.currentValue;
      } else {
        this.electricVehicle = new ElectricVehicle();
      }
      if(! changes.electricVehicle.isFirstChange()) {
        this.updateForm();
      }
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
      new ErrorMessage('scriptTimeoutSeconds', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get updateAfterTime() {
    return this.electricVehicle?.socScript?.updateAfterSeconds
      && TimeUtil.toHourMinute(this.electricVehicle.socScript.updateAfterSeconds);
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('id', new FormControl(this.electricVehicle?.id));
    this.form.addControl('name', new FormControl(this.electricVehicle?.name, Validators.required));
    this.form.addControl('batteryCapacity', new FormControl(this.electricVehicle?.batteryCapacity,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]));
    this.form.addControl('phases', new FormControl(this.electricVehicle?.phases,
      Validators.pattern(InputValidatorPatterns.INTEGER)))
    this.form.addControl('maxChargePower', new FormControl(this.electricVehicle?.maxChargePower,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('chargeLoss', new FormControl(this.electricVehicle?.chargeLoss,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('defaultSocManual', new FormControl(this.electricVehicle?.defaultSocManual,
      Validators.pattern(InputValidatorPatterns.PERCENTAGE)));
    this.form.addControl('defaultSocOptionalEnergy', new FormControl(this.electricVehicle?.defaultSocOptionalEnergy,
      Validators.pattern(InputValidatorPatterns.PERCENTAGE)));
    const socScript = this.electricVehicle?.socScript;
    this.form.addControl('scriptExtractionRegex', new FormControl(socScript?.extractionRegex));
    this.form.addControl('pluginStatusExtractionRegex', new FormControl(socScript?.pluginStatusExtractionRegex));
    this.form.addControl('pluginTimeExtractionRegex', new FormControl(socScript?.pluginTimeExtractionRegex));
    this.form.addControl('latitudeExtractionRegex', new FormControl(socScript?.latitudeExtractionRegex));
    this.form.addControl('longitudeExtractionRegex', new FormControl(socScript?.longitudeExtractionRegex));

    this.form.addControl('scriptUpdateSocAfterIncrease', new FormControl(socScript?.updateAfterIncrease,
      Validators.pattern(InputValidatorPatterns.PERCENTAGE)));
    this.form.addControl('scriptUpdateSocAfterSeconds', new FormControl(socScript?.updateAfterSeconds,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('scriptTimeoutSeconds', new FormControl(socScript?.timeoutSeconds,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
  }

  updateForm() {
    this.form.controls.name.setValue(this.electricVehicle.name);
    this.form.controls.batteryCapacity.setValue(this.electricVehicle.batteryCapacity);
    this.form.controls.maxChargePower.setValue(this.electricVehicle.maxChargePower);
    this.form.controls.chargeLoss.setValue(this.electricVehicle.chargeLoss);
    this.form.controls.defaultSocManual.setValue(this.electricVehicle.defaultSocManual);
    this.form.controls.defaultSocOptionalEnergy.setValue(this.electricVehicle.defaultSocOptionalEnergy);
    const socScript = this.electricVehicle?.socScript;
    if(!!socScript) {
      this.form.controls.scriptExtractionRegex.setValue(socScript.extractionRegex);
      this.form.controls.pluginStatusExtractionRegex.setValue(socScript.pluginStatusExtractionRegex);
      this.form.controls.pluginTimeExtractionRegex.setValue(socScript.pluginTimeExtractionRegex);
      this.form.controls.latitudeExtractionRegex.setValue(socScript.latitudeExtractionRegex);
      this.form.controls.longitudeExtractionRegex.setValue(socScript.longitudeExtractionRegex);

      this.form.controls.scriptUpdateSocAfterIncrease.setValue(socScript.updateAfterIncrease);
      this.form.controls.scriptUpdateSocAfterSeconds.setValue(socScript.updateAfterSeconds);
      this.form.controls.scriptTimeoutSeconds.setValue(socScript.timeoutSeconds);
    }
  }

  updateModelFromForm(): ElectricVehicle {
    const name = this.form.controls.name.value;
    const batteryCapacity = this.form.controls.batteryCapacity.value;
    const phases = this.form.controls.phases.value;
    const maxChargePower = this.form.controls.maxChargePower.value;
    const chargeLoss = this.form.controls.chargeLoss.value;
    const defaultSocManual = this.form.controls.defaultSocManual.value;
    const defaultSocOptionalEnergy = this.form.controls.defaultSocOptionalEnergy.value;
    const scriptFilename = this.socScriptFilenameInput.updateModelFromForm();
    const scriptTimeoutSeconds = this.form.controls.scriptTimeoutSeconds?.value;
    const extractionRegex = this.form.controls.scriptExtractionRegex?.value;
    const pluginStatusExtractionRegex = this.form.controls.pluginStatusExtractionRegex?.value;
    const pluginTimeExtractionRegex = this.form.controls.pluginTimeExtractionRegex?.value;
    const latitudeExtractionRegex = this.form.controls.latitudeExtractionRegex?.value;
    const longitudeExtractionRegex = this.form.controls.longitudeExtractionRegex?.value;
    const updateSocAfterIncrease = this.form.controls.scriptUpdateSocAfterIncrease?.value;
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
      this.electricVehicle.socScript.timeoutSeconds = scriptTimeoutSeconds;
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
