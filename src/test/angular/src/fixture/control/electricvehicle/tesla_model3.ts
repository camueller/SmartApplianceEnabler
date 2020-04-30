import {ElectricVehicle} from '../../../../../../main/angular/src/app/control/evcharger/electric-vehicle/electric-vehicle';
import {SocScript} from '../../../../../../main/angular/src/app/control/evcharger/electric-vehicle/soc-script';

export const tesla_model3: ElectricVehicle = {
  name: 'Tesla Model 3',
  batteryCapacity: 75000,
  phases: 3,
  maxChargePower: 11000,
  chargeLoss: 7,
  defaultSocManual: 75,
  defaultSocOptionalEnergy: 90,
  socScript: {script: '/tesla/model3', extractionRegex: '.*(\d*).*'} as SocScript,
} as ElectricVehicle;
