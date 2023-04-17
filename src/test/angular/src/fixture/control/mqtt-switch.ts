import {MqttSwitch} from '../../../../../main/angular/src/app/control/mqtt/mqtt-switch';

export const mqttSwitch = new MqttSwitch({
    topic: 'cmnd/tasmota/Power',
    onPayload: 'ON',
    offPayload: 'OFF',
    statusTopic: 'stat/tasmota/POWER',
    statusExtractionRegex: 'ON'
});
