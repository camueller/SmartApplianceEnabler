import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ModbusReadModel} from '../../modbus/read/modbus-read.model';

export interface MeterModbusModel {
  idref: FormControl<string>;
  slaveAddress: FormControl<string>;
  pollInterval: FormControl<number>;
  modbusReads: FormArray<FormGroup<ModbusReadModel>>
}
