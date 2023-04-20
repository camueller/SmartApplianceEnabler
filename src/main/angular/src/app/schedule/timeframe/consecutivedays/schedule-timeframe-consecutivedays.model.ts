import {FormControl} from '@angular/forms';

export interface ScheduleTimeframeConsecutivedaysModel {
  startDayOfWeek: FormControl<number>;
  endDayOfWeek: FormControl<number>;
}
