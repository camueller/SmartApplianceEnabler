import {ModbusSetting} from '../../../../../main/angular/src/app/settings/modbus/modbus-setting';
import {Selector} from 'testcafe';
import {assertInput, clickButton, inputText, selectorInputByFormControlName} from '../../shared/form';

export class ModbusPage {

  private static selectorBase(modbusSettingsIndex: number) {
    return ` app-settings-modbus[ng-reflect-name="${modbusSettingsIndex}"]`;
  }

  public static async getModbusSettingsCount() {
    return await Selector('app-settings-modbus').count;
  }

  public static async addModbus(t: TestController, modbusSettings: ModbusSetting, modbusSettingsIndex: number) {
    await ModbusPage.setId(t, modbusSettings.modbusTcpId, modbusSettingsIndex);
    await ModbusPage.setHost(t, modbusSettings.modbusTcpHost, modbusSettingsIndex);
    await ModbusPage.setPort(t, modbusSettings.modbusTcpPort, modbusSettingsIndex);
  }
  public static async assertModbus(t: TestController, modbusSettings: ModbusSetting, modbusSettingsIndex: number) {
    await ModbusPage.assertId(t, modbusSettings.modbusTcpId, modbusSettingsIndex);
    await ModbusPage.assertHost(t, modbusSettings.modbusTcpHost, modbusSettingsIndex);
    await ModbusPage.assertPort(t, modbusSettings.modbusTcpPort, modbusSettingsIndex);
  }

  public static async setId(t: TestController, id: string, modbusSettingsIndex: number) {
    await inputText(t, selectorInputByFormControlName('modbusTcpId', undefined,
      ModbusPage.selectorBase(modbusSettingsIndex)), id);
  }
  public static async assertId(t: TestController, id: string, modbusSettingsIndex: number) {
    await assertInput(t, selectorInputByFormControlName('modbusTcpId', undefined,
      ModbusPage.selectorBase(modbusSettingsIndex)), id);
  }

  public static async setHost(t: TestController, host: string, modbusSettingsIndex: number) {
    await inputText(t, selectorInputByFormControlName('modbusTcpHost', undefined,
      ModbusPage.selectorBase(modbusSettingsIndex)), host);
  }
  public static async assertHost(t: TestController, host: string, modbusSettingsIndex: number) {
    await assertInput(t, selectorInputByFormControlName('modbusTcpHost', undefined,
      ModbusPage.selectorBase(modbusSettingsIndex)), host);
  }

  public static async setPort(t: TestController, port: number, modbusSettingsIndex: number) {
    await inputText(t, selectorInputByFormControlName('modbusTcpPort', undefined,
      ModbusPage.selectorBase(modbusSettingsIndex)), port && port.toString());
  }
  public static async assertPort(t: TestController, port: number, modbusSettingsIndex: number) {
    await assertInput(t, selectorInputByFormControlName('modbusTcpPort', undefined,
      ModbusPage.selectorBase(modbusSettingsIndex)), port && port.toString());
  }
}
