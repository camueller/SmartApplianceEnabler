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
import {MeterDefaults} from './meter-defaults';
import {Logger} from '../log/logger';
import {S0ElectricityMeter} from './s0/s0-electricity-meter';
import {ModbusElectricityMeter} from './modbus/modbus-electricity-meter';
import {HttpElectricityMeter} from './http/http-electricity-meter';
import {MasterElectricityMeter} from './master/master-electricity-meter';
import {SlaveElectricityMeter} from './slave/master-electricity-meter';

export class MeterFactory {

  constructor(private logger: Logger) {
  }

  defaultsFromJSON(rawMeterDefaults: any): MeterDefaults {
    this.logger.debug('MeterDefaults (JSON): ' + JSON.stringify(rawMeterDefaults));
    const meterDefaults = new MeterDefaults(rawMeterDefaults);
    this.logger.debug('MeterDefaults (TYPE): ' + JSON.stringify(meterDefaults));
    return meterDefaults;
  }

  createEmptyMeter(): Meter {
    return new Meter();
  }

  fromJSON(rawMeter: any): Meter {
    this.logger.debug('Meter (JSON): ', rawMeter);
    const meter = new Meter();
    if (rawMeter['@class'] === MasterElectricityMeter.TYPE) {
      meter.isMasterMeter = true;
      meter.masterElectricityMeter = this.createMasterElectricityMeter(rawMeter);
      this.fromJSONbyType(meter, rawMeter.meter);
    } else {
      this.fromJSONbyType(meter, rawMeter);
    }
    meter.notifications = rawMeter.notifications;
    this.logger.debug('Meter (TYPE): ', meter);
    return meter;
  }

  fromJSONbyType(meter: Meter, rawMeter: any) {
    if (rawMeter != null) {
      this.initializeByType(meter, rawMeter, rawMeter['@class']);
    }
  }

  initializeByType(meter: Meter, rawMeter: any, type: string) {
    meter.type = type;
    meter.notifications = rawMeter.notifications;
    if (meter.type === S0ElectricityMeter.TYPE) {
      meter.s0ElectricityMeter = this.createS0ElectricityMeter(rawMeter);
    } else if (meter.type === ModbusElectricityMeter.TYPE) {
      meter.modbusElectricityMeter = this.createModbusElectricityMeter(rawMeter);
    } else if (meter.type === HttpElectricityMeter.TYPE) {
      meter.httpElectricityMeter = this.createHttpElectricityMeter(rawMeter);
    } else if (meter.type === SlaveElectricityMeter.TYPE) {
      meter.slaveElectricityMeter = this.createSlaveElectricityMeter(rawMeter);
    }
  }

  getMeterByType(meter: Meter): any {
    if (meter.type === S0ElectricityMeter.TYPE) {
      return meter.s0ElectricityMeter;
    } else if (meter.type === ModbusElectricityMeter.TYPE) {
      return meter.modbusElectricityMeter;
    } else if (meter.type === HttpElectricityMeter.TYPE) {
      return meter.httpElectricityMeter;
    } else if (meter.type === SlaveElectricityMeter.TYPE) {
      return meter.slaveElectricityMeter;
    }
    return null;
  }

  toJSON(meter: Meter): string {
    this.logger.debug('Meter (TYPE): ' + JSON.stringify(meter));
    let meterUsed: any;
    if (meter.isMasterMeter) {
      meter.masterElectricityMeter['meter'] = this.getMeterByType(meter);
      meter.masterElectricityMeter['meter'].notifications = meter.notifications;
      meterUsed = meter.masterElectricityMeter;
    } else {
      meterUsed = this.getMeterByType(meter);
    }
    let meterRaw: string;
    if (meterUsed != null) {
      meterRaw = JSON.stringify(meterUsed);
    }
    this.logger.debug('Meter (JSON): ' + meterRaw);
    return meterRaw;
  }

  createS0ElectricityMeter(rawMeter: any): S0ElectricityMeter {
    return rawMeter;
  }

  createModbusElectricityMeter(rawMeter: any): ModbusElectricityMeter {
    return rawMeter;
  }

  createHttpElectricityMeter(rawMeter: any): HttpElectricityMeter {
    return rawMeter;
  }

  createMasterElectricityMeter(rawMeter: any): MasterElectricityMeter {
    return rawMeter;
  }

  createSlaveElectricityMeter(rawMeter: any): SlaveElectricityMeter {
    return rawMeter;
  }
}
