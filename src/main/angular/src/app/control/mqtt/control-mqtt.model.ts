import {FormControl} from '@angular/forms';

export interface ControlMqttModel {
  topic: FormControl<string>;
  onPayload: FormControl<string>;
  offPayload: FormControl<string>;
  statusTopic: FormControl<string>;
  statusExtractionRegex: FormControl<string>;
}
