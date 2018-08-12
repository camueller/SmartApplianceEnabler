import {EvModbusControl} from './ev-modbus-control';

export class EvCharger {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger';
  }

  '@class' = EvCharger.TYPE;
  type: string;
  control: EvModbusControl;
  voltage: number;
  phases: number;
  pollInterval: number;

  public constructor(init?: Partial<EvCharger>) {
    Object.assign(this, init);
  }
}
