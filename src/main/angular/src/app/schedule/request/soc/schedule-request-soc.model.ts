import {FormControl} from '@angular/forms';

export interface ScheduleRequestSocModel {
  evId: FormControl<number>;
  soc: FormControl<number>;
  enabledExternally: FormControl<boolean>;
}
