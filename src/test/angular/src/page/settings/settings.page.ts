import {Selector} from 'testcafe';
import {ModbusSettings} from '../../../../../main/angular/src/app/settings/modbus-settings';
import {TopMenu} from '../top-menu.page';
import {ModbusPage} from './modbus.page';

export class SettingsPage {
  private static saveButton = Selector('button[type="submit"]');

  public static async addModbus(t: TestController, modbusSettings: ModbusSettings) {
    await TopMenu.clickSettings(t);
    await ModbusPage.addModbus(t, modbusSettings);
    await t.click(SettingsPage.saveButton);
    await TopMenu.clickStatus(t);
    await TopMenu.clickSettings(t);
    await ModbusPage.assertModbus(t, modbusSettings);
  }
}
