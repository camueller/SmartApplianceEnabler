import {FormControl} from '@angular/forms';

export interface MeterS0Model {
  gpio: FormControl<number>;
  pinPullResistance: FormControl<string>;
  impulsesPerKwh: FormControl<number>;
  minPulseDuration: FormControl<number>;
}
