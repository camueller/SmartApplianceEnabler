import {ModbusWrite} from '../../../../../main/angular/src/app/modbus/write/modbus-write';
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
import {ModbusWriteValuePage} from './modbus-write-value.page';


export class ModbusWritePage {

  private static selectorBase(modbusWriteIndex: number) {
    return `*[formarrayname="modbusWrites"] > app-modbus-write:nth-child(${modbusWriteIndex + 1})`;
  }

  public static async setModbusWrite(t: TestController, modbusWrite: ModbusWrite, modbusWriteIndex: number, selectorPrefix?: string) {
    await ModbusWritePage.setAddress(t, modbusWrite.address, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.setType(t, modbusWrite.type, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.setFactorToValue(t, modbusWrite.factorToValue, modbusWriteIndex, selectorPrefix);
    for (let i = 0; i < modbusWrite.writeValues.length; i++) {
      const modbusWriteValueSelectorPrefix = `${selectorPrefix} ${ModbusWritePage.selectorBase(modbusWriteIndex)}`;
      await ModbusWriteValuePage.setModbusWriteValue(t, modbusWrite.writeValues[i], i, modbusWriteValueSelectorPrefix);
    }
  }
  public static async assertModbusWrite(t: TestController, modbusWrite: ModbusWrite, modbusWriteIndex: number, selectorPrefix?: string,
                                        i18nPrefix?: string) {
    await ModbusWritePage.assertAddress(t, modbusWrite.address, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.assertType(t, modbusWrite.type, modbusWriteIndex, selectorPrefix);
    await ModbusWritePage.assertFactorToValue(t, modbusWrite.factorToValue, modbusWriteIndex, selectorPrefix);
    for (let i = 0; i < modbusWrite.writeValues.length; i++) {
      const modbusWriteValueSelectorPrefix = `${selectorPrefix} ${ModbusWritePage.selectorBase(modbusWriteIndex)}`;
      await ModbusWriteValuePage.assertModbusWriteValue(t, modbusWrite.writeValues[i], i, modbusWriteValueSelectorPrefix,
        i18nPrefix);
    }
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
    await selectOption(t, selectorSelectByFormControlName('type', selectorPrefix,
      ModbusWritePage.selectorBase(modbusWriteIndex)), type);
  }
  public static async assertType(t: TestController, type: string, modbusWriteIndex: number, selectorPrefix?: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('type', selectorPrefix,
      ModbusWritePage.selectorBase(modbusWriteIndex)), type, 'ModbusWriteComponent.type.');
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
