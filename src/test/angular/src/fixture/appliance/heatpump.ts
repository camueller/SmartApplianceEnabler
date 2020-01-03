import {Appliance} from '../../../../../main/angular/src/app/appliance/appliance';

export const heatPump = new Appliance({
    vendor: 'Viessmann',
    name: 'Vitocal 300',
    type: 'HeatPump',
    serial: '0123456789',
    maxPowerConsumption: 4000,
    interruptionsAllowed: true,
    minOnTime: 1800,
    maxOnTime: 7200,
    minOffTime: 600,
    maxOffTime: 900,
});
