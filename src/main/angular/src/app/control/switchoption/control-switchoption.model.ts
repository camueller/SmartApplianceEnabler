import {FormControl} from '@angular/forms';

export interface ControlSwitchoptionModel {
  powerThreshold: FormControl<number>;
  switchOnDetectionDuration: FormControl<number>;
  switchOffDetectionDuration: FormControl<number>;
}
