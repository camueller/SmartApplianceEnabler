import {ModbusRead} from '../../../../../main/angular/src/app/modbus/read/modbus-read';
import {
  assertInput,
  assertSelect,
  clickButton,
  inputText,
  selectOptionByAttribute,
  selectorButton,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {ModbusReadValuePage} from './modbus-read-value.page';

export class ModbusReadPage {

  private static selectorBase(modbusReadIndex: number) {
    return `*[formarrayname="modbusReads"] > app-modbus-read:nth-child(${modbusReadIndex + 1})`;
  }

  public static async setModbusRead(t: TestController, modbusRead: ModbusRead, modbusReadIndex: number, selectorPrefix?: string) {
    await ModbusReadPage.setAddress(t, modbusRead.address, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setType(t, modbusRead.type, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setBytes(t, modbusRead.bytes, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setByteOrder(t, modbusRead.byteOrder, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setFactorToValue(t, modbusRead.factorToValue, modbusReadIndex, selectorPrefix);
    for (let i = 0; i < modbusRead.readValues.length; i++) {
      const modbusReadValueSelectorPrefix = `${selectorPrefix} ${ModbusReadPage.selectorBase(modbusReadIndex)}`;
      await ModbusReadValuePage.setModbusReadValue(t, modbusRead.readValues[i], i, modbusReadValueSelectorPrefix);
    }
  }
  public static async assertModbusRead(t: TestController, modbusRead: ModbusRead, modbusReadIndex: number, selectorPrefix?: string,
                                       i18nPrefix?: string) {
    await ModbusReadPage.assertAddress(t, modbusRead.address, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertType(t, modbusRead.type, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertBytes(t, modbusRead.bytes, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertByteOrder(t, modbusRead.byteOrder, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertFactorToValue(t, modbusRead.factorToValue, modbusReadIndex, selectorPrefix);
    for (let i = 0; i < modbusRead.readValues.length; i++) {
      const modbusReadValueSelectorPrefix = `${selectorPrefix} ${ModbusReadPage.selectorBase(modbusReadIndex)}`;
      await ModbusReadValuePage.assertModbusReadValue(t, modbusRead.readValues[i], i, modbusReadValueSelectorPrefix, i18nPrefix);
    }
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

  public static async setType(t: TestController, type: string, modbusReadIndex: number,
                              selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), type);
  }
  public static async assertType(t: TestController, type: string, modbusReadIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectedByFormControlName('type', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), type);
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
      ModbusReadPage.selectorBase(modbusReadIndex)), byteOrder);
  }
  public static async assertByteOrder(t: TestController, byteOrder: string, modbusReadIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectedByFormControlName('byteOrder', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), byteOrder);
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

  public static async clickAddModbusRead(t: TestController, selectorPrefix?: string, buttonClass?: string) {
    await clickButton(t, selectorButton(selectorPrefix, buttonClass));
  }
}
