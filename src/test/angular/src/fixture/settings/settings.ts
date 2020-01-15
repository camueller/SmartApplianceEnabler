import {Settings} from '../../../../../main/angular/src/app/settings/settings';

export const settings = new Settings({
  modbusSettings: [
    {
      modbusTcpId: 'modbus',
      modbusTcpHost: 'localhost',
      modbusTcpPort: 502
    }
  ]
});
