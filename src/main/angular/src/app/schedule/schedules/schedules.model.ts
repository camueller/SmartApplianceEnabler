import {FormArray, FormGroup} from '@angular/forms';
import {ScheduleModel} from '../schedule.model';

export interface SchedulesModel {
  schedules: FormArray<FormGroup<ScheduleModel>>;
}
