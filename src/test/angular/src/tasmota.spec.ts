import {
    configurationKey,
    createAndAssertAppliance,
    createAndAssertControl,
    createAndAssertMeter,
    fixtureName,
    testSpeed
} from './shared/helper';
import {baseUrl} from './page/page';
import {ApplianceConfiguration} from './shared/appliance-configuration';
import {generateApplianceId} from './shared/appliance-id-generator';
import {httpSwitch_2httpWrite_httpRead_complete} from './fixture/control/http-control';
import {tasmota} from './fixture/appliance/tasmota';
import {MqttElectricityMeter} from '../../../main/angular/src/app/meter/mqtt/mqtt-electricity-meter';
import {MqttSwitch} from '../../../main/angular/src/app/control/mqtt/mqtt-switch';
import {mqttMeter} from './fixture/meter/mqtt-meter';
import {mqttSwitch} from './fixture/control/mqtt-switch';


fixture('Tasmota')
    .beforeEach(async t => {
        await t.maximizeWindow();
        await t.setTestSpeed(testSpeed);
    })
    .page(baseUrl());

function createTasmota(): ApplianceConfiguration {
    return new ApplianceConfiguration({
        appliance: {...tasmota, id: generateApplianceId()},
        meter: {type: MqttElectricityMeter.TYPE, mqttElectricityMeter: mqttMeter},
        control: {type: MqttSwitch.TYPE, mqttSwitch, startingCurrentSwitchUsed: false}
    });
}

test('Create appliance', async t => {
    await createAndAssertAppliance(t, createTasmota());
});

test('Create MQTT meter', async t => {
    await createAndAssertMeter(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});

test('Create MQTT switch', async t => {
    await createAndAssertControl(t, t.fixtureCtx[configurationKey(t, fixtureName(t))]);
});
