import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export const evchargerGoe = new Appliance({
  vendor: 'go-e',
  name: 'eCharger',
  type: 'EVCharger',
  serial: 'WB-98765',
  maxPowerConsumption: 22000,
  minPowerConsumption: 1380,
  interruptionsAllowed: true,
});
