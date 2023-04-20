import {FormControl} from '@angular/forms';

export interface ApplianceModel {
  id: FormControl<string>;
  vendor: FormControl<string>;
  name: FormControl<string>;
  type: FormControl<string>;
  serial: FormControl<string>;
  minPowerConsumption: FormControl<number>;
  maxPowerConsumption: FormControl<number>;
  interruptionsAllowed: FormControl<boolean>;
  minOnTime: FormControl<number>;
  minOffTime: FormControl<number>;
  maxOnTime: FormControl<number>;
  maxOffTime: FormControl<number>;
  notificationSenderId: FormControl<string>;
}
