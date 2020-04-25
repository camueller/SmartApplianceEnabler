import {
  assertInput,
  assertSelect,
  inputText,
  selectOptionByAttribute,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {settings} from '../../fixture/settings/settings';
import {ControlPage} from './control.page';
import {ControlValueName} from '../../../../../main/angular/src/app/control/control-value-name';
import {ModbusWritePage} from '../modbus/modbus-write.page';
import {ModbusSwitch} from '../../../../../main/angular/src/app/control/modbus/modbus-switch';

export class ModbusControlPage extends ControlPage {

  private static selectorPrefix = 'app-control-modbus';
  private static i18nPrefix = 'ControlModbusComponent.';

  public static async setModbusSwitch(t: TestController, modbusSwitch: ModbusSwitch) {
    await ModbusControlPage.setType(t, ModbusSwitch.TYPE);

    await ModbusControlPage.setIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusControlPage.setAddress(t, modbusSwitch.slaveAddress);

    const onModbusWrite = modbusSwitch.modbusWrites.find(
      modbusWrite => modbusWrite.writeValues.find(modbusWriteValue => modbusWriteValue.name === ControlValueName.On));
    let modbusWriteIndex = 0;
    await ModbusWritePage.setModbusWrite(t, onModbusWrite, modbusWriteIndex, this.selectorPrefix);
    await ModbusWritePage.setModbusWriteValue(t, onModbusWrite.writeValues[0], modbusWriteIndex, 0, this.selectorPrefix);

    const offModbusWrite = modbusSwitch.modbusWrites.find(
    modbusWrite => modbusWrite.writeValues.find(modbusWriteValue => modbusWriteValue.name === ControlValueName.Off));
    modbusWriteIndex = 1;
    await ModbusWritePage.clickAddModbusWrite(t, this.selectorPrefix, 'ControlModbusComponent__addModbusWrite');
    await ModbusWritePage.setModbusWrite(t, offModbusWrite, modbusWriteIndex, this.selectorPrefix);
    await ModbusWritePage.setModbusWriteValue(t, offModbusWrite.writeValues[0], modbusWriteIndex, 0, this.selectorPrefix);
  }
  public static async assertModbusSwitch(t: TestController, modbusSwitch: ModbusSwitch) {
    await ModbusControlPage.assertType(t, ModbusSwitch.TYPE);

    await ModbusControlPage.assertIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusControlPage.assertAddress(t, modbusSwitch.slaveAddress);

    const onModbusWrite = modbusSwitch.modbusWrites.find(
      modbusWrite => modbusWrite.writeValues.find(modbusWriteValue => modbusWriteValue.name === ControlValueName.On));
    let modbusWriteIndex = 0;
    await ModbusWritePage.assertModbusWrite(t, onModbusWrite, modbusWriteIndex, this.selectorPrefix);
    await ModbusWritePage.assertModbusWriteValue(t, onModbusWrite.writeValues[0], modbusWriteIndex, 0,
      this.selectorPrefix, this.i18nPrefix);

    const offModbusWrite = modbusSwitch.modbusWrites.find(
      modbusWrite => modbusWrite.writeValues.find(modbusWriteValue => modbusWriteValue.name === ControlValueName.Off));
    modbusWriteIndex = 1;
    await ModbusWritePage.assertModbusWrite(t, offModbusWrite, modbusWriteIndex, this.selectorPrefix);
    await ModbusWritePage.assertModbusWriteValue(t, offModbusWrite.writeValues[0], modbusWriteIndex, 0,
      this.selectorPrefix, this.i18nPrefix);
  }

  public static async setIdRef(t: TestController, idref: string) {
    await selectOptionByAttribute(t, selectorSelectByFormControlName('idref'), idref);
  }
  public static async assertIdRef(t: TestController, idref: string) {
    await assertSelect(t, selectorSelectedByFormControlName('idref'), idref);
  }

  public static async setAddress(t: TestController, address: string) {
    await inputText(t, selectorInputByFormControlName('slaveAddress'), address);
  }
  public static async assertAddress(t: TestController, address: string) {
    await assertInput(t, selectorInputByFormControlName('slaveAddress'), address);
  }
}
