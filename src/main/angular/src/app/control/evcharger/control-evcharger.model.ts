import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ElectricVehicleModel} from './electric-vehicle/electric-vehicle.model';

export interface ControlEvchargerModel {
  template: FormControl<string>;
  protocol: FormControl<string>;
  voltage: FormControl<number>;
  phases: FormControl<number>;
  pollInterval: FormControl<number>;
  startChargingStateDetectionDelay: FormControl<number>;
  forceInitialCharging: FormControl<boolean>;
  chargePowerRepetition: FormControl<number>;
  electricVehicles: FormArray<FormGroup<ElectricVehicleModel>>;
  latitude: FormControl<number>;
  longitude: FormControl<number>;

}
