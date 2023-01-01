import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MaterialModule} from '../material/material.module';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ReactiveFormsModule} from '@angular/forms';
import {SchedulesComponent} from './schedules/schedules.component';
import {ScheduleComponent} from './schedule.component';
import {ScheduleTimeframeDayComponent} from './timeframe/day/schedule-timeframe-day.component';
import {ScheduleRequestEnergyComponent} from './request/energy/schedule-request-energy.component';
import {ScheduleRequestRuntimeComponent} from './request/runtime/schedule-request-runtime.component';
import {ScheduleRequestSocComponent} from './request/soc/schedule-request-soc.component';
import {ScheduleTimeframeConsecutivedaysComponent} from './timeframe/consecutivedays/schedule-timeframe-consecutivedays.component';
import {ScheduleService} from './schedule-service';
import {ScheduleResolver} from './schedule-resolver.service';
import {HttpLoaderFactory} from '../shared/http-loader-factory';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
    ScheduleComponent,
    SchedulesComponent,
    ScheduleRequestRuntimeComponent,
    ScheduleRequestEnergyComponent,
    ScheduleRequestSocComponent,
    ScheduleTimeframeConsecutivedaysComponent,
    ScheduleTimeframeDayComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    HttpClientModule,
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
  providers: [
    ScheduleService,
    ScheduleResolver,
  ]
})
export class ScheduleModule { }
