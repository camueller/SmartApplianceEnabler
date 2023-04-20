import {FormControl} from '@angular/forms';

export interface ControlPwmModel {
  gpio: FormControl<number>;
  pwmFrequency: FormControl<number>;
  minDutyCycle: FormControl<number>;
  maxDutyCycle: FormControl<number>;
}
