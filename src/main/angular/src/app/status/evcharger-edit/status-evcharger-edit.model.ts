import {FormControl} from '@angular/forms';
import {ChargeMode} from './charge-mode';

export interface StatusEvchargerEditModel {
  chargeMode: FormControl<ChargeMode>;
  electricVehicle: FormControl<number>;
  socCurrent: FormControl<string>;
  socTarget: FormControl<number>;
  chargeEndDow?: FormControl<number>;
}
