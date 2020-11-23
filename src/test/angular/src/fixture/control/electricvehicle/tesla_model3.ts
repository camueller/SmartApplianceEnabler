import {ElectricVehicle} from '../../../../../../main/angular/src/app/control/evcharger/electric-vehicle/electric-vehicle';

export const tesla_model3: ElectricVehicle = {
  name: 'Tesla Model 3',
  batteryCapacity: 75000,
  phases: 3,
  maxChargePower: 11000,
  chargeLoss: 7,
  defaultSocManual: 75,
  defaultSocOptionalEnergy: 90,
} as ElectricVehicle;
