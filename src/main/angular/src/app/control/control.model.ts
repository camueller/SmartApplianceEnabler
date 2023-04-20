import {FormControl} from '@angular/forms';

export interface ControlModel {
  controlType: FormControl<string>;
  startingCurrentSwitchUsed: FormControl<boolean>;
  switchOptionUsed: FormControl<boolean>;
}
