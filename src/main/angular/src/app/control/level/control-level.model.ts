import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ControlSwitchModel} from '../switch/control-switch.model';
import {ControlHttpModel} from '../http/control-http.model';
import {ControlModbusModel} from '../modbus/control-modbus.model';
import {ControlMqttModel} from '../mqtt/control-mqtt.model';

export type ControlLevelSupportedTypes = ControlSwitchModel | ControlHttpModel | ControlModbusModel | ControlMqttModel;

export interface ControlLevelModel {
  realControlType: FormControl<string>;
  controls: FormArray<FormGroup<ControlLevelSupportedTypes>>;
  powerLevels: FormArray<FormGroup<PowerLevelModel>>;
}

export interface PowerLevelModel {
  power: FormControl<number>;
  switchStatuses: FormArray<FormGroup<SwitchStatusModel>>;
}

export interface SwitchStatusModel {
  idref: FormControl<string>;
  on: FormControl<boolean>;
}
