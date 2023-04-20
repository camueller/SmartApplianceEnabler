import {FormControl} from '@angular/forms';

export interface ScheduleModel {
  enabled: FormControl<boolean>;
  timeframeType: FormControl<string>;
  requestType: FormControl<string>;
}
