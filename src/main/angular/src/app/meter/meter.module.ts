import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MeterComponent} from './meter.component';
import {MeterModbusComponent} from './modbus/meter-modbus.component';
import {MeterHttpComponent} from './http/meter-http.component';
import {MeterS0Component} from './s0/meter-s0.component';
import {MaterialModule} from '../material/material.module';
import {HttpModule} from '../http/http.module';
import {ModbusModule} from '../modbus/modbus.module';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MeterService} from './meter-service';
import {MeterResolver} from './meter-resolver.service';
import {MeterDefaultsResolver} from './meter-defaults-resolver.service';
import { HttpLoaderFactory } from '../shared/http-loader-factory';
import {NotificationModule} from '../notification/notification.module';
import {MeterMasterComponent} from './master/meter-master.component';
import {MeterSlaveComponent} from './slave/meter-slave.component';

@NgModule({
  declarations: [
    MeterComponent,
    MeterMasterComponent,
    MeterModbusComponent,
    MeterHttpComponent,
    MeterS0Component,
    MeterSlaveComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    HttpModule,
    HttpClientModule,
    ModbusModule,
    NotificationModule,
    ReactiveFormsModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    })
  ],
  providers: [
    MeterService,
    MeterResolver,
    MeterDefaultsResolver,
  ]
})
export class MeterModule { }
