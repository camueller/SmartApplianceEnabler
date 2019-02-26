import {EvModbusControl} from './ev-modbus-control';
import {ElectricVehicle} from './electric-vehicle';

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
  startChargingStateDetectionDelay: number;
  forceInitialCharging: boolean;
  vehicles: ElectricVehicle[];

  public constructor(init?: Partial<EvCharger>) {
    Object.assign(this, init);
  }
}
