import {ElectricVehicle} from '../../../../../../main/angular/src/app/control/evcharger/electric-vehicle/electric-vehicle';
import {SocScript} from '../../../../../../main/angular/src/app/control/evcharger/electric-vehicle/soc-script';

export const nissan_leaf: ElectricVehicle = {
  name: 'Nissan Leaf',
  batteryCapacity: 40000,
  phases: 1,
  maxChargePower: 7300,
  chargeLoss: 8,
  defaultSocManual: 60,
  defaultSocOptionalEnergy: 90,
  socScript: {script: '/my/script', extractionRegex: '.*(\d+).*'} as SocScript,
} as ElectricVehicle;
