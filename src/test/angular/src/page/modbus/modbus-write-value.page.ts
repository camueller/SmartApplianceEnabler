import {
  assertInput,
  assertSelect,
  getIndexedSelectOptionValueRegExp,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName
} from '../../shared/form';
import {ModbusWriteValue} from '../../../../../main/angular/src/app/modbus/write-value/modbus-write-value';

export class ModbusWriteValuePage {

  private static selectorBase(modbusWriteValueIndex: number) {
    return `app-modbus-write-value:nth-child(${modbusWriteValueIndex + 1})`;
  }

  public static async setModbusWriteValue(t: TestController, modbusWriteValue: ModbusWriteValue,
                                         modbusWriteValueIndex: number, selectorPrefix?: string) {
    await ModbusWriteValuePage.setName(t, modbusWriteValue.name, modbusWriteValueIndex, selectorPrefix);
    await ModbusWriteValuePage.setValue(t, modbusWriteValue.value, modbusWriteValueIndex, selectorPrefix);
  }

  public static async assertModbusWriteValue(t: TestController, modbusWriteValue: ModbusWriteValue,
                                            modbusWriteValueIndex: number, selectorPrefix?: string) {
    await ModbusWriteValuePage.assertName(t, modbusWriteValue.name, modbusWriteValueIndex, selectorPrefix);
    await ModbusWriteValuePage.assertValue(t, modbusWriteValue.value, modbusWriteValueIndex, selectorPrefix);
  }

  public static async setName(t: TestController, name: string, modbusWriteValueIndex: number, selectorPrefix?: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('name', selectorPrefix,
      ModbusWriteValuePage.selectorBase(modbusWriteValueIndex)), name, true);
  }
  public static async assertName(t: TestController, name: string, modbusWriteValueIndex: number,
                                 selectorPrefix?: string) {
    await assertSelect(t, selectorSelectByFormControlName('name', selectorPrefix,
      ModbusWriteValuePage.selectorBase(modbusWriteValueIndex)), getIndexedSelectOptionValueRegExp(name));
  }

  public static async setValue(t: TestController, value: string, modbusWriteValueIndex: number, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('value', selectorPrefix,
      ModbusWriteValuePage.selectorBase(modbusWriteValueIndex)), value);
  }
  public static async assertValue(t: TestController, value: string, modbusWriteValueIndex: number, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('value', selectorPrefix,
      ModbusWriteValuePage.selectorBase(modbusWriteValueIndex)), value);
  }
}
