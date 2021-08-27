import {ModbusRead} from '../../../../../main/angular/src/app/modbus/read/modbus-read';
import {
  assertInput,
  assertSelectOption,
  clickButton,
  inputText,
  selectOption,
  selectorButton,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {ModbusReadValuePage} from './modbus-read-value.page';

export class ModbusReadPage {

  private static selectorBase(modbusReadIndex?: number) {
    if (modbusReadIndex) {
      return `*[formarrayname="modbusReads"] > app-modbus-read:nth-child(${modbusReadIndex + 1})`;
    }
    return `app-modbus-read`;
  }

  public static async setModbusRead(t: TestController, modbusRead: ModbusRead, modbusReadIndex?: number, selectorPrefix?: string) {
    await ModbusReadPage.setAddress(t, modbusRead.address, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setType(t, modbusRead.type, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setValueType(t, modbusRead.valueType, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.setWords(t, modbusRead.words, modbusReadIndex, selectorPrefix);
    if (modbusRead.byteOrder) {
      await ModbusReadPage.setByteOrder(t, modbusRead.byteOrder, modbusReadIndex, selectorPrefix);
    }
    if (modbusRead.factorToValue) {
      await ModbusReadPage.setFactorToValue(t, modbusRead.factorToValue, modbusReadIndex, selectorPrefix);
    }
    for (let i = 0; i < modbusRead.readValues.length; i++) {
      const modbusReadValueSelectorPrefix = `${selectorPrefix || ''} ${ModbusReadPage.selectorBase(modbusReadIndex)}`;
      await ModbusReadValuePage.setModbusReadValue(t, modbusRead.readValues[i], i, modbusReadValueSelectorPrefix);
    }
  }
  public static async assertModbusRead(t: TestController, modbusRead: ModbusRead, modbusReadIndex: number, selectorPrefix?: string,
                                       i18nPrefix?: string) {
    await ModbusReadPage.assertAddress(t, modbusRead.address, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertType(t, modbusRead.type, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertValueType(t, modbusRead.valueType, modbusReadIndex, selectorPrefix);
    await ModbusReadPage.assertWords(t, modbusRead.words, modbusReadIndex, selectorPrefix);
    if (modbusRead.byteOrder) {
      await ModbusReadPage.assertByteOrder(t, modbusRead.byteOrder, modbusReadIndex, selectorPrefix);
    }
    if (modbusRead.factorToValue) {
      await ModbusReadPage.assertFactorToValue(t, modbusRead.factorToValue, modbusReadIndex, selectorPrefix);
    }
    for (let i = 0; i < modbusRead.readValues.length; i++) {
      const modbusReadValueSelectorPrefix = `${selectorPrefix || ''} ${ModbusReadPage.selectorBase(modbusReadIndex)}`;
      await ModbusReadValuePage.assertModbusReadValue(t, modbusRead.readValues[i], i, modbusReadValueSelectorPrefix, i18nPrefix);
    }
  }
  public static async setAddress(t: TestController, address: string, modbusReadIndex?: number,
                                 selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('address', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), address);
  }
  public static async assertAddress(t: TestController, address: string, modbusReadIndex?: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('address', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), address);
  }

  public static async setType(t: TestController, type: string, modbusReadIndex?: number,
                              selectorPrefix?: string) {
    await selectOption(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), type);
  }
  public static async assertType(t: TestController, type: string, modbusReadIndex?: number, selectorPrefix?: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('type', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), type, 'ModbusReadComponent.type.');
  }

  public static async setValueType(t: TestController, valueType: string, modbusReadIndex?: number,
                              selectorPrefix?: string) {
    await selectOption(t, selectorSelectByFormControlName('valueType', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), valueType);
  }
  public static async assertValueType(t: TestController, valueType: string, modbusReadIndex?: number, selectorPrefix?: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('valueType', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), valueType, 'ModbusReadComponent.valueType.');
  }

  public static async setWords(t: TestController, words: number, modbusReadIndex?: number,
                               selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('words', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), words && words.toString());
  }
  public static async assertWords(t: TestController, words: number, modbusReadIndex?: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('words', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), words && words.toString());
  }

  public static async setByteOrder(t: TestController, byteOrder: string, modbusReadIndex?: number,
                                   selectorPrefix?: string) {
    await selectOption(t, selectorSelectByFormControlName('byteOrder', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), byteOrder);
  }
  public static async assertByteOrder(t: TestController, byteOrder: string, modbusReadIndex?: number, selectorPrefix?: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('byteOrder', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), byteOrder, 'ModbusReadComponent.byteOrder.');
  }

  public static async setFactorToValue(t: TestController, factorToValue: number, modbusReadIndex?: number,
                                       selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), factorToValue && factorToValue.toString());
  }
  public static async assertFactorToValue(t: TestController, factorToValue: number, modbusReadIndex?: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('factorToValue', selectorPrefix,
      ModbusReadPage.selectorBase(modbusReadIndex)), factorToValue && factorToValue.toString());
  }

  public static async clickAddModbusRead(t: TestController, selectorPrefix?: string, buttonClass?: string) {
    await clickButton(t, selectorButton(selectorPrefix, buttonClass));
  }
}
