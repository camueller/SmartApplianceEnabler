import {ModbusSettings} from '../../../../../main/angular/src/app/settings/modbus-settings';
import {Selector} from 'testcafe';
import {assertInput, inputText, selectorInputByFormControlName} from '../../shared/form';

export class ModbusPage {

  private static selectorBase = 'app-settings';
  private static addModbusButton = Selector(`${ModbusPage.selectorBase} i.green.icon.add`);

  public static async addModbus(t: TestController, modbusSettings: ModbusSettings) {
    await t.click(ModbusPage.addModbusButton);
    await ModbusPage.setId(t, modbusSettings.modbusTcpId);
    await ModbusPage.setHost(t, modbusSettings.modbusTcpHost);
    await ModbusPage.setPort(t, modbusSettings.modbusTcpPort);
  }
  public static async assertModbus(t: TestController, modbusSettings: ModbusSettings) {
    await t.click(ModbusPage.addModbusButton);
    await ModbusPage.assertId(t, modbusSettings.modbusTcpId);
    await ModbusPage.assertHost(t, modbusSettings.modbusTcpHost);
    await ModbusPage.assertPort(t, modbusSettings.modbusTcpPort);
  }

  public static async setId(t: TestController, id: string) {
    await inputText(t, selectorInputByFormControlName('modbusTcpId'), id);
  }
  public static async assertId(t: TestController, id: string) {
    await assertInput(t, selectorInputByFormControlName('modbusTcpId'), id);
  }

  public static async setHost(t: TestController, host: string) {
    await inputText(t, selectorInputByFormControlName('modbusTcpHost'), host);
  }
  public static async assertHost(t: TestController, host: string) {
    await assertInput(t, selectorInputByFormControlName('modbusTcpHost'), host);
  }

  public static async setPort(t: TestController, port: number) {
    await inputText(t, selectorInputByFormControlName('modbusTcpPort'), port && port.toString());
  }
  public static async assertPort(t: TestController, port: number) {
    await assertInput(t, selectorInputByFormControlName('modbusTcpPort'), port && port.toString());
  }
}
