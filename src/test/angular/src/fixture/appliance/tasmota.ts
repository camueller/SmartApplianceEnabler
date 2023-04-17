import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export const tasmota = new Appliance({
    vendor: 'Tasmota',
    name: 'MQTT controlled',
    type: 'Other',
    serial: '2233445566',
    maxPowerConsumption: 500,
    interruptionsAllowed: true,
});
