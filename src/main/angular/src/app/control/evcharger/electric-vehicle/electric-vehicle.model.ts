import {SocScript} from './soc-script';
import {FormControl} from '@angular/forms';

export interface ElectricVehicleModel {
  id: FormControl<number>;
  name: FormControl<string>;
  batteryCapacity: FormControl<number>;
  phases: FormControl<number>;
  maxChargePower: FormControl<number>;
  chargeLoss: FormControl<number>;
  defaultSocManual: FormControl<number>;
  defaultSocOptionalEnergy: FormControl<number>;

  scriptExtractionRegex: FormControl<string>;
  pluginStatusExtractionRegex: FormControl<string>;
  pluginTimeExtractionRegex: FormControl<string>;
  latitudeExtractionRegex: FormControl<string>;
  longitudeExtractionRegex: FormControl<string>;
  scriptUpdateSocAfterIncrease: FormControl<number>;
  scriptUpdateSocAfterSeconds: FormControl<number>;
  scriptTimeoutSeconds: FormControl<number>;
}
