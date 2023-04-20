import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ModbusWriteValueModel} from '../write-value/modbus-write-value.model';

export interface ModbusWriteModel {
  address: FormControl<string>;
  type: FormControl<string>;
  valueType: FormControl<string>;
  factorToValue: FormControl<number>;
  modbusWriteValues: FormArray<FormGroup<ModbusWriteValueModel>>;
}
