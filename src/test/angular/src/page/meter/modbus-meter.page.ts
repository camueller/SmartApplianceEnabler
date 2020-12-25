import {MeterPage} from './meter.page';
import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {ModbusElectricityMeter} from '../../../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {ModbusReadPage} from '../modbus/modbus-read.page';
import {
  assertInput,
  assertSelectOption,
  inputText,
  selectOption,
  selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {settings} from '../../fixture/settings/settings';

export class ModbusMeterPage extends MeterPage {

  private static selectorPrefix = 'app-meter-modbus';
  private static i18nPrefix = 'MeterModbusComponent.';

  public static async setModbusElectricityMeter(t: TestController, modbusElectricityMeter: ModbusElectricityMeter) {
    await ModbusMeterPage.setType(t, ModbusElectricityMeter.TYPE);

    await ModbusMeterPage.setIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusMeterPage.setAddress(t, modbusElectricityMeter.slaveAddress);

    const powerModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Power));
    let modbusReadIndex = 0;
    await ModbusReadPage.setModbusRead(t, powerModbusRead, modbusReadIndex, this.selectorPrefix);

    const energyModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Energy));
    if (energyModbusRead) {
      modbusReadIndex = 1;
      await ModbusReadPage.clickAddModbusRead(t, this.selectorPrefix, 'MeterModbusComponent__addModbusRead');
      await ModbusReadPage.setModbusRead(t, energyModbusRead, modbusReadIndex, this.selectorPrefix);
    }
  }
  public static async assertModbusElectricityMeter(t: TestController, modbusElectricityMeter: ModbusElectricityMeter) {
    await ModbusMeterPage.assertType(t, ModbusElectricityMeter.TYPE);

    await ModbusMeterPage.assertIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusMeterPage.assertAddress(t, modbusElectricityMeter.slaveAddress);

    const powerModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Power));
    let modbusReadIndex = 0;
    await ModbusReadPage.assertModbusRead(t, powerModbusRead, modbusReadIndex, this.selectorPrefix, this.i18nPrefix);

    const energyModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Energy));
    if (energyModbusRead) {
      modbusReadIndex = 1;
      await ModbusReadPage.assertModbusRead(t, energyModbusRead, modbusReadIndex, this.selectorPrefix, this.i18nPrefix);
    }
  }

  public static async setIdRef(t: TestController, idref: string) {
    await selectOption(t, selectorSelectByFormControlName('idref'), idref);
  }
  public static async assertIdRef(t: TestController, idref: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('idref'), idref);
  }

  public static async setAddress(t: TestController, address: string) {
    await inputText(t, selectorInputByFormControlName('slaveAddress'), address);
  }
  public static async assertAddress(t: TestController, address: string) {
    await assertInput(t, selectorInputByFormControlName('slaveAddress'), address);
  }
}
