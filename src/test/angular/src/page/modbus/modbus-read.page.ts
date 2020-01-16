import {ModbusRead} from '../../../../../main/angular/src/app/modbus-read/modbus-read';
import {
  assertInput,
  assertSelect,
  getIndexedSelectOptionValueRegExp,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName
} from '../../shared/form';
import {ModbusReadValue} from '../../../../../main/angular/src/app/modbus-read-value/modbus-read-value';
import {ModbusReadValuePage} from './modbus-read-value.page';
import { Selector } from 'testcafe';

export class ModbusReadPage {

  private static selectorBase(modbusReadIndex: number) {
    return `app-modbus-read:nth-child(${modbusReadIndex + 1})`;
  }

  private static addModbusReadButton(selectorPrefix?: string) {
    return Selector(`${selectorPrefix || ''} button:nth-child(2)`);
  }

  public static async setModbusRead(t: TestController, modbusRead: ModbusRead, modbusReadIndex: number, selectorPrefix?: string) {
    ModbusReadPage.setAddress(t, modbusRead.address, modbusReadIndex, selectorPrefix);
    ModbusReadPage.setType(t, modbusRead.type, modbusReadIndex, selectorPrefix);
    ModbusReadPage.setBytes(t, modbusRead.bytes, modbusReadIndex, selectorPrefix);
    ModbusReadPage.setByteOrder(t, modbusRead.byteOrder, modbusReadIndex, selectorPrefix);
    ModbusReadPage.setFactorToValue(t, modbusRead.factorToValue, modbusReadIndex, selectorPrefix);
  }
  public static async assertModbusRead(t: TestController, modbusRead: ModbusRead, modbusReadIndex: number, selectorPrefix?: string) {
    ModbusReadPage.assertAddress(t, modbusRead.address, modbusReadIndex, selectorPrefix);
    ModbusReadPage.assertType(t, modbusRead.type, modbusReadIndex, selectorPrefix);
    ModbusReadPage.assertBytes(t, modbusRead.bytes, modbusReadIndex, selectorPrefix);
    ModbusReadPage.assertByteOrder(t, modbusRead.byteOrder, modbusReadIndex, selectorPrefix);
    ModbusReadPage.assertFactorToValue(t, modbusRead.factorToValue, modbusReadIndex, selectorPrefix);
  }

  public static async setModbusReadValue(t: TestController, modbusReadValue: ModbusReadValue, modbusReadIndex: number,
                                         selectorPrefix?: string) {
    const modbusReadValueSelectorPrefix = `${selectorPrefix} ${ModbusReadPage.selectorBase(modbusReadIndex)}`;
    await ModbusReadValuePage.setModbusReadValue(t, modbusReadValue, 0, modbusReadValueSelectorPrefix);
  }
  public static async assertModbusReadValue(t: TestController, modbusReadValue: ModbusReadValue, modbusReadIndex: number,
                                            selectorPrefix?: string) {
    const modbusReadValueSelectorPrefix = `${selectorPrefix} ${ModbusReadPage.selectorBase(modbusReadIndex)}`;
    await ModbusReadValuePage.assertModbusReadValue(t, modbusReadValue, 0, modbusReadValueSelectorPrefix);
  }

  public static async setAddress(t: TestController, address: string, modbusReadIndex: number,
                                 selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('address', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), address);
  }
  public static async assertAddress(t: TestController, address: string, modbusReadIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('address', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), address);
  }

  public static async setType(t: TestController, method: string, modbusReadIndex: number,
                              selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), method, true);
  }
  public static async assertType(t: TestController, method: string, modbusReadIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), getIndexedSelectOptionValueRegExp(method));
  }

  public static async setBytes(t: TestController, bytes: number, modbusReadIndex: number,
                                 selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('bytes', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), bytes && bytes.toString());
  }
  public static async assertBytes(t: TestController, bytes: number, modbusReadIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('bytes', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), bytes && bytes.toString());
  }

  public static async setByteOrder(t: TestController, byteOrder: string, modbusReadIndex: number,
                              selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('byteOrder', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), byteOrder, true);
  }
  public static async assertByteOrder(t: TestController, byteOrder: string, modbusReadIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('byteOrder', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), getIndexedSelectOptionValueRegExp(byteOrder));
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, modbusReadIndex: number,
                               selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), factorToValue && factorToValue.toString());
  }
  public static async assertFactorToValue(t: TestController, factorToValue: number, modbusReadIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), factorToValue && factorToValue.toString());
  }

  public static async clickAddModbusRead(t: TestController, selectorPrefix?: string) {
    await t.click(ModbusReadPage.addModbusReadButton(selectorPrefix));
  }
}
