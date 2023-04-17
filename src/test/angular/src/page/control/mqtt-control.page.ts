import {ControlPage} from './control.page';
import {
    assertCheckbox,
    assertInput,
    inputText,
    selectorCheckboxByFormControlName,
    selectorCheckboxCheckedByFormControlName,
    selectorInputByFormControlName,
    setCheckboxEnabled
} from '../../shared/form';
import {MqttSwitch} from '../../../../../main/angular/src/app/control/mqtt/mqtt-switch';

export class MqttControlPage extends ControlPage {

    public static async setMqttSwitch(t: TestController, mqttSwitch: MqttSwitch, selectorPrefix = 'app-control-mqtt') {
        await MqttControlPage.setType(t, MqttSwitch.TYPE);

        await MqttControlPage.setTopic(t, mqttSwitch.topic, selectorPrefix);
        await MqttControlPage.setOnPayload(t, mqttSwitch.onPayload, selectorPrefix);
        await MqttControlPage.setOffPayload(t, mqttSwitch.offPayload, selectorPrefix);
        await MqttControlPage.setStatusTopic(t, mqttSwitch.statusTopic, selectorPrefix);
        await MqttControlPage.setStatusExtractionRegex(t, mqttSwitch.statusExtractionRegex, selectorPrefix);
    }
    public static async assertMqttSwitch(t: TestController, mqttSwitch: MqttSwitch, selectorPrefix = 'app-control-mqtt') {
        await MqttControlPage.assertType(t, MqttSwitch.TYPE);

        await MqttControlPage.assertTopic(t, mqttSwitch.topic, selectorPrefix);
        await MqttControlPage.assertOnPayload(t, mqttSwitch.onPayload, selectorPrefix);
        await MqttControlPage.assertOffPayload(t, mqttSwitch.offPayload, selectorPrefix);
        await MqttControlPage.assertStatusTopic(t, mqttSwitch.statusTopic, selectorPrefix);
        await MqttControlPage.assertStatusExtractionRegex(t, mqttSwitch.statusExtractionRegex, selectorPrefix);
    }

    public static async setTopic(t: TestController, topic: string, selectorPrefix: string) {
        await inputText(t, selectorInputByFormControlName('topic', selectorPrefix), topic);
    }
    public static async assertTopic(t: TestController, topic: string, selectorPrefix: string) {
        await assertInput(t, selectorInputByFormControlName('topic', selectorPrefix), topic);
    }

    public static async setOnPayload(t: TestController, onPayload: string, selectorPrefix: string) {
        await inputText(t, selectorInputByFormControlName('onPayload', selectorPrefix), onPayload);
    }
    public static async assertOnPayload(t: TestController, onPayload: string, selectorPrefix: string) {
        await assertInput(t, selectorInputByFormControlName('onPayload', selectorPrefix), onPayload);
    }

    public static async setOffPayload(t: TestController, offPayload: string, selectorPrefix: string) {
        await inputText(t, selectorInputByFormControlName('offPayload', selectorPrefix), offPayload);
    }
    public static async assertOffPayload(t: TestController, offPayload: string, selectorPrefix: string) {
        await assertInput(t, selectorInputByFormControlName('offPayload', selectorPrefix), offPayload);
    }

    public static async setStatusTopic(t: TestController, statusTopic: string, selectorPrefix: string) {
        await inputText(t, selectorInputByFormControlName('statusTopic', selectorPrefix), statusTopic);
    }
    public static async assertStatusTopic(t: TestController, statusTopic: string, selectorPrefix: string) {
        await assertInput(t, selectorInputByFormControlName('statusTopic', selectorPrefix), statusTopic);
    }

    public static async setStatusExtractionRegex(t: TestController, statusExtractionRegex: string, selectorPrefix: string) {
        await inputText(t, selectorInputByFormControlName('statusExtractionRegex', selectorPrefix), statusExtractionRegex);
    }
    public static async assertStatusExtractionRegex(t: TestController, statusExtractionRegex: string, selectorPrefix: string) {
        await assertInput(t, selectorInputByFormControlName('statusExtractionRegex', selectorPrefix), statusExtractionRegex);
    }
}
