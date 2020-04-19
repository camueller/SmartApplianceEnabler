import {Selector} from 'testcafe';
import {ModbusSetting} from '../../../../../main/angular/src/app/settings/modbus/modbus-setting';
import {TopMenu} from '../top-menu.page';
import {ModbusPage} from './modbus.page';
import {SideMenu} from '../side.menu.page';
import {clickButton} from '../../shared/form';

export class SettingsPage {
  private static selectorBase = 'app-settings';
  private static saveButton = Selector('button[type="submit"]');
  private static addModbusButton = Selector(`${SettingsPage.selectorBase} button.SettingsComponent__addModbusSetting`);

  public static async addModbus(t: TestController, modbusSettings: ModbusSetting) {
    await TopMenu.clickMenu(t);
    await SideMenu.clickSettings(t);
    const modbusSettingsIndex = await ModbusPage.getModbusSettingsCount();
    await clickButton(t, SettingsPage.addModbusButton);
    await ModbusPage.addModbus(t, modbusSettings, modbusSettingsIndex);
    await clickButton(t, SettingsPage.saveButton);
    await SideMenu.clickStatus(t);
    await SideMenu.clickSettings(t);
    await ModbusPage.assertModbus(t, modbusSettings, modbusSettingsIndex);
  }
}
