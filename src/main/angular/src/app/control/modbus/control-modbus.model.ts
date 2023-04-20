import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ModbusWriteModel} from '../../modbus/write/modbus-write.model';

export interface ControlModbusModel {
  idref: FormControl<string>;
  slaveAddress: FormControl<string>;
  modbusWrites: FormArray<FormGroup<ModbusWriteModel>>;
}
