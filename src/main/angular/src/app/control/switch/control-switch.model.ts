import {FormControl} from '@angular/forms';

export interface ControlSwitchModel {
  gpio: FormControl<number>;
  reverseStates: FormControl<boolean>;
}
