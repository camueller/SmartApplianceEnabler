import {FormControl} from '@angular/forms';

export interface MeterMasterModel {
  masterSwitchOn: FormControl<string>;
  slaveSwitchOn: FormControl<string>;
}
