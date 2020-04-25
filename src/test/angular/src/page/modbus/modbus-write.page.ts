import {ModbusWrite} from '../../../../../main/angular/src/app/modbus/write/modbus-write';
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
import {ModbusWriteValue} from '../../../../../main/angular/src/app/modbus/write-value/modbus-write-value';
import {ModbusWriteValuePage} from './modbus-write-value.page';


export class ModbusWritePage {

  private static selectorBase(modbusReadIndex: number) {
    return `app-modbus-write:nth-child(${modbusReadIndex + 1})`;
  }

  public static async setModbusWrite(t: TestController, modbusWrite: ModbusWrite, modbusWriteIndex: number, selectorPrefix?: string) {
    await ModbusWritePage.setAddress(t, modbusWrite.address, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.setType(t, modbusWrite.type, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.setFactorToValue(t, modbusWrite.factorToValue, modbusWriteIndex, selectorPrefix);
  }
  public static async assertModbusWrite(t: TestController, modbusWrite: ModbusWrite, modbusWriteIndex: number, selectorPrefix?: string,
                                        i18nPrefix?: string) {
    await ModbusWritePage.assertAddress(t, modbusWrite.address, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.assertType(t, modbusWrite.type, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.assertFactorToValue(t, modbusWrite.factorToValue, modbusWriteIndex, selectorPrefix);
    for (let i = 0; i < modbusWrite.writeValues.length; i++) {
      await this.assertModbusWriteValue(t, modbusWrite.writeValues[i], modbusWriteIndex, i, selectorPrefix, i18nPrefix);
    }
  }

  public static async setModbusWriteValue(t: TestController, modbusWriteValue: ModbusWriteValue, modbusWriteIndex: number,
                                          modbusWriteValueIndex: number, selectorPrefix?: string) {
    const modbusWriteValueSelectorPrefix = `${selectorPrefix} ${ModbusWritePage.selectorBase(modbusWriteIndex)}`;
    await ModbusWriteValuePage.setModbusWriteValue(t, modbusWriteValue, modbusWriteValueIndex, modbusWriteValueSelectorPrefix);
  }
  public static async assertModbusWriteValue(t: TestController, modbusWriteValue: ModbusWriteValue, modbusWriteIndex: number,
                                             modbusWriteValueIndex: number, selectorPrefix?: string, i18nPrefix?: string) {
    const modbusWriteValueSelectorPrefix = `${selectorPrefix} ${ModbusWritePage.selectorBase(modbusWriteIndex)}`;
    await ModbusWriteValuePage.assertModbusWriteValue(t, modbusWriteValue, modbusWriteValueIndex, modbusWriteValueSelectorPrefix,
      i18nPrefix);
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

  public static async setType(t: TestController, type: string, modbusWriteIndex: number,
                              selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusWritePage.selectorBase(modbusWriteIndex)), type);
  }
  public static async assertType(t: TestController, type: string, modbusWriteIndex: number, selectorPrefix?: string) {
    await assertSelect(t, selectorSelectedByFormControlName('type', selectorPrefix,
      ModbusWritePage.selectorBase(modbusWriteIndex)), type);
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

  public static async clickAddModbusWrite(t: TestController, selectorPrefix?: string, buttonClass?: string) {
    await clickButton(t, selectorButton(selectorPrefix, buttonClass));
  }
}
