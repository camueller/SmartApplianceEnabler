import {ModbusSetting} from '../../../../../main/angular/src/app/settings/modbus/modbus-setting';
import {ModbusPage} from './modbus.page';
import {SideMenu} from '../side.menu.page';
import {assertInput, clickButton, inputText, selectorInputByFormControlName} from '../../shared/form';
import {Settings} from '../../../../../main/angular/src/app/settings/settings';
import {MqttSettings} from '../../../../../main/angular/src/app/settings/mqtt-settings';
import {Selector} from 'testcafe';
import {saeRestartTimeout} from '../../shared/timeout';

export class SettingsPage {
  private static selectorBase = 'app-settings';
  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';
  private static ADD_MODBUS_BUTTON_SELECTOR = `${SettingsPage.selectorBase} button.SettingsComponent__addModbusSetting`;

  private static async waitForPage(t: TestController) {
    await t.expect(Selector('form.SettingsComponent').exists).ok({timeout: saeRestartTimeout});
  }

  public static async createAndAssertSettings(t: TestController, settings: Settings) {
    await SettingsPage.createSettings(t, settings);
    await SideMenu.clickStatus(t);
    await SettingsPage.assertSettings(t, settings);
  }

  public static async createSettings(t: TestController, settings: Settings) {
    await SideMenu.clickSettings(t);
    await this.waitForPage(t);
    await SettingsPage.setMqttBroker(t, settings.mqttSettings);
    await SettingsPage.addModbus(t, settings.modbusSettings[0]);
    await SettingsPage.setNotificationCommand(t, settings.notificationCommand);
    await clickButton(t, SettingsPage.SAVE_BUTTON_SELECTOR);
  }

  public static async assertSettings(t: TestController, settings: Settings) {
    await SideMenu.clickSettings(t);
    await this.waitForPage(t);
    const modbusSettingsIndex = await ModbusPage.getModbusSettingsCount() - 1;
    await ModbusPage.assertModbus(t, settings.modbusSettings[0], modbusSettingsIndex);
    await SettingsPage.assertNotificationCommand(t, settings.notificationCommand);
  }

  private static async setMqttBroker(t: TestController, mqttSettings: MqttSettings) {
    await inputText(t, selectorInputByFormControlName('mqttBrokerHost'), undefined);
    if (mqttSettings.mqttBrokerHost) {
      await inputText(t, selectorInputByFormControlName('mqttBrokerHost'), mqttSettings.mqttBrokerHost);
    }
    await inputText(t, selectorInputByFormControlName('mqttBrokerPort'), undefined);
    if (mqttSettings.mqttBrokerPort) {
      await inputText(t, selectorInputByFormControlName('mqttBrokerPort'), mqttSettings.mqttBrokerPort.toString());
    }
  }
  private static async assertMqttBroker(t: TestController, mqttSettings: MqttSettings) {
    if (mqttSettings.mqttBrokerHost) {
      await assertInput(t, selectorInputByFormControlName('mqttBrokerHost'), mqttSettings.mqttBrokerHost);
    }
    if (mqttSettings.mqttBrokerPort) {
      await assertInput(t, selectorInputByFormControlName('mqttBrokerPort'), mqttSettings.mqttBrokerPort.toString());
    }
  }

  private static async addModbus(t: TestController, modbusSettings: ModbusSetting) {
    const modbusSettingsIndex = await ModbusPage.getModbusSettingsCount();
    await clickButton(t, SettingsPage.ADD_MODBUS_BUTTON_SELECTOR);
    await ModbusPage.addModbus(t, modbusSettings, modbusSettingsIndex);
  }

  private static async setNotificationCommand(t: TestController, notificationCommand: string) {
    await inputText(t, selectorInputByFormControlName('notificationCommand'), notificationCommand);
  }
  private static async assertNotificationCommand(t: TestController, notificationCommand: string) {
    await assertInput(t, selectorInputByFormControlName('notificationCommand'), notificationCommand);
  }
}
