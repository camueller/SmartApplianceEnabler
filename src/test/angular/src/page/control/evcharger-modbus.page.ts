import {
  assertInput,
  assertSelectOption,
  inputText,
  selectOption, selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName
} from '../../shared/form';
import {EvModbusControl} from '../../../../../main/angular/src/app/control/evcharger/modbus/ev-modbus-control';
import {ModbusRead} from '../../../../../main/angular/src/app/modbus/read/modbus-read';
import {ModbusReadPage} from '../modbus/modbus-read.page';
import {ModbusWrite} from '../../../../../main/angular/src/app/modbus/write/modbus-write';
import {ModbusWritePage} from '../modbus/modbus-write.page';

export class EvchargerModbusPage {

  public static async setEvChargerModbus(t: TestController, evModbusControl: EvModbusControl) {
    await EvchargerModbusPage.setIdRef(t, evModbusControl.idref);
    await EvchargerModbusPage.setSlaveAddress(t, evModbusControl.slaveAddress);
  }

  public static async assertEvChargerModbus(t: TestController, evModbusControl: EvModbusControl) {
    await EvchargerModbusPage.assertIdRef(t, evModbusControl.idref);
    await EvchargerModbusPage.assertSlaveAddress(t, evModbusControl.slaveAddress);
    await EvchargerModbusPage.assertModbusReads(t, evModbusControl.modbusReads);
    await EvchargerModbusPage.assertModbusWrites(t, evModbusControl.modbusWrites);
  }


  public static async setIdRef(t: TestController, idref: string) {
    await selectOption(t, selectorSelectByFormControlName('idref'), idref);
  }

  public static async assertIdRef(t: TestController, idref: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('idref'), idref);
  }

  public static async setSlaveAddress(t: TestController, slaveAddress: string) {
    await inputText(t, selectorInputByFormControlName('slaveAddress'), slaveAddress);
  }

  public static async assertSlaveAddress(t: TestController, slaveAddress: string) {
    await assertInput(t, selectorInputByFormControlName('slaveAddress'), slaveAddress);
  }

  public static async setModbusReads(t: TestController, modbusReads: ModbusRead[]) {
    for (let i = 0; i < modbusReads.length; i++) {
      await ModbusReadPage.setModbusRead(t, modbusReads[i], i, 'app-control-evcharger-modbus');
    }
  }
  public static async assertModbusReads(t: TestController, modbusReads: ModbusRead[]) {
    for (let i = 0; i < modbusReads.length; i++) {
      await ModbusReadPage.assertModbusRead(t, modbusReads[i], i, 'app-control-evcharger-modbus', 'ControlEvchargerComponent.');
    }
  }

  public static async setModbusWrites(t: TestController, modbusWrites: ModbusWrite[]) {
    for (let i = 0; i < modbusWrites.length; i++) {
      await ModbusWritePage.setModbusWrite(t, modbusWrites[i], i, 'app-control-evcharger-modbus');
    }
  }
  public static async assertModbusWrites(t: TestController, modbusWrites: ModbusWrite[]) {
    for (let i = 0; i < modbusWrites.length; i++) {
      await ModbusWritePage.assertModbusWrite(t, modbusWrites[i], i, 'app-control-evcharger-modbus', 'ControlEvchargerComponent.');
    }
  }
}
