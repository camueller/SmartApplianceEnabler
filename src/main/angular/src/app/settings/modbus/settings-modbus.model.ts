import {FormControl} from '@angular/forms';

export interface SettingsModbusModel {
  modbusTcpId: FormControl<string>;
  modbusTcpHost: FormControl<string>;
  modbusTcpPort: FormControl<number>;
}
