import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ModbusReadValueModel} from '../read-value/modbus-read-value.model';

export interface ModbusReadModel {
  address: FormControl<string>;
  type: FormControl<string>;
  valueType: FormControl<string>;
  words: FormControl<number>;
  byteOrder: FormControl<string>;
  factorToValue: FormControl<number>;
  modbusReadValues: FormArray<FormGroup<ModbusReadValueModel>>;
}
