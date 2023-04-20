import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {HttpWriteValueModel} from '../write-value/http-write-value.model';

export interface HttpWriteModel {
  url: FormControl<string>;
  httpWriteValues: FormArray<FormGroup<HttpWriteValueModel>>;
}
