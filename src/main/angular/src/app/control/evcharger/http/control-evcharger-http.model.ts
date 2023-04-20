import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {HttpConfigurationModel} from '../../../http/configuration/http-configuration.model';
import {HttpReadModel} from '../../../http/read/http-read.model';
import {HttpWriteModel} from '../../../http/write/http-write.model';

export interface ControlEvchargerHttpModel {
  httpConfiguration: FormGroup<HttpConfigurationModel>;
  contentProtocol: FormControl<string>;
  httpReads: FormArray<FormGroup<HttpReadModel>>;
  httpWrites: FormArray<FormGroup<HttpWriteModel>>;
}
