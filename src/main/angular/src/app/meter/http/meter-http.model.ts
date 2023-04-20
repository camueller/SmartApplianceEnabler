import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {HttpReadModel} from '../../http/read/http-read.model';

export interface MeterHttpModel {
  pollInterval: FormControl<number>;
  contentProtocol: FormControl<string>;
  httpReads: FormArray<FormGroup<HttpReadModel>>;
}
