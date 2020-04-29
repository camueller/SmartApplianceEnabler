import {ModbusSetting} from '../../../../../main/angular/src/app/settings/modbus/modbus-setting';
import {TopMenu} from '../top-menu.page';
import {ModbusPage} from './modbus.page';
import {SideMenu} from '../side.menu.page';
import {clickButton} from '../../shared/form';

export class SettingsPage {
  private static selectorBase = 'app-settings';
  private static SAVE_BUTTON_SELECTOR = 'button[type="submit"]';
  private static ADD_MODBUS_BUTTON_SELECTOR = `${SettingsPage.selectorBase} button.SettingsComponent__addModbusSetting`;

  public static async addModbus(t: TestController, modbusSettings: ModbusSetting) {
    await TopMenu.clickMenu(t);
    await SideMenu.clickSettings(t);
    const modbusSettingsIndex = await ModbusPage.getModbusSettingsCount();
    await clickButton(t, SettingsPage.ADD_MODBUS_BUTTON_SELECTOR);
    await ModbusPage.addModbus(t, modbusSettings, modbusSettingsIndex);
    await clickButton(t, SettingsPage.SAVE_BUTTON_SELECTOR);
    await SideMenu.clickStatus(t);
    await SideMenu.clickSettings(t);
    await ModbusPage.assertModbus(t, modbusSettings, modbusSettingsIndex);
  }
}
