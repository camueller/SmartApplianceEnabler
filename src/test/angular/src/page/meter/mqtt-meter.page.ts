import {MeterPage} from './meter.page';
import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {MqttElectricityMeter} from '../../../../../main/angular/src/app/meter/mqtt/mqtt-electricity-meter';
import {ModbusReadPage} from '../modbus/modbus-read.page';
import {
    assertInput,
    assertSelectOption,
    inputText,
    selectOption,
    selectorInputByFormControlName,
    selectorSelectByFormControlName,
    selectorSelectedByFormControlName
} from '../../shared/form';
import {settings} from '../../fixture/settings/settings';
import {ModbusMeterPage} from './modbus-meter.page';

export class MqttMeterPage extends MeterPage {

    private static selectorPrefix = 'app-meter-mqtt';
    private static i18nPrefix = 'MeterMqttComponent.';

    public static async setMqttElectricityMeter(t: TestController, mqttElectricityMeter: MqttElectricityMeter) {
        await MqttMeterPage.setType(t, MqttElectricityMeter.TYPE);

        await MqttMeterPage.setTopic(t, mqttElectricityMeter.topic);
        await MqttMeterPage.setName(t, mqttElectricityMeter.name);
        await MqttMeterPage.setContentProtocol(t, mqttElectricityMeter.contentProtocol);
        await MqttMeterPage.setPath(t, mqttElectricityMeter.path);
        await MqttMeterPage.setTimePath(t, mqttElectricityMeter.timePath);
    }
    public static async assertMqttElectricityMeter(t: TestController, mqttElectricityMeter: MqttElectricityMeter) {
        await ModbusMeterPage.assertType(t, MqttElectricityMeter.TYPE);

        await MqttMeterPage.assertTopic(t, mqttElectricityMeter.topic);
        await MqttMeterPage.assertName(t, mqttElectricityMeter.name);
        await MqttMeterPage.assertContentProtocol(t, mqttElectricityMeter.contentProtocol);
        await MqttMeterPage.assertPath(t, mqttElectricityMeter.path);
        await MqttMeterPage.assertTimePath(t, mqttElectricityMeter.timePath);
    }

    public static async setTopic(t: TestController, topic: string) {
        await inputText(t, selectorInputByFormControlName('topic'), topic);
    }
    public static async assertTopic(t: TestController, topic: string) {
        await assertInput(t, selectorInputByFormControlName('topic'), topic);
    }

    public static async setName(t: TestController, name: string) {
        await selectOption(t, selectorSelectByFormControlName('name', MqttMeterPage.selectorPrefix), name);
    }
    public static async assertName(t: TestController, name: string) {
        await assertSelectOption(t, selectorSelectedByFormControlName('name', MqttMeterPage.selectorPrefix),
            name, MqttMeterPage.i18nPrefix);
    }

    public static async setContentProtocol(t: TestController, contentProtocol: string) {
        await selectOption(t, selectorSelectByFormControlName('contentProtocol', MqttMeterPage.selectorPrefix), contentProtocol);
    }
    public static async assertContentProtocol(t: TestController, contentProtocol: string) {
        await assertSelectOption(t, selectorSelectedByFormControlName('contentProtocol', MqttMeterPage.selectorPrefix),
            contentProtocol, MqttMeterPage.i18nPrefix);
    }

    public static async setPath(t: TestController, path: string) {
        await inputText(t, selectorInputByFormControlName('path'), path);
    }
    public static async assertPath(t: TestController, path: string) {
        await assertInput(t, selectorInputByFormControlName('path'), path);
    }

    public static async setTimePath(t: TestController, timePath: string) {
        await inputText(t, selectorInputByFormControlName('timePath'), timePath);
    }
    public static async assertTimePath(t: TestController, timePath: string) {
        await assertInput(t, selectorInputByFormControlName('timePath'), timePath);
    }
}
