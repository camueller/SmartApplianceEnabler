import {FormControl} from '@angular/forms';

export interface HttpReadValueModel {
  name: FormControl<string>;
  method: FormControl<string>;
  data: FormControl<string>;
  path: FormControl<string>;
  extractionRegex: FormControl<string>;
  factorToValue: FormControl<number>;
}
