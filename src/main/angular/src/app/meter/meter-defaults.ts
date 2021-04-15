import {S0ElectricityMeterDefaults} from './s0/s0-electricity-meter-defaults';
import {HttpElectricityMeterDefaults} from './http/http-electricity-meter-defaults';
import {ModbusElectricityMeterDefaults} from './modbus/modbus-electricity-meter-defaults';
import {ModbusReadDefaults} from '../modbus/read/modbus-read-defaults';

export class MeterDefaults {
  s0ElectricityMeterDefaults: S0ElectricityMeterDefaults;
  httpElectricityMeterDefaults: HttpElectricityMeterDefaults;
  modbusElectricityMeterDefaults: ModbusElectricityMeterDefaults;
  modbusReadDefaults: ModbusReadDefaults;

  public constructor(init?: Partial<MeterDefaults>) {
    Object.assign(this, init);
  }
}
