import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModbusWriteValueComponent} from './write-value/modbus-write-value.component';
import {ModbusWriteComponent} from './write/modbus-write.component';
import {ModbusReadValueComponent} from './read-value/modbus-read-value.component';
import {ModbusReadComponent} from './read/modbus-read.component';
import {MaterialModule} from '../material/material.module';
import {ReactiveFormsModule} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {HttpLoaderFactory} from '../shared/http-loader-factory';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
    ModbusReadComponent,
    ModbusReadValueComponent,
    ModbusWriteComponent,
    ModbusWriteValueComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    ReactiveFormsModule,
    SharedModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    })
  ],
  exports: [
    ModbusReadComponent,
    ModbusWriteComponent,
  ]
})
export class ModbusModule { }
