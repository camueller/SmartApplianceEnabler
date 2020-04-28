import {HttpElectricityMeter} from '../../../../../main/angular/src/app/meter/http/http-electricity-meter';
import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {HttpReadValue} from '../../../../../main/angular/src/app/http/read-value/http-read-value';
import {HttpRead} from '../../../../../main/angular/src/app/http/read/http-read';

export const httpMeter_goeCharger = new HttpElectricityMeter({
  contentProtocol: 'JSON',
  httpReads: [
    new HttpRead({
      url: 'http://go-echarger/status',
      readValues: [
        new HttpReadValue({
          name: MeterValueName.Energy,
          path: '$.dws',
          factorToValue: 0.0000027778
        })
      ]
    })
  ]
});
