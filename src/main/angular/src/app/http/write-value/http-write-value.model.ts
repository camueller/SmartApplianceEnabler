import {FormControl} from '@angular/forms';

export interface HttpWriteValueModel {
  name: FormControl<string>;
  value: FormControl<string>;
  factorToValue: FormControl<number>;
  method: FormControl<string>;
}
