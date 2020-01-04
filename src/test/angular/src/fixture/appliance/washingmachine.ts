import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export const washingMachine = new Appliance({
  vendor: 'Bosch',
  name: 'SuperWash',
  type: 'WashingMachine',
  serial: '1122334455',
  maxPowerConsumption: 1800,
  interruptionsAllowed: false,
  maxOnTime: 1800,
});
