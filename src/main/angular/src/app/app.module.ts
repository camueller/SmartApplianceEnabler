/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';

import {AppComponent} from './app.component';
import {ApplianceService} from './appliance/appliance.service';
import {ApplianceComponent} from './appliance/appliance.component';
import {AppRoutingModule} from './app-routing.module';
import {PageNotFoundComponent} from './not-found.component';
import {SettingsComponent} from './settings/settings.component';
import {ApplianceResolver} from './appliance/appliance-resolver.service';
import {AppliancesReloadService} from './appliance/appliances-reload-service';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {SettingsService} from './settings/settings-service';
import {SettingsResolver} from './settings/settings-resolver.service';
import {SettingsDefaultsResolver} from './settings/settings-defaults-resolver.service';
import {DialogService} from './shared/dialog.service';
import {CanDeactivateGuard} from './shared/can-deactivate-guard.service';
import {ErrorInterceptor} from './shared/http-error-interceptor';
import {Logger, Options} from './log/logger';
import {Level} from './log/level';
import {MaterialModule} from './material/material.module';
import {LayoutComponent} from './layout/layout.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HeaderComponent} from './navigation/header/header.component';
import {SidenavComponent} from './navigation/sidenav/sidenav.component';
import {ControlModule} from './control/control.module';
import {MeterModule} from './meter/meter.module';
import {ScheduleModule} from './schedule/schedule.module';
import {StatusModule} from './status/status.module';
import {HttpLoaderFactory} from './shared/http-loader-factory';
import {SettingsModbusComponent} from './settings/modbus/settings-modbus.component';
import {NotificationModule} from './notification/notification.module';
import {VersionService} from './shared/version-service';
import {ApplianceIdsResolver} from './appliance/appliance-ids-resolver';
import {LanguageService} from './shared/language-service';

@NgModule({
  declarations: [
    AppComponent,
    ApplianceComponent,
    HeaderComponent,
    LayoutComponent,
    PageNotFoundComponent,
    SettingsComponent,
    SettingsModbusComponent,
    SidenavComponent,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    ControlModule,
    FlexLayoutModule,
    HttpClientModule,
    MaterialModule,
    MeterModule,
    NotificationModule,
    ReactiveFormsModule,
    ScheduleModule,
    StatusModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    })
  ],
  providers: [
    ApplianceService,
    AppliancesReloadService,
    ApplianceResolver,
    ApplianceIdsResolver,
    CanDeactivateGuard,
    DialogService,
    LanguageService,
    Logger,
    {provide: Options, useValue: {level: Level.DEBUG}},
    SettingsService,
    SettingsResolver,
    SettingsDefaultsResolver,
    VersionService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
