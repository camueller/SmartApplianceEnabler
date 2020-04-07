import {
  assertInput,
  assertSelect,
  getIndexedSelectOptionValueRegExp,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName
} from '../../shared/form';
import {ModbusReadValue} from '../../../../../main/angular/src/app/modbus/read-value/modbus-read-value';

export class ModbusReadValuePage {

  private static selectorBase(modbusReadValueIndex: number) {
    return `app-modbus-read-value:nth-child(${modbusReadValueIndex + 1})`;
  }

  public static async setModbusReadValue(t: TestController, modbusReadValue: ModbusReadValue,
                                       modbusReadValueIndex: number, selectorPrefix?: string) {
    await ModbusReadValuePage.setName(t, modbusReadValue.name, modbusReadValueIndex, selectorPrefix);
    await ModbusReadValuePage.setExtractionRegex(t, modbusReadValue.extractionRegex, modbusReadValueIndex, selectorPrefix);
  }

  public static async assertModbusReadValue(t: TestController, modbusReadValue: ModbusReadValue,
                                          modbusReadValueIndex: number, selectorPrefix?: string) {
    await ModbusReadValuePage.assertName(t, modbusReadValue.name, modbusReadValueIndex, selectorPrefix);
    await ModbusReadValuePage.assertExtractionRegex(t, modbusReadValue.extractionRegex, modbusReadValueIndex, selectorPrefix);
  }

  public static async setName(t: TestController, name: string, modbusReadValueIndex: number, selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('name', selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex)), name, true);
  }
  public static async assertName(t: TestController, name: string, modbusReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('name', selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex)), getIndexedSelectOptionValueRegExp(name));
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
