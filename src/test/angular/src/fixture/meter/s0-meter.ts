import {S0ElectricityMeter} from '../../../../../main/angular/src/app/meter/s0/s0-electricity-meter';

export const s0Meter = new S0ElectricityMeter({
  gpio: 1,
  pinPullResistance: 'PULL_DOWN',
  impulsesPerKwh: 1000,
  measurementInterval: 60
});
