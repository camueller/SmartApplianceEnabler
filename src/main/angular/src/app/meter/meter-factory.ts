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

export class MeterFactory {

  static defaultsFromJSON(rawMeterDefaults: any): MeterDefaults {
    console.log('MeterDefaults (JSON): ' + JSON.stringify(rawMeterDefaults));
    const meterDefaults = new MeterDefaults();
    meterDefaults.s0ElectricityMeter_measurementInterval
      = rawMeterDefaults.s0ElectricityMeter.measurementInterval;
    meterDefaults.httpElectricityMeter_factorToWatt = rawMeterDefaults.httpElectricityMeter.factorToWatt;
    meterDefaults.httpElectricityMeter_measurementInterval = rawMeterDefaults.httpElectricityMeter.measurementInterval;
    meterDefaults.httpElectricityMeter_pollInterval = rawMeterDefaults.httpElectricityMeter.pollInterval;
    meterDefaults.modbusElectricityMeter_pollInterval = rawMeterDefaults.modbusElectricityMeter.pollInterval;
    console.log('MeterDefaults (TYPE): ' + JSON.stringify(meterDefaults));
    return meterDefaults;
  }

  static createEmptyMeter(): Meter {
    return new Meter();
  }

  static fromJSON(rawMeter: any): Meter {
    console.log('Meter (JSON): ' + JSON.stringify(rawMeter));
    const meter = new Meter();
    meter.type = rawMeter['@class'];
    if (meter.type === S0ElectricityMeter.TYPE) {
      meter.s0ElectricityMeter = MeterFactory.createS0ElectricityMeter(rawMeter, false);
    } else if (meter.type === S0ElectricityMeter.TYPE_NETWORKED) {
      meter.s0ElectricityMeterNetworked = MeterFactory.createS0ElectricityMeter(rawMeter, true);
    } else if (meter.type === ModbusElectricityMeter.TYPE) {
      meter.modbusElectricityMeter = MeterFactory.createModbusElectricityMeter(rawMeter);
    } else if (meter.type === HttpElectricityMeter.TYPE) {
      meter.httpElectricityMeter = MeterFactory.createHttpElectricityMeter(rawMeter);
    }
    console.log('Meter (TYPE): ' + JSON.stringify(meter));
    return meter;
  }

  static toJSON(meter: Meter): string {
    console.log('Meter (TYPE): ' + JSON.stringify(meter));
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
      meterRaw = JSON.stringify(meterUsed);
    }
    console.log('Meter (JSON): ' + meterRaw);
    return meterRaw;
  }

  static createS0ElectricityMeter(rawMeter: any, networked: boolean): S0ElectricityMeter {
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

  static createModbusElectricityMeter(rawMeter: any): ModbusElectricityMeter {
    const modbusElectricityMeter = new ModbusElectricityMeter();
    modbusElectricityMeter.slaveAddress = rawMeter.slaveAddress;
    modbusElectricityMeter.registerAddress = rawMeter.registerAddress;
    modbusElectricityMeter.pollInterval = rawMeter.pollInterval;
    modbusElectricityMeter.measurementInterval = rawMeter.measurementInterval;
    return modbusElectricityMeter;
  }

  static createHttpElectricityMeter(rawMeter: any): HttpElectricityMeter {
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
