import {MqttElectricityMeter} from '../../../../../main/angular/src/app/meter/mqtt/mqtt-electricity-meter';

export const mqttMeter = new MqttElectricityMeter({
    topic: 'tele/tasmota/SENSOR',
    name: 'Energy',
    contentProtocol: 'JSON',
    path: '$.ENERGY.Total',
    timePath: '$.Time'
});
