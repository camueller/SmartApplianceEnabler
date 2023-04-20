import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {DayTimeframe} from './day-timeframe';
import {ErrorMessages} from '../../../shared/error-messages';
import {TimeOfDay} from '../../time-of-day';
import {DayOfWeek, DaysOfWeek} from '../../../shared/days-of-week';
import {TimeUtil} from '../../../shared/time-util';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {TimepickerComponent} from '../../../material/timepicker/timepicker.component';
import {ScheduleTimeframeDayModel} from './schedule-timeframe-day.model';
import {isRequired} from 'src/app/shared/form-util';

@Component({
  selector: 'app-schedule-timeframe-day',
  templateUrl: './schedule-timeframe-day.component.html',
  styleUrls: ['./schedule-timeframe-day.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleTimeframeDayComponent implements OnChanges, OnInit {
  @Input()
  dayTimeFrame: DayTimeframe;
  @Input()
  enabled: boolean;
  @ViewChild('startTimeComponent', {static: true})
  startTimeComp: TimepickerComponent;
  @ViewChild('endTimeComponent', {static: true})
  endTimeComp: TimepickerComponent;
  form: FormGroup<ScheduleTimeframeDayModel>;
  daysOfWeek: DayOfWeek[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.dayTimeFrame) {
      if (changes.dayTimeFrame.currentValue) {
        this.dayTimeFrame = changes.dayTimeFrame.currentValue;
      } else {
        this.dayTimeFrame = new DayTimeframe();
      }
      this.expandParentForm();
    }
    if (changes.enabled && !changes.enabled.firstChange) {
      this.setEnabled(changes.enabled.currentValue);
    }
  }

  ngOnInit() {
    DaysOfWeek.getDows(this.translate).subscribe(daysOfWeek => this.daysOfWeek = daysOfWeek);
    this.errorMessages = new ErrorMessages('ScheduleTimeframeDayComponent.error.', [
      new ErrorMessage('startTime', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('startTime', ValidatorType.pattern),
      new ErrorMessage('endTime', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('endTime', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get startTime() {
    return this.dayTimeFrame.start && TimeUtil.timestringFromTimeOfDay(this.dayTimeFrame.start);
  }

  get endTime() {
    return this.dayTimeFrame.end && TimeUtil.timestringFromTimeOfDay(this.dayTimeFrame.end);
  }

  get daysOfWeekValues() {
    return this.dayTimeFrame.daysOfWeek && TimeUtil.toDayOfWeekValues(this.dayTimeFrame.daysOfWeek);
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.daysOfWeekValues.enable();
    } else {
      this.form.controls.daysOfWeekValues.disable();
    }
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('daysOfWeekValues', new FormControl(this.daysOfWeekValues));
  }

  updateModelFromForm(): DayTimeframe | undefined {
    const daysOfWeekValues = this.form.controls.daysOfWeekValues.value;
    const startTime = this.startTimeComp.updateModelFromForm();
    const endTime = this.endTimeComp.updateModelFromForm();

    if (!(daysOfWeekValues || startTime || endTime)) {
      return undefined;
    }

    this.dayTimeFrame.daysOfWeek = TimeUtil.toDaysOfWeek(daysOfWeekValues);
    this.dayTimeFrame.start = TimeOfDay.fromString(startTime);
    this.dayTimeFrame.end = TimeOfDay.fromString(endTime);
    return this.dayTimeFrame;
  }
}
