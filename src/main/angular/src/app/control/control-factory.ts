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

import {Control} from './control';
import {StartingCurrentSwitch} from './starting-current-switch';
import {Switch} from './switch';
import {ModbusSwitch} from './modbus-switch';
import {HttpSwitch} from './http-switch';
import {AlwaysOnSwitch} from './always-on-switch';
import {ControlDefaults} from './control-defaults';
import {MockSwitch} from './mock-switch';
import {Logger} from '../log/logger';

export class ControlFactory {

  constructor(private logger: Logger) {
  }

  defaultsFromJSON(rawControlDefaults: any): ControlDefaults {
    this.logger.debug('ControlDefaults (JSON): ' + JSON.stringify(rawControlDefaults));
    const controlDefaults = new ControlDefaults();
    controlDefaults.startingCurrentSwitchDefaults_powerThreshold
      = rawControlDefaults.startingCurrentSwitchDefaults.powerThreshold;
    controlDefaults.startingCurrentSwitchDefaults_startingCurrentDetectionDuration
      = rawControlDefaults.startingCurrentSwitchDefaults.startingCurrentDetectionDuration;
    controlDefaults.startingCurrentSwitchDefaults_finishedCurrentDetectionDuration
      = rawControlDefaults.startingCurrentSwitchDefaults.finishedCurrentDetectionDuration;
    controlDefaults.startingCurrentSwitchDefaults_minRunningTime
      = rawControlDefaults.startingCurrentSwitchDefaults.minRunningTime;
    this.logger.debug('ControlDefaults (TYPE): ' + JSON.stringify(controlDefaults));
    return controlDefaults;
  }

  createEmptyControl(): Control {
    return new Control();
  }

  fromJSON(rawControl: any): Control {
    this.logger.debug('Control (JSON): ' + JSON.stringify(rawControl));
    const control = new Control();
    if (rawControl['@class'] === StartingCurrentSwitch.TYPE) {
      control.startingCurrentDetection = true;
      control.startingCurrentSwitch = this.createStartingCurrentSwitch(rawControl);
      this.fromJSONbyType(control, rawControl.control);
    } else {
      this.fromJSONbyType(control, rawControl);
    }
    this.logger.debug('Control (TYPE): ' + JSON.stringify(control));
    return control;
  }

  toJSON(control: Control): string {
    this.logger.debug('Control (TYPE): ' + JSON.stringify(control));
    let controlUsed: any;
    if (control.startingCurrentSwitch != null) {
      control.startingCurrentSwitch['control'] = this.getControlByType(control);
      controlUsed = control.startingCurrentSwitch;
      if (controlUsed.powerThreshold === '') {
        controlUsed.powerThreshold = null;
      }
      if (controlUsed.startingCurrentDetectionDuration === '') {
        controlUsed.startingCurrentDetectionDuration = null;
      }
      if (controlUsed.finishedCurrentDetectionDuration === '') {
        controlUsed.finishedCurrentDetectionDuration = null;
      }
      if (controlUsed.minRunningTime === '') {
        controlUsed.minRunningTime = null;
      }
    } else {
      controlUsed = this.getControlByType(control);
    }
    let rawControl: string;
    if (controlUsed != null) {
      rawControl = JSON.stringify(controlUsed);
    }
    this.logger.debug('Control (JSON): ' + rawControl);
    return rawControl;
  }

  fromJSONbyType(control: Control, rawControl: any) {
    if (rawControl != null) {
      control.type = rawControl['@class'];
      if (control.type === AlwaysOnSwitch.TYPE) {
        control.alwaysOnSwitch = this.createAlwaysOnSwitch(rawControl);
      } else if (control.type === MockSwitch.TYPE) {
        control.mockSwitch = this.createMockSwitch(rawControl);
      } else if (control.type === Switch.TYPE) {
        control.switch_ = this.createSwitch(rawControl);
      } else if (control.type === ModbusSwitch.TYPE) {
        control.modbusSwitch = this.createModbusSwitch(rawControl);
      } else if (control.type === HttpSwitch.TYPE) {
        control.httpSwitch = this.createHttpSwitch(rawControl);
      }
    }
  }

  getControlByType(control: Control): any {
    if (control.type === AlwaysOnSwitch.TYPE) {
      return control.alwaysOnSwitch;
    } else if (control.type === MockSwitch.TYPE) {
      return control.mockSwitch;
    } else if (control.type === Switch.TYPE) {
      return control.switch_;
    } else if (control.type === ModbusSwitch.TYPE) {
      return control.modbusSwitch;
    } else if (control.type === HttpSwitch.TYPE) {
      return control.httpSwitch;
    }
    return null;
  }

  createAlwaysOnSwitch(rawAlwaysOnSwitch: any): AlwaysOnSwitch {
    return new AlwaysOnSwitch();
  }

  createMockSwitch(rawMockSwitch: any): MockSwitch {
    return new MockSwitch();
  }

  createStartingCurrentSwitch(rawStartingCurrentSwitch: any): StartingCurrentSwitch {
    const startingCurrentSwitch = new StartingCurrentSwitch();
    startingCurrentSwitch.powerThreshold = rawStartingCurrentSwitch.powerThreshold;
    startingCurrentSwitch.startingCurrentDetectionDuration = rawStartingCurrentSwitch.startingCurrentDetectionDuration;
    startingCurrentSwitch.finishedCurrentDetectionDuration = rawStartingCurrentSwitch.finishedCurrentDetectionDuration;
    startingCurrentSwitch.minRunningTime = rawStartingCurrentSwitch.minRunningTime;
    return startingCurrentSwitch;
  }

  createSwitch(rawSwitch: any): Switch {
    const switch_ = new Switch();
    switch_.gpio = rawSwitch.gpio;
    switch_.reverseStates = rawSwitch.reverseStates;
    return switch_;
  }

  createModbusSwitch(rawModbusSwitch: any): ModbusSwitch {
    const modbusSwitch = new ModbusSwitch();
    modbusSwitch.slaveAddress = rawModbusSwitch.slaveAddress;
    modbusSwitch.registerAddress = rawModbusSwitch.registerAddress;
    return modbusSwitch;
  }

  createHttpSwitch(rawHttpSwitch: any): HttpSwitch {
    const httpSwitch = new HttpSwitch();
    httpSwitch.onUrl = rawHttpSwitch.onUrl;
    httpSwitch.offUrl = rawHttpSwitch.offUrl;
    httpSwitch.username = rawHttpSwitch.username;
    httpSwitch.password = rawHttpSwitch.password;
    httpSwitch.contentType = rawHttpSwitch.contentType;
    httpSwitch.onData = rawHttpSwitch.onData;
    httpSwitch.offData = rawHttpSwitch.offData;
    return httpSwitch;
  }
}
