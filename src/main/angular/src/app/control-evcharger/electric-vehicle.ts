import {SocScript} from './soc-script';

export class ElectricVehicle {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.control.ev.ElectricVehicle';
  }
  '@class' = ElectricVehicle.TYPE;

  id: string;
  name: string;
  batteryCapacity: number;
  phases: number;
  maxChargePower: number;
  defaultSocManual: number;
  defaultSocSchedule: number;
  defaultSocOptionalEnergy: number;
  socScript: SocScript;

  public constructor(init?: Partial<ElectricVehicle>) {
    Object.assign(this, init);
  }
}
