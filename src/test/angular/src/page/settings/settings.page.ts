import {Selector} from 'testcafe';
import {ModbusSetting} from '../../../../../main/angular/src/app/settings/modbus/modbus-setting';
import {TopMenu} from '../top-menu.page';
import {ModbusPage} from './modbus.page';
import {SideMenu} from '../side.menu.page';

export class SettingsPage {
  private static saveButton = Selector('button[type="submit"]');

  public static async addModbus(t: TestController, modbusSettings: ModbusSetting) {
    await TopMenu.clickMenu(t);
    await SideMenu.clickSettings(t);
    await ModbusPage.addModbus(t, modbusSettings);
    await t.click(SettingsPage.saveButton);
    await SideMenu.clickStatus(t);
    await SideMenu.clickSettings(t);
    await ModbusPage.assertModbus(t, modbusSettings);
  }
}
