/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import {Meter} from './meter';
import {S0ElectricityMeter} from './s0-electricity-meter';
import {ModbusElectricityMeter} from './modbus-electricity-meter';
import {HttpElectricityMeter} from './http-electricity-meter';
import {MeterDefaults} from './meter-defaults';
import {Logger} from '../log/logger';
import {ModbusSwitch} from '../control/modbus-switch';
import {ModbusRegisterRead} from '../shared/modbus-register-read';
import {ModbusRegisterReadValue} from '../shared/modbus-register-read-value';
import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';

export class MeterFactory {

  constructor(private logger: Logger) {
  }

  defaultsFromJSON(rawMeterDefaults: any): MeterDefaults {
    this.logger.debug('MeterDefaults (JSON): ' + JSON.stringify(rawMeterDefaults));
    const meterDefaults = new MeterDefaults();
    meterDefaults.s0ElectricityMeter_measurementInterval
      = rawMeterDefaults.s0ElectricityMeter.measurementInterval;
    meterDefaults.httpElectricityMeter_factorToWatt = rawMeterDefaults.httpElectricityMeter.factorToWatt;
    meterDefaults.httpElectricityMeter_measurementInterval = rawMeterDefaults.httpElectricityMeter.measurementInterval;
    meterDefaults.httpElectricityMeter_pollInterval = rawMeterDefaults.httpElectricityMeter.pollInterval;
    meterDefaults.modbusElectricityMeter_pollInterval = rawMeterDefaults.modbusElectricityMeter.pollInterval;
    this.logger.debug('MeterDefaults (TYPE): ' + JSON.stringify(meterDefaults));
    return meterDefaults;
  }

  createEmptyMeter(): Meter {
    return new Meter();
  }

  fromJSON(rawMeter: any): Meter {
    this.logger.debug('Meter (JSON): ' + JSON.stringify(rawMeter));
    const meter = new Meter();
    meter.type = rawMeter['@class'];
    if (meter.type === S0ElectricityMeter.TYPE) {
      meter.s0ElectricityMeter = this.createS0ElectricityMeter(rawMeter, false);
    } else if (meter.type === S0ElectricityMeter.TYPE_NETWORKED) {
      meter.s0ElectricityMeterNetworked = this.createS0ElectricityMeter(rawMeter, true);
    } else if (meter.type === ModbusElectricityMeter.TYPE) {
      meter.modbusElectricityMeter = this.createModbusElectricityMeter(rawMeter);
    } else if (meter.type === HttpElectricityMeter.TYPE) {
      meter.httpElectricityMeter = this.createHttpElectricityMeter(rawMeter);
    }
    this.logger.debug('Meter (TYPE): ' + JSON.stringify(meter));
    return meter;
  }

  toJSON(meter: Meter): string {
    this.logger.debug('Meter (TYPE): ' + JSON.stringify(meter));
    let meterUsed: any;
    if (meter.type === S0ElectricityMeter.TYPE) {
      meterUsed =  meter.s0ElectricityMeter;
    } else if (meter.type === S0ElectricityMeter.TYPE_NETWORKED) {
      meterUsed =  meter.s0ElectricityMeterNetworked;
    } else if (meter.type === ModbusElectricityMeter.TYPE) {
      meterUsed =  meter.modbusElectricityMeter;
    } else if (meter.type === HttpElectricityMeter.TYPE) {
      meterUsed =  meter.httpElectricityMeter;
    }
    let meterRaw: string;
    if (meterUsed != null) {
      if (meter.type === ModbusElectricityMeter.TYPE) {
        this.toJSONModbusElectricityMeter(meter.modbusElectricityMeter);
      }
      meterRaw = JSON.stringify(meterUsed);
    }
    this.logger.debug('Meter (JSON): ' + meterRaw);
    return meterRaw;
  }

  toJSONModbusElectricityMeter(modbusElectricityMeter: ModbusElectricityMeter) {
    const powerConfiguration = modbusElectricityMeter.powerConfiguration;
    const powerRegisterRead = new ModbusRegisterRead({
      address: powerConfiguration.address,
      bytes: powerConfiguration.bytes,
      type: powerConfiguration.type,
      registerReadValues: [new ModbusRegisterReadValue({name: 'Power'})]
    });
    const energyConfiguration = modbusElectricityMeter.energyConfiguration;
    const energyRegisterRead = new ModbusRegisterRead({
      address: energyConfiguration.address,
      bytes: energyConfiguration.bytes,
      type: energyConfiguration.type,
      registerReadValues: [new ModbusRegisterReadValue({name: 'Energy'})]
    });
    modbusElectricityMeter.registerReads = [powerRegisterRead, energyRegisterRead];
  }

  createS0ElectricityMeter(rawMeter: any, networked: boolean): S0ElectricityMeter {
    const s0ElectricityMeter = new S0ElectricityMeter();
    if (networked) {
      s0ElectricityMeter['@class'] = S0ElectricityMeter.TYPE_NETWORKED;
    }
    s0ElectricityMeter.gpio = rawMeter.gpio;
    s0ElectricityMeter.pinPullResistance = rawMeter.pinPullResistance;
    s0ElectricityMeter.impulsesPerKwh = rawMeter.impulsesPerKwh;
    s0ElectricityMeter.measurementInterval = rawMeter.measurementInterval;
    s0ElectricityMeter.powerOnAlways = rawMeter.powerOnAlways;
    return s0ElectricityMeter;
  }

  createModbusElectricityMeter(rawMeter: any): ModbusElectricityMeter {
    const modbusElectricityMeter = new ModbusElectricityMeter();
    modbusElectricityMeter.idref = rawMeter.idref;
    modbusElectricityMeter.slaveAddress = rawMeter.slaveAddress;
    modbusElectricityMeter.pollInterval = rawMeter.pollInterval;
    modbusElectricityMeter.measurementInterval = rawMeter.measurementInterval;
    if (rawMeter.registerReads != null) {
      rawMeter.registerReads.forEach((registerRead) => {
        const name = registerRead.registerReadValues[0].name;
        if (name === 'Power') {
          modbusElectricityMeter.powerConfiguration = new ModbusRegisterConfguration({
            address: registerRead.address,
            bytes: registerRead.bytes,
            type: registerRead.type
          });
        }
        if (name === 'Energy') {
          modbusElectricityMeter.energyConfiguration = new ModbusRegisterConfguration({
            address: registerRead.address,
            bytes: registerRead.bytes,
            type: registerRead.type
          });
        }
      });
    }
    return modbusElectricityMeter;
  }

  createHttpElectricityMeter(rawMeter: any): HttpElectricityMeter {
    const httpElectricityMeter = new HttpElectricityMeter();
    httpElectricityMeter.url = rawMeter.url;
    httpElectricityMeter.username = rawMeter.username;
    httpElectricityMeter.password = rawMeter.password;
    httpElectricityMeter.contentType = rawMeter.contentType;
    httpElectricityMeter.data = rawMeter.data;
    httpElectricityMeter.powerValueExtractionRegex = rawMeter.powerValueExtractionRegex;
    httpElectricityMeter.factorToWatt = rawMeter.factorToWatt;
    httpElectricityMeter.pollInterval = rawMeter.pollInterval;
    httpElectricityMeter.measurementInterval = rawMeter.measurementInterval;
    return httpElectricityMeter;
  }
}
