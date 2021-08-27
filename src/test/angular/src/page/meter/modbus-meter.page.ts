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

    await ModbusReadPage.setModbusRead(t, modbusElectricityMeter.modbusReads[0]);

    if (modbusElectricityMeter.pollInterval) {
      await ModbusMeterPage.setPollInterval(t, modbusElectricityMeter.pollInterval);
    }
  }
  public static async assertModbusElectricityMeter(t: TestController, modbusElectricityMeter: ModbusElectricityMeter) {
    await ModbusMeterPage.assertType(t, ModbusElectricityMeter.TYPE);

    await ModbusMeterPage.assertIdRef(t, settings.modbusSettings[0].modbusTcpId);
    await ModbusMeterPage.assertAddress(t, modbusElectricityMeter.slaveAddress);
    if (modbusElectricityMeter.pollInterval) {
      await ModbusMeterPage.assertPollInterval(t, modbusElectricityMeter.pollInterval);
    }

    const powerModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Power));
    if (powerModbusRead) {
      await ModbusReadPage.assertModbusRead(t, powerModbusRead, 0, this.selectorPrefix, this.i18nPrefix);
    }

    const energyModbusRead = modbusElectricityMeter.modbusReads.find(
      modbusRead => modbusRead.readValues.find(modbusReadValue => modbusReadValue.name === MeterValueName.Energy));
    if (energyModbusRead) {
      await ModbusReadPage.assertModbusRead(t, energyModbusRead, 0, this.selectorPrefix, this.i18nPrefix);
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

  public static async setPollInterval(t: TestController, pollInterval: number) {
    await inputText(t, selectorInputByFormControlName('pollInterval'), pollInterval && pollInterval.toString());
  }
  public static async assertPollInterval(t: TestController, pollInterval: number) {
    await assertInput(t, selectorInputByFormControlName('pollInterval'), pollInterval && pollInterval.toString());
  }
}
