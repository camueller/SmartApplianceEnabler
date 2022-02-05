import {ModbusSetting} from '../../../../../main/angular/src/app/settings/modbus/modbus-setting';
import {ModbusPage} from './modbus.page';
import {SideMenu} from '../side.menu.page';
import {assertInput, clickButton, inputText, selectorInputByFormControlName} from '../../shared/form';
import {Settings} from '../../../../../main/angular/src/app/settings/settings';
import {Selector} from 'testcafe';

export class SettingsPage {
  private static selectorBase = 'app-settings';
  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';
  private static ADD_MODBUS_BUTTON_SELECTOR = `${SettingsPage.selectorBase} button.SettingsComponent__addModbusSetting`;

  private static async waitForPage(t: TestController) {
    await t.expect(Selector('form.SettingsComponent').exists).ok();
  }

  public static async createAndAssertSettings(t: TestController, settings: Settings) {
    await SettingsPage.createSettings(t, settings);
    await SideMenu.clickStatus(t);
    await SettingsPage.assertSettings(t, settings);
  }

  public static async createSettings(t: TestController, settings: Settings) {
    await SideMenu.clickSettings(t);
    await this.waitForPage(t);
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
