import {FormControl} from '@angular/forms';

export interface ModbusWriteValueModel {
  name: FormControl<string>;
  value: FormControl<string>;
}
