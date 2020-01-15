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

  public static async setName(t: TestController, name: string, modbusReadValueIndex: number,
                              selectorPrefix?: string): Promise<TestController> {
    await selectOptionByAttribute(t, selectorSelectByFormControlName(modbusReadValueIndex, selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex), 'name'), name, true);
    return t;
  }
  public static async assertName(t: TestController, name: string, modbusReadValueIndex: number,
                                 selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName(modbusReadValueIndex, selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex), 'name'), getIndexedSelectOptionValueRegExp(name));
  }

  public static async setExtractionRegex(t: TestController, extractionRegex: string, modbusReadValueIndex: number,
                                         selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName(modbusReadValueIndex, selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex), 'extractionRegex'), extractionRegex);
  }
  public static async assertExtractionRegex(t: TestController, extractionRegex: string, modbusReadValueIndex: number,
                                            selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName(modbusReadValueIndex, selectorPrefix,
      ModbusReadValuePage.selectorBase(modbusReadValueIndex), 'extractionRegex'), extractionRegex);
  }
}
