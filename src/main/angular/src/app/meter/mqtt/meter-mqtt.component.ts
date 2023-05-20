import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {MeterDefaults} from '../meter-defaults';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {MqttElectricityMeter} from './mqtt-electricity-meter';
import {MeterValueName} from '../meter-value-name';
import {Logger} from '../../log/logger';
import {ValueNameChangedEvent} from '../value-name-changed-event';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ContentProtocol} from '../../shared/content-protocol';
import {MeterMqttModel} from './meter-mqtt.model';
import {getValidString, isRequired} from 'src/app/shared/form-util';

@Component({
  selector: 'app-meter-mqtt',
  templateUrl: './meter-mqtt.component.html',
  styleUrls: ['./meter-mqtt.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeterMqttComponent implements OnChanges, OnInit {
  @Input()
  mqttElectricityMeter: MqttElectricityMeter;
  @Input()
  meterDefaults: MeterDefaults;
  contentProtocols = [undefined, ContentProtocol.JSON.toUpperCase()];
  readValueName: MeterValueName;
  form: FormGroup<MeterMqttModel>;
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.mqttElectricityMeter) {
      if (changes.mqttElectricityMeter.currentValue) {
        this.mqttElectricityMeter = changes.mqttElectricityMeter.currentValue;
      } else {
        this.mqttElectricityMeter = new MqttElectricityMeter();
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterMqttComponent.error.', [
      new ErrorMessage('topic', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('name', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('factorToValue', ValidatorType.pattern)
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.translate.get(this.valueNameTextKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  get valueNames() {
    return [MeterValueName.Energy, MeterValueName.Power];
  }

  get valueNameTextKeys() {
    return ['MeterMqttComponent.Energy', 'MeterMqttComponent.Power'];
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `MeterMqttComponent.${valueName}`;
    return this.translatedStrings[textKey];
  }

  onNameChanged(event: ValueNameChangedEvent) {
    if (event.name === MeterValueName.Energy) {
      this.readValueName = MeterValueName.Energy;
    } else if (event.name === MeterValueName.Power) {
      this.readValueName = MeterValueName.Power;
    }
  }

  onContentProtocolChanged(value: string) {
    if (value === ContentProtocol.JSON) {
      this.form.controls.path?.enable();
      this.form.controls.timePath?.enable();
    } else {
      this.form.controls.path?.disable();
      this.form.controls.timePath?.disable();
    }
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('topic', new FormControl(this.mqttElectricityMeter.topic, Validators.required));
    this.form.addControl('name', new FormControl(this.mqttElectricityMeter.name, Validators.required));
    this.form.addControl('contentProtocol', new FormControl(this.mqttElectricityMeter.contentProtocol));
    this.form.addControl('path', new FormControl(this.mqttElectricityMeter.path));
    this.form.addControl('timePath', new FormControl(this.mqttElectricityMeter.timePath));
    this.form.addControl('extractionRegex', new FormControl(this.mqttElectricityMeter?.extractionRegex));
    this.form.addControl('factorToValue', new FormControl(this.mqttElectricityMeter.factorToValue,
      Validators.pattern(InputValidatorPatterns.FLOAT)));
    this.onContentProtocolChanged(this.mqttElectricityMeter.contentProtocol);
  }

  updateModelFromForm(): MqttElectricityMeter | undefined {
    const topic = this.form.controls.topic.value;
    const name = this.form.controls.name.value;
    const contentProtocol = this.form.controls.contentProtocol.value;
    const path = !!contentProtocol ? this.form.controls.path.value : undefined;
    const timePath = !!contentProtocol ? this.form.controls.timePath.value : undefined;
    const extractionRegex = getValidString(this.form.controls.extractionRegex.value);
    const factorToValue = this.form.controls.factorToValue.value;

    if (!(topic || name || contentProtocol || path || timePath || extractionRegex || factorToValue)) {
      return undefined;
    }

    this.mqttElectricityMeter.topic = topic;
    this.mqttElectricityMeter.name = name;
    this.mqttElectricityMeter.contentProtocol = contentProtocol;
    this.mqttElectricityMeter.path = path;
    this.mqttElectricityMeter.timePath = timePath;
    this.mqttElectricityMeter.extractionRegex = extractionRegex;
    this.mqttElectricityMeter.factorToValue = factorToValue;
    return this.mqttElectricityMeter;
  }
}
