import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {HttpReadModel} from '../../http/read/http-read.model';
import {HttpWriteModel} from '../../http/write/http-write.model';

export interface ControlHttpModel {
  httpWrites: FormArray<FormGroup<HttpWriteModel>>;
  readControlState: FormControl<boolean>;
  httpRead: FormGroup<HttpReadModel>;
}
