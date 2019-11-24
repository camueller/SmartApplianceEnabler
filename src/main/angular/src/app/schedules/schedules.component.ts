import {Component, OnChanges, OnInit, QueryList, SimpleChanges, ViewChildren} from '@angular/core';
import {Logger} from '../log/logger';
import {ActivatedRoute} from '@angular/router';
import {Schedule} from '../schedule/schedule';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {FormGroup} from '@angular/forms';
import {ScheduleService} from '../schedule/schedule-service';
import {FormHandler} from '../shared/form-handler';
import {DayTimeframe} from '../schedule-timeframe-day/day-timeframe';
import {ConsecutiveDaysTimeframe} from '../schedule-timeframe-consecutivedays/consecutive-days-timeframe';
import {TranslateService} from '@ngx-translate/core';
import {RuntimeRequest} from '../schedule-request-runtime/runtime-request';
import {EnergyRequest} from '../schedule-request-energy/energy-request';
import {SocRequest} from '../schedule-request-soc/soc-request';
import {ScheduleComponent} from '../schedule/schedule.component';

@Component({
  selector: 'app-schedules',
  templateUrl: './schedules.component.html',
  styleUrls: ['../global.css'],
})
export class SchedulesComponent implements OnChanges, OnInit {
  @ViewChildren('scheduleComponents')
  scheduleComps: QueryList<ScheduleComponent>;
  form: FormGroup;
  formHandler: FormHandler;
  schedules: Schedule[];
  electricVehicles: ElectricVehicle[];
  applianceId: string;
  timeframeTypes: {key: string, value?: string}[] = [
    { key: DayTimeframe.TYPE },
    { key: ConsecutiveDaysTimeframe.TYPE },
  ];
  evRequestTypes: {key: string, value?: string}[] = [
    { key: RuntimeRequest.TYPE },
    { key: EnergyRequest.TYPE },
    { key: SocRequest.TYPE },
  ];
  nonEvRequestTypes: {key: string, value?: string}[] = [];

  constructor(private logger: Logger,
              private route: ActivatedRoute,
              private scheduleService: ScheduleService,
              private translate: TranslateService
              ) {
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // if (changes.control && changes.schedules.currentValue) {
    //   this.schedules = changes.schedules.currentValue;
    // }
    // if (this.form) {
    //   this.updateForm(this.form, this.schedules, this.formHandler);
    // }
  }

  ngOnInit() {
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {schedules: Schedule[], electricVehicles: ElectricVehicle[]}) => {
      this.schedules = data.schedules;
      this.electricVehicles = data.electricVehicles;
    });
    this.form = this.buildForm(this.schedules, this.formHandler);

    const timeframeTypeKeys = this.timeframeTypes.map(timeframeType => timeframeType.key);
    this.translate.get(timeframeTypeKeys).subscribe(
      translatedKeys => {
        this.timeframeTypes.forEach(timeframeType => timeframeType.value = translatedKeys[timeframeType.key]);
      });

    const requestTypeKeys = this.evRequestTypes.map(requestType => requestType.key);
    this.translate.get(requestTypeKeys).subscribe(
      translatedKeys => {
        this.evRequestTypes.forEach(requestType => requestType.value = translatedKeys[requestType.key]);
        this.nonEvRequestTypes = this.evRequestTypes.filter(requestType => requestType.key === RuntimeRequest.TYPE);
      });

  }

  getFormControlPrefix(index: number) {
    return `schedule${index}.`;
  }

  get validRequestTypes() {
    if (this.hasElectricVehicles) {
      return this.evRequestTypes;
    }
    return this.nonEvRequestTypes;
  }

  get hasElectricVehicles(): boolean {
    return this.electricVehicles.length > 0;
  }

  addSchedule() {
    this.schedules.push(new Schedule({enabled: true}));
    this.form.markAsDirty();
  }

  onScheduleRemove(index: number) {
    console.log('Remove ', index);
    this.schedules.splice(index, 1);
    this.form.markAsDirty();
  }

  buildForm(schedules: Schedule[], formHandler: FormHandler): FormGroup {
    const form = new FormGroup({});
    return form;
  }

  updateForm(form: FormGroup, schedules: Schedule[], formHandler: FormHandler) {
  }

  submitForm() {
    if (this.scheduleComps) {
      this.schedules = [];
      this.scheduleComps.forEach(scheduleComponent => {
        const schedule = scheduleComponent.updateModelFromForm();
        if (schedule) {
          this.schedules.push(schedule);
        }
      });
    }
    this.scheduleService.setSchedules(this.applianceId, this.schedules).subscribe();
    this.form.markAsPristine();
  }
}
