import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {ControlContainer, FormGroupDirective, UntypedFormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {MqttSwitch} from './mqtt-switch';
import {FormHandler} from '../../shared/form-handler';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ControlValueName} from '../control-value-name';
import {getValidString} from '../../shared/form-util';

@Component({
  selector: 'app-control-mqtt',
  templateUrl: './control-mqtt.component.html',
  styleUrls: ['./control-mqtt.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ControlMqttComponent implements OnChanges, OnInit {
  @Input()
  mqttSwitch: MqttSwitch;
  @Input()
  form: UntypedFormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
              private changeDetectorRef: ChangeDetectorRef
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.mqttSwitch) {
      if (changes.mqttSwitch.currentValue) {
        this.mqttSwitch = changes.mqttSwitch.currentValue;
      } else {
        this.mqttSwitch = new MqttSwitch();
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlHttpComponent.error.', [
      new ErrorMessage('topic', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('onPayload', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('offPayload', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'topic', this.mqttSwitch.topic,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'onPayload', this.mqttSwitch.onPayload,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'offPayload', this.mqttSwitch.offPayload,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'statusTopic', this.mqttSwitch.statusTopic);
    this.formHandler.addFormControl(this.form, 'statusExtractionRegex', this.mqttSwitch.statusExtractionRegex);
  }

  updateModelFromForm(): MqttSwitch | undefined {
    const topic = getValidString(this.form.controls.topic.value);
    const onPayload = getValidString(this.form.controls.onPayload.value);
    const offPayload = getValidString(this.form.controls.offPayload.value);
    const statusTopic = getValidString(this.form.controls.statusTopic.value);
    const statusExtractionRegex = getValidString(this.form.controls.statusExtractionRegex.value);

    if (!(topic || onPayload || offPayload || statusTopic || statusExtractionRegex)) {
      return undefined;
    }

    this.mqttSwitch.topic = topic;
    this.mqttSwitch.onPayload = onPayload;
    this.mqttSwitch.offPayload = offPayload;
    this.mqttSwitch.statusTopic = statusTopic;
    this.mqttSwitch.statusExtractionRegex = statusExtractionRegex;
    return this.mqttSwitch;
  }
}
