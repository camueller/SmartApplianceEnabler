import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MaterialModule} from '../material/material.module';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ReactiveFormsModule} from '@angular/forms';
import {StatusComponent} from './status.component';
import {StatusService} from './status.service';
import {StatusViewComponent} from './view/status-view.component';
import {StatusEditComponent} from './edit/status-edit.component';
import {StatusEvchargerEditComponent} from './evcharger-edit/status-evcharger-edit.component';
import {StatusEvchargerViewComponent} from './evcharger-view/status-evcharger-view.component';
import {TrafficLightComponent} from './traffic-light/traffic-light.component';
import {HttpLoaderFactory} from '../shared/http-loader-factory';

@NgModule({
  declarations: [
    StatusComponent,
    StatusEditComponent,
    StatusEvchargerEditComponent,
    StatusEvchargerViewComponent,
    StatusViewComponent,
    TrafficLightComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    HttpClientModule,
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
    StatusService
  ]
})
export class StatusModule { }
