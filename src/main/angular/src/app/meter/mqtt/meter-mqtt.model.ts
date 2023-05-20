import {Form, FormControl} from '@angular/forms';

export interface MeterMqttModel {
  topic: FormControl<string>;
  name: FormControl<string>;
  contentProtocol: FormControl<string>;
  path: FormControl<string>;
  timePath: FormControl<string>;
  extractionRegex: FormControl<string>;
  factorToValue: FormControl<number>;
}
