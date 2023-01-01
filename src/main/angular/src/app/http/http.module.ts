import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpReadValueComponent} from './read-value/http-read-value.component';
import {HttpReadComponent} from './read/http-read.component';
import {HttpWriteValueComponent} from './write-value/http-write-value.component';
import {HttpConfigurationComponent} from './configuration/http-configuration.component';
import {HttpWriteComponent} from './write/http-write.component';
import {MaterialModule} from '../material/material.module';
import {ReactiveFormsModule} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {HttpLoaderFactory} from '../shared/http-loader-factory';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
    HttpConfigurationComponent,
    HttpReadComponent,
    HttpReadValueComponent,
    HttpWriteComponent,
    HttpWriteValueComponent,
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
    HttpConfigurationComponent,
    HttpReadComponent,
    HttpWriteComponent,
  ]
})
export class HttpModule { }
