import {MeterReportingSwitchDefaults} from './meterreporting/meter-reporting-switch-defaults';
import {StartingCurrentSwitchDefaults} from './startingcurrent/starting-current-switch-defaults';
import {EvChargerDefaults} from './evcharger/ev-charger-defaults';
import {ModbusWriteDefaults} from '../modbus/write/modbus-write-defaults';

export class ControlDefaults {
  meterReportingSwitchDefaults: MeterReportingSwitchDefaults;
  startingCurrentSwitchDefaults: StartingCurrentSwitchDefaults;
  evChargerDefaults: EvChargerDefaults;
  modbusWriteDefaults: ModbusWriteDefaults;

  public constructor(init?: Partial<ControlDefaults>) {
    Object.assign(this, init);
  }
}
