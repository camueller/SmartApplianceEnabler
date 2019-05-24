import {EvModbusControl} from '../control-evcharger-modbus/ev-modbus-control';
import {ElectricVehicle} from './electric-vehicle';
import {EvHttpControl} from '../control-evcharger-http/ev-http-control';

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
