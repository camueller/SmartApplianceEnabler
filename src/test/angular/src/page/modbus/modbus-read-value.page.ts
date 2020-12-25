import {
  assertInput,
  assertSelectOption,
  inputText,
  selectOption,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {ModbusReadValue} from '../../../../../main/angular/src/app/modbus/read-value/modbus-read-value';

export class ModbusReadValuePage {

  private static selectorBase(modbusReadValueIndex: number) {
    return `div > div > div:nth-child(${modbusReadValueIndex + 1}) > div > app-modbus-read-value`;
  }

  public static async setModbusReadValue(t: TestController, modbusReadValue: ModbusReadValue,
                                       modbusReadValueIndex: number, selectorPrefix?: string) {
    await ModbusReadValuePage.setName(t, modbusReadValue.name, modbusReadValueIndex, selectorPrefix);
    await ModbusReadValuePage.setExtractionRegex(t, modbusReadValue.extractionRegex, modbusReadValueIndex, selectorPrefix);
  }

  public static async assertModbusReadValue(t: TestController, modbusReadValue: ModbusReadValue,
                                          modbusReadValueIndex: number, selectorPrefix?: string, i18nPrefix?: string) {
    await ModbusReadValuePage.assertName(t, modbusReadValue.name, modbusReadValueIndex, selectorPrefix, i18nPrefix);
    await ModbusReadValuePage.assertExtractionRegex(t, modbusReadValue.extractionRegex, modbusReadValueIndex, selectorPrefix);
  }

  public static async setName(t: TestController, name: string, modbusReadValueIndex: number, selectorPrefix?: string) {
    await selectOption(t, selectorSelectByFormControlName('name', selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex)), name);
  }
  public static async assertName(t: TestController, name: string, modbusReadValueIndex: number,
                                 selectorPrefix?: string, i18nPrefix?: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('name', selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex)), name, i18nPrefix);
  }

  public static async setExtractionRegex(t: TestController, extractionRegex: string, modbusReadValueIndex: number,
                                         selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('extractionRegex', selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex)), extractionRegex);
  }
  public static async assertExtractionRegex(t: TestController, extractionRegex: string, modbusReadValueIndex: number,
                                            selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('extractionRegex', selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex)), extractionRegex);
  }
}
