import {HttpElectricityMeter} from '../../../../../main/angular/src/app/meter/http/http-electricity-meter';
import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {HttpReadValue} from '../../../../../main/angular/src/app/http/read-value/http-read-value';
import {HttpRead} from '../../../../../main/angular/src/app/http/read/http-read';

export const httpMeter_complete = new HttpElectricityMeter({
  httpReads: [
    new HttpRead({
      url: 'http://fritz.box/energy',
      readValues: [
        new HttpReadValue({
          name: MeterValueName.Energy,
          data: 'SOME_DATA',
          extractionRegex: '.*"Power":(\\d+).*',
          factorToValue: 10
        })
      ]
    })
  ]
});

export const httpMeter_pollInterval = new HttpElectricityMeter({
  pollInterval: 30,
  httpReads: [
    new HttpRead({
      url: 'http://fritz.box/power',
      readValues: [
        new HttpReadValue({
          name: MeterValueName.Power,
        })
      ]
    })
  ]
});
