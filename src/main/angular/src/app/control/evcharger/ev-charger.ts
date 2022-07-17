import {ElectricVehicle} from './electric-vehicle/electric-vehicle';
import {EvModbusControl} from './modbus/ev-modbus-control';
import {EvHttpControl} from './http/ev-http-control';
import {EvChargerProtocol} from './ev-charger-protocol';

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
  chargePowerRepetition: number;
  vehicles: ElectricVehicle[];
  latitude: number;
  longitude: number;

  public constructor(init?: Partial<EvCharger>) {
    Object.assign(this, init);
  }

  get protocol(): string {
    if (this.modbusControl && this.modbusControl['@class'] === EvModbusControl.TYPE) {
      return EvChargerProtocol.MODBUS;
    } else if (this.httpControl && this.httpControl['@class'] === EvHttpControl.TYPE) {
      return EvChargerProtocol.HTTP;
    }
    return undefined;
  }
}
