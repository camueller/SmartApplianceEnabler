import {FormControl} from '@angular/forms';

export interface ModbusReadValueModel {
  name: FormControl<string>;
  extractionRegex: FormControl<string>;

}
