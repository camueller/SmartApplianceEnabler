import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ModbusReadModel} from '../../../modbus/read/modbus-read.model';
import {ModbusWriteModel} from '../../../modbus/write/modbus-write.model';

export interface ControlEvchargerModbusModel {
  idref: FormControl<string>;
  slaveAddress: FormControl<string>;
  modbusReads: FormArray<FormGroup<ModbusReadModel>>;
  modbusWrites: FormArray<FormGroup<ModbusWriteModel>>;
}
