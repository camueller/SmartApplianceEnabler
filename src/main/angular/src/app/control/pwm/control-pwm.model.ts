import {FormControl} from '@angular/forms';

export interface ControlPwmModel {
  gpio: FormControl<number>;
  pwmChip: FormControl<number>;
  pwmChannel: FormControl<number>;
  pwmFrequency: FormControl<number>;
  minDutyCycle: FormControl<number>;
  maxDutyCycle: FormControl<number>;
}
