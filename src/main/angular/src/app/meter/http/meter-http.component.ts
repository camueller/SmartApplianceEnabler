import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {
  ControlContainer,
  FormControl,
  FormGroup,
  FormGroupDirective,
  Validators
} from '@angular/forms';
import {HttpElectricityMeter} from './http-electricity-meter';
import {ContentProtocol} from '../../shared/content-protocol';
import {HttpReadComponent} from '../../http/read/http-read.component';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../../shared/form-util';
import {MeterDefaults} from '../meter-defaults';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {MeterValueName} from '../meter-value-name';
import {HttpRead} from '../../http/read/http-read';
import {HttpConfigurationComponent} from '../../http/configuration/http-configuration.component';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ValueNameChangedEvent} from '../value-name-changed-event';
import {MeterHttpModel} from './meter-http.model';

@Component({
  selector: 'app-meter-http',
  templateUrl: './meter-http.component.html',
  styleUrls: ['./meter-http-component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeterHttpComponent implements OnChanges, OnInit {
  @Input()
  httpElectricityMeter: HttpElectricityMeter;
  @ViewChild(HttpConfigurationComponent, {static: true})
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChild(HttpReadComponent, {static: true})
  httpReadComp: HttpReadComponent;
  @Input()
  meterDefaults: MeterDefaults;
  readValueName: MeterValueName;
  contentProtocols = [undefined, ContentProtocol.JSON.toUpperCase()];
  form: FormGroup<MeterHttpModel>;
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
    if (changes.httpElectricityMeter) {
      if (changes.httpElectricityMeter.currentValue) {
        this.httpElectricityMeter = changes.httpElectricityMeter.currentValue;
      } else {
        this.httpElectricityMeter = new HttpElectricityMeter();
        this.httpElectricityMeter.httpReads = [HttpRead.createWithSingleChild()];
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterHttpComponent.error.', [
      new ErrorMessage('pollInterval', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get valueNames() {
    return [MeterValueName.Energy, MeterValueName.Power];
  }

  get valueNameTextKeys() {
    return ['MeterHttpComponent.Energy', 'MeterHttpComponent.Power'];
  }

  onNameChanged(event: ValueNameChangedEvent) {
    if (event.name === MeterValueName.Energy) {
      this.readValueName = MeterValueName.Energy;
    } else if (event.name === MeterValueName.Power) {
      this.readValueName = MeterValueName.Power;
    }
  }

  get displayPollInterval(): boolean {
    return this.readValueName === MeterValueName.Power;
  }

  get contentProtocol(): string {
    const contentProtocolControl = this.form.controls.contentProtocol;
    return (contentProtocolControl && contentProtocolControl.value ? contentProtocolControl.value.toUpperCase() : '');
  }

  get isAddHttpReadPossible() {
    return this.httpElectricityMeter.httpReads.length === 0;
  }

  get maxValues() {
    return 1;
  }

  get httpReadsFormArray() {
    return this.form.controls.httpReads;
  }

  getHttpReadFormGroup(index: number) {
    return this.httpReadsFormArray.controls[index];
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('pollInterval', new FormControl( this.httpElectricityMeter.pollInterval,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('contentProtocol', new FormControl(this.httpElectricityMeter.contentProtocol));
    this.form.addControl('httpReads', buildFormArrayWithEmptyFormGroups(this.httpElectricityMeter.httpReads));
  }

  updateModelFromForm(): HttpElectricityMeter | undefined {
    const pollInterval = this.form.controls.pollInterval.value;
    const contentProtocol = this.form.controls.contentProtocol.value;
    const httpConfiguration = this.httpConfigurationComp.updateModelFromForm();
    const httpRead = this.httpReadComp.updateModelFromForm();

    if (!(pollInterval || contentProtocol || httpConfiguration || httpRead)) {
      return undefined;
    }

    this.httpElectricityMeter.pollInterval = pollInterval;
    this.httpElectricityMeter.contentProtocol = contentProtocol;
    this.httpElectricityMeter.httpConfiguration = httpConfiguration;
    this.httpElectricityMeter.httpReads = httpRead ? [httpRead] : undefined;
    return this.httpElectricityMeter;
  }
}
