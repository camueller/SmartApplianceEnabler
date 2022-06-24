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
import {ControlDefaults} from './control-defaults';
import {MockSwitch} from './mock-switch';
import {Logger} from '../log/logger';
import {Switch} from './switch/switch';
import {HttpSwitch} from './http/http-switch';
import {EvModbusControl} from './evcharger/modbus/ev-modbus-control';
import {StartingCurrentSwitch} from './startingcurrent/starting-current-switch';
import {AlwaysOnSwitch} from './alwayson/always-on-switch';
import {ModbusSwitch} from './modbus/modbus-switch';
import {EvHttpControl} from './evcharger/http/ev-http-control';
import {EvCharger} from './evcharger/ev-charger';
import {ElectricVehicle} from './evcharger/electric-vehicle/electric-vehicle';
import {MeterReportingSwitch} from './meterreporting/meter-reporting-switch';
import {PwmSwitch} from './pwm/pwm-switch';
import {LevelSwitch} from './level/level-switch';

export class ControlFactory {

  constructor(private logger: Logger) {
  }

  defaultsFromJSON(rawControlDefaults: any): ControlDefaults {
    this.logger.debug('ControlDefaults (JSON): ' + JSON.stringify(rawControlDefaults));
    const controlDefaults = new ControlDefaults(rawControlDefaults);
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

  fromJSONbyType(control: Control, rawControl: any) {
    if (rawControl != null) {
      this.initializeByType(control, rawControl, rawControl['@class']);
    }
  }

  initializeByType(control: Control, rawControl: any, type: string) {
    control.type = type;
    control.notifications = rawControl.notifications;
    if (control.type === MeterReportingSwitch.TYPE) {
      control.meterReportingSwitch = this.createMeterReportingSwitch(rawControl);
    } else if (control.type === AlwaysOnSwitch.TYPE) {
      control.alwaysOnSwitch = this.createAlwaysOnSwitch(rawControl);
    } else if (control.type === MockSwitch.TYPE) {
      control.mockSwitch = this.createMockSwitch(rawControl);
    } else if (control.type === Switch.TYPE) {
      control.switch_ = this.createSwitch(rawControl);
    } else if (control.type === ModbusSwitch.TYPE) {
      control.modbusSwitch = this.createModbusSwitch(rawControl);
    } else if (control.type === LevelSwitch.TYPE) {
      control.levelSwitch = this.createLevelSwitch(rawControl);
    } else if (control.type === PwmSwitch.TYPE) {
      control.pwmSwitch = this.createPwmSwitch(rawControl);
    } else if (control.type === EvCharger.TYPE) {
      control.evCharger = this.createEvCharger(rawControl);
    } else if (control.type === HttpSwitch.TYPE) {
      control.httpSwitch = this.createHttpSwitch(rawControl);
    }
  }

  getControlByType(control: Control): any {
    if (control.type === MeterReportingSwitch.TYPE) {
      return control.meterReportingSwitch;
    } else if (control.type === AlwaysOnSwitch.TYPE) {
      return control.alwaysOnSwitch;
    } else if (control.type === MockSwitch.TYPE) {
      return control.mockSwitch;
    } else if (control.type === Switch.TYPE) {
      return control.switch_;
    } else if (control.type === ModbusSwitch.TYPE) {
      return control.modbusSwitch;
    } else if (control.type === LevelSwitch.TYPE) {
      return control.levelSwitch;
    } else if (control.type === PwmSwitch.TYPE) {
      return control.pwmSwitch;
    } else if (control.type === EvCharger.TYPE) {
      return control.evCharger;
    } else if (control.type === HttpSwitch.TYPE) {
      return control.httpSwitch;
    }
    return null;
  }

  createMeterReportingSwitch(rawMeterReportingSwitch?: any): MeterReportingSwitch {
    return rawMeterReportingSwitch;
  }

  createAlwaysOnSwitch(rawAlwaysOnSwitch?: any): AlwaysOnSwitch {
    return new AlwaysOnSwitch(rawAlwaysOnSwitch);
  }

  createMockSwitch(rawMockSwitch?: any): MockSwitch {
    return new MockSwitch(rawMockSwitch);
  }

  createStartingCurrentSwitch(rawStartingCurrentSwitch: any): StartingCurrentSwitch {
    return new StartingCurrentSwitch(rawStartingCurrentSwitch);
  }

  createSwitch(rawSwitch: any): Switch {
    return new Switch(rawSwitch);
  }

  createModbusSwitch(rawModbusSwitch: any): ModbusSwitch {
    return new ModbusSwitch(rawModbusSwitch);
  }

  createHttpSwitch(rawHttpSwitch: any): HttpSwitch {
    return new HttpSwitch(rawHttpSwitch);
  }

  createLevelSwitch(rawLevelSwitch: any): LevelSwitch {
    return new LevelSwitch(rawLevelSwitch);
  }

  createPwmSwitch(rawPwmSwitch: any): PwmSwitch {
    return new PwmSwitch(rawPwmSwitch);
  }

  createEvCharger(rawEvCharger: any): EvCharger {
    let evCharger = new EvCharger();
    if (rawEvCharger) {
      const evs: ElectricVehicle[] = [];
      if (rawEvCharger.vehicles) {
        (rawEvCharger.vehicles as any[]).map(rawEv => {
          const ev: ElectricVehicle = {...rawEv};
          evs.push(ev);
        });
      }

      evCharger = new EvCharger({
        ...rawEvCharger,
        vehicles: evs
      });

      if (rawEvCharger.control['@class'] === EvModbusControl.TYPE) {
        evCharger.modbusControl = this.createEvModbusControl(rawEvCharger.control);
      } else if (rawEvCharger.control['@class'] === EvHttpControl.TYPE) {
        evCharger.httpControl = this.createEvHttpControl(rawEvCharger.control);
      }
    }
    return evCharger;
  }

  createEvModbusControl(rawModbusControl: any): EvModbusControl {
    return rawModbusControl;
  }

  createEvHttpControl(rawHttpControl: any): EvHttpControl {
    return rawHttpControl;
  }

  toJSON(control: Control): string {
    this.logger.debug('Control (TYPE): ' + JSON.stringify(control));
    let controlUsed: any;
    if (control.startingCurrentDetection) {
      control.startingCurrentSwitch['control'] = this.getControlByType(control);
      control.startingCurrentSwitch['control'].notifications = control.notifications;
      controlUsed = control.startingCurrentSwitch;
    } else {
      controlUsed = this.getControlByType(control);
    }
    let rawControl: string;
    if (controlUsed) {
      controlUsed.notifications = control.notifications;
      if (control.type === EvCharger.TYPE) {
        this.toJSONEvCharger(control);
      }
      rawControl = JSON.stringify(controlUsed);
    }
    this.logger.debug('Control (JSON): ' + rawControl);
    return rawControl;
  }

  toJSONEvCharger(control: Control) {
    const evCharger = control.evCharger;
    if (evCharger.httpControl) {
      evCharger['control'] = evCharger.httpControl;
    }
    if (evCharger.modbusControl) {
      evCharger['control'] = evCharger.modbusControl;
    }
  }

  toElectricVehicle(rawEv: any): ElectricVehicle {
    return rawEv;
  }
}
