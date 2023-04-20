import {FormControl} from '@angular/forms';

export interface ScheduleRequestEnergyModel {
  minEnergy: FormControl<number>;
  maxEnergy: FormControl<number>;
  enabledExternally: FormControl<boolean>;
}
