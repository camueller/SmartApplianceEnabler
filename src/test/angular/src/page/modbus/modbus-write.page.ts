import {ModbusWrite} from '../../../../../main/angular/src/app/modbus-write/modbus-write';
import {
  assertInput,
  assertSelect,
  getIndexedSelectOptionValueRegExp,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName
} from '../../shared/form';
import {ModbusWriteValue} from '../../../../../main/angular/src/app/modbus-write-value/modbus-write-value';
import {ModbusWriteValuePage} from './modbus-write-value.page';
import {Selector} from 'testcafe';


export class ModbusWritePage {

  private static selectorBase(modbusReadIndex: number) {
    return `app-modbus-write:nth-child(${modbusReadIndex + 1})`;
  }

  private static addModbusWriteButton(selectorPrefix?: string) {
    return Selector(`${selectorPrefix || ''} button:nth-child(2)`);
  }

  public static async setModbusWrite(t: TestController, modbusWrite: ModbusWrite, modbusWriteIndex: number, selectorPrefix?: string) {
    ModbusWritePage.setAddress(t, modbusWrite.address, modbusWriteIndex, selectorPrefix);
    ModbusWritePage.setType(t, modbusWrite.type, modbusWriteIndex, selectorPrefix);
    ModbusWritePage.setFactorToValue(t, modbusWrite.factorToValue, modbusWriteIndex, selectorPrefix);
  }

  public static async assertModbusWrite(t: TestController, modbusWrite: ModbusWrite, modbusWriteIndex: number, selectorPrefix?: string) {
    ModbusWritePage.assertAddress(t, modbusWrite.address, modbusWriteIndex, selectorPrefix);
    ModbusWritePage.assertType(t, modbusWrite.type, modbusWriteIndex, selectorPrefix);
    ModbusWritePage.assertFactorToValue(t, modbusWrite.factorToValue, modbusWriteIndex, selectorPrefix);
  }

  public static async setModbusWriteValue(t: TestController, modbusWriteValue: ModbusWriteValue, modbusWriteIndex: number,
                                          selectorPrefix?: string) {
    const modbusWriteValueSelectorPrefix = `${selectorPrefix} ${ModbusWritePage.selectorBase(modbusWriteIndex)}`;
    await ModbusWriteValuePage.setModbusWriteValue(t, modbusWriteValue, 0, modbusWriteValueSelectorPrefix);
  }

  public static async assertModbusWriteValue(t: TestController, modbusWriteValue: ModbusWriteValue, modbusWriteIndex: number,
                                             selectorPrefix?: string) {
    const modbusWriteValueSelectorPrefix = `${selectorPrefix} ${ModbusWritePage.selectorBase(modbusWriteIndex)}`;
    await ModbusWriteValuePage.assertModbusWriteValue(t, modbusWriteValue, 0, modbusWriteValueSelectorPrefix);
  }

  public static async setAddress(t: TestController, address: string, modbusReadIndex: number,
                                 selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('address', selectorPrefix,
      ModbusWritePage.selectorBase(modbusReadIndex)), address);
  }

  public static async assertAddress(t: TestController, address: string, modbusReadIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('address', selectorPrefix,
      ModbusWritePage.selectorBase(modbusReadIndex)), address);
  }

  public static async setType(t: TestController, method: string, modbusWriteIndex: number,
                              selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusWritePage.selectorBase(modbusWriteIndex)), method, true);
  }

  public static async assertType(t: TestController, method: string, modbusWriteIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusWritePage.selectorBase(modbusWriteIndex)), getIndexedSelectOptionValueRegExp(method));
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, modbusWriteIndex: number,
                                       selectorPrefix?: string) {
    if (factorToValue) {
      await inputText(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
        ModbusWritePage.selectorBase(modbusWriteIndex)), factorToValue && factorToValue.toString());
    }
  }

  public static async assertFactorToValue(t: TestController, factorToValue: number, modbusWriteIndex: number, selectorPrefix?: string) {
    if (factorToValue) {
      await assertInput(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
        ModbusWritePage.selectorBase(modbusWriteIndex)), factorToValue && factorToValue.toString());
    }
  }

  public static async clickAddModbusWrite(t: TestController, selectorPrefix?: string) {
    await t.click(ModbusWritePage.addModbusWriteButton(selectorPrefix));
  }
}
