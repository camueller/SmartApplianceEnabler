import {FormControl} from '@angular/forms';

export interface ControlStartingcurrentModel {
  powerThreshold: FormControl<number>;
  startingCurrentDetectionDuration: FormControl<number>;
  finishedCurrentDetectionDuration: FormControl<number>;
  minRunningTime: FormControl<number>;
}
