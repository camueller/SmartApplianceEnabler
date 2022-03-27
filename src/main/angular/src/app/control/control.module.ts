import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ControlComponent} from './control.component';
import {ControlSwitchComponent} from './switch/control-switch.component';
import {MaterialModule} from '../material/material.module';
import {ReactiveFormsModule} from '@angular/forms';
import {ControlHttpComponent} from './http/control-http.component';
import {ControlModbusComponent} from './modbus/control-modbus.component';
import {ControlEvchargerModbusComponent} from './evcharger/modbus/control-evcharger-modbus.component';
import {ControlEvchargerComponent} from './evcharger/control-evcharger.component';
import {ControlEvchargerHttpComponent} from './evcharger/http/control-evcharger-http.component';
import {ControlStartingcurrentComponent} from './startingcurrent/control-startingcurrent.component';
import {ElectricVehicleResolver} from './evcharger/electric-vehicle-resolver.service';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ElectricVehicleComponent} from './evcharger/electric-vehicle/electric-vehicle.component';
import {HttpModule} from '../http/http.module';
import {ModbusModule} from '../modbus/modbus.module';
import {ControlService} from './control-service';
import {ControlResolver} from './control-resolver.service';
import {ControlDefaultsResolver} from './control-defaults-resolver.service';
import {HttpLoaderFactory} from '../shared/http-loader-factory';
import {NotificationModule} from '../notification/notification.module';
import { ControlMeterreportingComponent } from './meterreporting/control-meterreporting.component';
import {ControlPwmComponent} from './pwm/control-pwm.component';

@NgModule({
  declarations: [
    ControlComponent,
    ControlEvchargerComponent,
    ControlEvchargerHttpComponent,
    ControlEvchargerModbusComponent,
    ControlHttpComponent,
    ControlMeterreportingComponent,
    ControlModbusComponent,
    ControlPwmComponent,
    ControlStartingcurrentComponent,
    ControlSwitchComponent,
    ElectricVehicleComponent,
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
    }),
  ],
  providers: [
    ControlService,
    ControlResolver,
    ControlDefaultsResolver,
    ElectricVehicleResolver,
  ]
})
export class ControlModule { }
