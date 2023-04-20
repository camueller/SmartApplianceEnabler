import {FormControl} from '@angular/forms';

export interface ScheduleRequestRuntimeModel {
  minRuntime: FormControl<string>;
  maxRuntime: FormControl<string>;
  enabledExternally: FormControl<boolean>;
}
