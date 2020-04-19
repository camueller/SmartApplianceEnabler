import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export const washingMachine = new Appliance({
  vendor: 'Bosch',
  name: 'SuperWash',
  type: 'WashingMachine',
  serial: '1122334455',
  maxPowerConsumption: 2500,
  interruptionsAllowed: true,
  maxOnTime: 1800,
});
