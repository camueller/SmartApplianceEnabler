import {ElectricVehicle} from './electric-vehicle/electric-vehicle';
import {EvModbusControl} from './modbus/ev-modbus-control';
import {EvHttpControl} from './http/ev-http-control';

export class EvCharger {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger';
  }

  '@class' = EvCharger.TYPE;
  type: string;
  modbusControl: EvModbusControl;
  httpControl: EvHttpControl;
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
