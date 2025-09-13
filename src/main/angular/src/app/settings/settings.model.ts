import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {SettingsModbusModel} from './modbus/settings-modbus.model';

export interface SettingsModel {
  mqttHost: FormControl<string>;
  mqttPort: FormControl<number>;
  nodeRedDashboardUrl: FormControl<string>;
  mqttUsername: FormControl<string>;
  mqttPassword: FormControl<string>;
  mqttRootTopic: FormControl<string>;
  holidaysEnabled: FormControl<boolean>;
  holidaysUrl: FormControl<string>;
  modbusSettings: FormArray<FormGroup<SettingsModbusModel>>;
}
