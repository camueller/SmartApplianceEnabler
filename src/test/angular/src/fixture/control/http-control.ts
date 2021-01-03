import {HttpReadValue} from '../../../../../main/angular/src/app/http/read-value/http-read-value';
import {HttpRead} from '../../../../../main/angular/src/app/http/read/http-read';
import {HttpConfiguration} from '../../../../../main/angular/src/app/http/configuration/http-configuration';
import {HttpWrite} from '../../../../../main/angular/src/app/http/write/http-write';
import {HttpWriteValue} from '../../../../../main/angular/src/app/http/write-value/http-write-value';
import {ControlValueName} from '../../../../../main/angular/src/app/control/control-value-name';
import {HttpSwitch} from '../../../../../main/angular/src/app/control/http/http-switch';

export const httpSwitch_2httpWrite_httpRead_complete = new HttpSwitch({
  httpConfiguration: new HttpConfiguration({
    contentType: 'application/json',
    username: 'myUser',
    password: 'mySecret'
  }),
  httpWrites: [
    new HttpWrite({
      url: 'http://fritz.box/setOn',
      writeValues: [
        new HttpWriteValue({
          name: ControlValueName.On,
          method: 'GET',
          value: '1',
        })
      ]
    }),
    new HttpWrite({
      url: 'http://fritz.box/setOff',
      writeValues: [
        new HttpWriteValue({
          name: ControlValueName.Off,
          method: 'POST',
          value: '0',
        })
      ]
    }),
  ],
  httpRead: new HttpRead({
    url: 'http://fritz.box/status',
    readValues: [
      new HttpReadValue({
        name: ControlValueName.On,
        data: 'POWER',
        path: '$.power',
        extractionRegex: '.*"Power":(\\d+).*',
      })
    ]
  })
});
