import {FormControl} from '@angular/forms';

export interface HttpConfigurationModel {
  contentType: FormControl<string>;
  username: FormControl<string>;
  password: FormControl<string>;

}
