import {ElectricVehicle} from '../../../../../../main/angular/src/app/control/evcharger/electric-vehicle/electric-vehicle';

export const nissan_leaf: ElectricVehicle = {
  id: 1,
  name: 'Nissan Leaf',
  batteryCapacity: 40000,
  phases: 1,
  maxChargePower: 7300,
  chargeLoss: 8,
  defaultSocManual: 60,
  defaultSocOptionalEnergy: 90,
  socScript: {'@class': '', script: '/my/script', extractionRegex: '.*(\d+).*'},
} as ElectricVehicle;
