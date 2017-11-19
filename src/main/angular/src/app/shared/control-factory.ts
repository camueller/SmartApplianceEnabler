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

export class ControlFactory {

  static createEmptyControl(): Control {
    return new Control();
  }

  static fromJSON(rawControl: any): Control {
    console.log('Control (JSON): ' + JSON.stringify(rawControl));
    const control = new Control();
    if (rawControl['@class'] === StartingCurrentSwitch.TYPE) {
      control.startingCurrentDetection = true;
      control.startingCurrentSwitch = ControlFactory.createStartingCurrentSwitch(rawControl);
      ControlFactory.fromJSONbyType(control, rawControl.controls[0]);
    } else {
      ControlFactory.fromJSONbyType(control, rawControl);
    }
    console.log('Control (TYPE): ' + JSON.stringify(control));
    return control;
  }

  static toJSON(control: Control): string {
    console.log('Control (TYPE): ' + JSON.stringify(control));
    let rawControl: string;
    if (control.startingCurrentSwitch != null) {
      control.startingCurrentSwitch['controls'] = [ this.getControlByType(control) ];
      rawControl = JSON.stringify(control.startingCurrentSwitch);
    } else {
      rawControl = JSON.stringify(this.getControlByType(control));
    }
    console.log('Control (JSON): ' + JSON.stringify(rawControl));
    return rawControl;
  }

  static fromJSONbyType(control: Control, rawControl: any) {
    control.type = rawControl['@class'];
    if (control.type === Switch.TYPE) {
      control.alwaysOnSwitch = ControlFactory.createAlwaysOnSwitch(rawControl);
    } else if (control.type === Switch.TYPE) {
      control.switch_ = ControlFactory.createSwitch(rawControl);
    } else if (control.type === ModbusSwitch.TYPE) {
      control.modbusSwitch = ControlFactory.createModbusSwitch(rawControl);
    } else if (control.type === HttpSwitch.TYPE) {
      control.httpSwitch = ControlFactory.createHttpSwitch(rawControl);
    }
  }

  static getControlByType(control: Control): any {
    if (control.type === AlwaysOnSwitch.TYPE) {
      return control.alwaysOnSwitch;
    } else if (control.type === Switch.TYPE) {
      return control.switch_;
    } else if (control.type === ModbusSwitch.TYPE) {
      return control.modbusSwitch;
    } else if (control.type === HttpSwitch.TYPE) {
      return control.httpSwitch;
    }
    return null;
  }

  static createAlwaysOnSwitch(rawAlwaysOnSwitch: any): AlwaysOnSwitch {
    return new AlwaysOnSwitch();
  }

  static createStartingCurrentSwitch(rawStartingCurrentSwitch: any): StartingCurrentSwitch {
    const startingCurrentSwitch = new StartingCurrentSwitch();
    startingCurrentSwitch.powerThreshold = rawStartingCurrentSwitch.powerThreshold;
    startingCurrentSwitch.startingCurrentDetectionDuration = rawStartingCurrentSwitch.startingCurrentDetectionDuration;
    startingCurrentSwitch.finishedCurrentDetectionDuration = rawStartingCurrentSwitch.finishedCurrentDetectionDuration;
    startingCurrentSwitch.minRunningTime = rawStartingCurrentSwitch.minRunningTime;
    return startingCurrentSwitch;
  }

  static createSwitch(rawSwitch: any): Switch {
    const switch_ = new Switch();
    switch_.gpio = rawSwitch.gpio;
    switch_.reverseStates = rawSwitch.reverseStates;
    return switch_;
  }

  static createModbusSwitch(rawModbusSwitch: any): ModbusSwitch {
    const modbusSwitch = new ModbusSwitch();
    modbusSwitch.slaveAddress = rawModbusSwitch.slaveAddress;
    modbusSwitch.registerAddress = rawModbusSwitch.registerAddress;
    return modbusSwitch;
  }

  static createHttpSwitch(rawHttpSwitch: any): HttpSwitch {
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
