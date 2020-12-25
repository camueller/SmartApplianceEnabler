import {Settings} from '../../../../../main/angular/src/app/settings/settings';

export const settings = new Settings({
  modbusSettings: [
    {
      modbusTcpId: `id-${process.env.CI ? (Math.floor(Math.random() * 99999) + 1).toString() : 'modbus'}`,
      modbusTcpHost: 'localhost',
      modbusTcpPort: 502
    }
  ],
  notificationCommand: '/opt/sae/notifyWithTelegram.sh',
});
