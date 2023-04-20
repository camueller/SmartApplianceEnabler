import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {HttpReadValueModel} from '../read-value/http-read-value.model';

export interface HttpReadModel {
  url: FormControl<string>;
  httpReadValues: FormArray<FormGroup<HttpReadValueModel>>;
}
