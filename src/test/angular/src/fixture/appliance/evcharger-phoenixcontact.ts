import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export const evchargerPhoenixContact = new Appliance({
  vendor: 'ESL',
  name: 'Wallbox',
  type: 'EVCharger',
  serial: 'WB-12345',
  maxPowerConsumption: 22000,
  minPowerConsumption: 1380,
  interruptionsAllowed: true,
});
