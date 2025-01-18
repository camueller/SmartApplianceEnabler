import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MeterComponent} from './meter.component';
import {MeterModbusComponent} from './modbus/meter-modbus.component';
import {MeterHttpComponent} from './http/meter-http.component';
import {MeterS0Component} from './s0/meter-s0.component';
import {MaterialModule} from '../material/material.module';
import {HttpModule} from '../http/http.module';
import {ModbusModule} from '../modbus/modbus.module';
import { HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MeterService} from './meter-service';
import {MeterResolver} from './meter-resolver.service';
import {MeterDefaultsResolver} from './meter-defaults-resolver.service';
import { HttpLoaderFactory } from '../shared/http-loader-factory';
import {NotificationModule} from '../notification/notification.module';
import {MeterMasterComponent} from './master/meter-master.component';
import {MeterSlaveComponent} from './slave/meter-slave.component';
import {MeterMqttComponent} from './mqtt/meter-mqtt.component';
import {SharedModule} from '../shared/shared.module';

@NgModule({ declarations: [
        MeterComponent,
        MeterMasterComponent,
        MeterModbusComponent,
        MeterMqttComponent,
        MeterHttpComponent,
        MeterS0Component,
        MeterSlaveComponent,
    ], imports: [CommonModule,
        MaterialModule,
        HttpModule,
        ModbusModule,
        NotificationModule,
        ReactiveFormsModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        SharedModule], providers: [
        MeterService,
        MeterResolver,
        MeterDefaultsResolver,
        provideHttpClient(withInterceptorsFromDi()),
    ] })
export class MeterModule { }
