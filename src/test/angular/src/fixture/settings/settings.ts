import {Settings} from '../../../../../main/angular/src/app/settings/settings';

export const settings = new Settings({
  modbusSettings: [
    {
      modbusTcpId: `id-${(Math.floor(Math.random() * 99999) + 1).toString()}`,
      modbusTcpHost: 'localhost',
      modbusTcpPort: 502
    }
  ]
});
