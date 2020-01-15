import {MeterPage} from './meter.page';
import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {ModbusElectricityMeter} from '../../../../../main/angular/src/app/meter-modbus/modbus-electricity-meter';
import {ModbusReadPage} from '../modbus/modbus-read.page';
import {assertInput, getIndexedSelectOptionValueRegExp, inputText, selectOptionByAttribute} from '../../shared/form';
import {Selector} from 'testcafe';
import {settings} from '../../fixture/settings/settings';

export class ModbusMeterPage extends MeterPage {

  private static selectorPrefix = 'app-meter-modbus';
  private static idRefSelect = Selector('select[formcontrolname="idref"]');
  private static slaveAddressInput = Selector('input[formcontrolname="slaveAddress"]');

  public static async setModbusElectricityMeter(t: TestController, modbusElectricityMeter: ModbusElectricityMeter) {
    await ModbusMeterPage.setType(t, ModbusElectricityMeter.TYPE);

    await ModbusMeterPage.setIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusMeterPage.setAddress(t, modbusElectricityMeter.slaveAddress);

    const powerModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Power));
    let modbusReadIndex = 0;
    await ModbusReadPage.setModbusRead(t, powerModbusRead, modbusReadIndex, this.selectorPrefix);
    await ModbusReadPage.setModbusReadValue(t, powerModbusRead.readValues[0], modbusReadIndex, this.selectorPrefix);

    const energyModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Energy));
    if (energyModbusRead) {
      modbusReadIndex = 1;
      await ModbusReadPage.clickAddModbusRead(t, this.selectorPrefix);
      await ModbusReadPage.setModbusRead(t, energyModbusRead, modbusReadIndex, this.selectorPrefix);
      await ModbusReadPage.setModbusReadValue(t, energyModbusRead.readValues[0], modbusReadIndex, this.selectorPrefix);
    }
  }
  public static async assertModbusElectricityMeter(t: TestController, modbusElectricityMeter: ModbusElectricityMeter) {
    await ModbusMeterPage.assertType(t, ModbusElectricityMeter.TYPE);

    await ModbusMeterPage.assertIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusMeterPage.assertAddress(t, modbusElectricityMeter.slaveAddress);

    const powerModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Power));
    let modbusReadIndex = 0;
    await ModbusReadPage.assertModbusRead(t, powerModbusRead, modbusReadIndex, this.selectorPrefix);
    await ModbusReadPage.assertModbusReadValue(t, powerModbusRead.readValues[0], modbusReadIndex, this.selectorPrefix);

    const energyModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Energy));
    if (energyModbusRead) {
      modbusReadIndex = 1;
      await ModbusReadPage.assertModbusRead(t, energyModbusRead, modbusReadIndex, this.selectorPrefix);
      await ModbusReadPage.assertModbusReadValue(t, energyModbusRead.readValues[0], modbusReadIndex, this.selectorPrefix);
    }
  }

  public static async setIdRef(t: TestController, idRef: string): Promise<TestController> {
    await selectOptionByAttribute(t, ModbusMeterPage.idRefSelect, idRef, true);
    return t;
  }
  public static async assertIdRef(t: TestController, idRef: string) {
    await t.expect(ModbusMeterPage.idRefSelect.value).match(getIndexedSelectOptionValueRegExp(idRef));
  }

  public static async setAddress(t: TestController, address: string) {
    await inputText(t, ModbusMeterPage.slaveAddressInput, address);
  }
  public static async assertAddress(t: TestController, address: string) {
    await assertInput(t, ModbusMeterPage.slaveAddressInput, address);
  }
}
