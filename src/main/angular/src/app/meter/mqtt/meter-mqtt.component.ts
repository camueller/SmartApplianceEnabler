import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroupDirective, UntypedFormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {MeterDefaults} from '../meter-defaults';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {MqttElectricityMeter} from './mqtt-electricity-meter';
import {MeterValueName} from '../meter-value-name';
import {FormHandler} from '../../shared/form-handler';
import {Logger} from '../../log/logger';
import {ValueNameChangedEvent} from '../value-name-changed-event';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {getValidFloat, getValidString} from '../../shared/form-util';
import {ContentProtocol} from '../../shared/content-protocol';

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
  form: UntypedFormGroup;
  formHandler: FormHandler;
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
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

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'topic', this.mqttElectricityMeter.topic,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'name', this.mqttElectricityMeter.name,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'contentProtocol', this.mqttElectricityMeter.contentProtocol);
    this.formHandler.addFormControl(this.form, 'path', this.mqttElectricityMeter.path);
    this.formHandler.addFormControl(this.form, 'timePath', this.mqttElectricityMeter.timePath);
    this.formHandler.addFormControl(this.form, 'factorToValue', this.mqttElectricityMeter.factorToValue,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
  }

  updateModelFromForm(): MqttElectricityMeter | undefined {
    const topic = getValidString(this.form.controls.topic.value);
    const name = getValidString(this.form.controls.name.value);
    const contentProtocol = this.form.controls.contentProtocol.value;
    const path = !!contentProtocol ? getValidString(this.form.controls.path.value) : undefined;
    const timePath = !!contentProtocol ? getValidString(this.form.controls.timePath.value) : undefined;
    const factorToValue = getValidFloat(this.form.controls.factorToValue.value);

    if (!(topic || name || contentProtocol || path || timePath || factorToValue)) {
      return undefined;
    }

    this.mqttElectricityMeter.topic = topic;
    this.mqttElectricityMeter.name = name;
    this.mqttElectricityMeter.contentProtocol = contentProtocol;
    this.mqttElectricityMeter.path = path;
    this.mqttElectricityMeter.timePath = timePath;
    this.mqttElectricityMeter.factorToValue = factorToValue;
    return this.mqttElectricityMeter;
  }
}
