import {FormControl} from '@angular/forms';

export interface MeterModel {
  meterType: FormControl<string>;
  isMasterMeter: FormControl<boolean>;
}
