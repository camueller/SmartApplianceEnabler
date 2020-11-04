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

import {RouterModule, Routes} from '@angular/router';
import {ApplianceComponent} from './appliance/appliance.component';
import {NgModule} from '@angular/core';
import {PageNotFoundComponent} from './not-found.component';
import {MeterComponent} from './meter/meter.component';
import {ControlComponent} from './control/control.component';
import {SettingsComponent} from './settings/settings.component';
import {ApplianceResolver} from './appliance/appliance-resolver.service';
import {StatusComponent} from './status/status.component';
import {ControlResolver} from './control/control-resolver.service';
import {MeterResolver} from './meter/meter-resolver.service';
import {MeterDefaultsResolver} from './meter/meter-defaults-resolver.service';
import {ControlDefaultsResolver} from './control/control-defaults-resolver.service';
import {ScheduleResolver} from './schedule/schedule-resolver.service';
import {SettingsResolver} from './settings/settings-resolver.service';
import {SettingsDefaultsResolver} from './settings/settings-defaults-resolver.service';
import {CanDeactivateGuard} from './shared/can-deactivate-guard.service';
import {SchedulesComponent} from './schedule/schedules/schedules.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'status',
    pathMatch: 'full'
  },
  {
    path: 'appliance/:id',
    component: ApplianceComponent,
    resolve: {
      appliance: ApplianceResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'appliance',
    component: ApplianceComponent
  },
  {
    /* FIXME: Resolver verwenden */
    path: 'status',
    component: StatusComponent
  },
  {
    path: 'meter/:id',
    component: MeterComponent,
    resolve: {
      meter: MeterResolver,
      meterDefaults: MeterDefaultsResolver,
      settings: SettingsResolver,
      settingsDefaults: SettingsDefaultsResolver,
      appliance: ApplianceResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'control/:id',
    component: ControlComponent,
    resolve: {
      control: ControlResolver,
      controlDefaults: ControlDefaultsResolver,
      meterDefaults: MeterDefaultsResolver,
      appliance: ApplianceResolver,
      settings: SettingsResolver,
      settingsDefaults: SettingsDefaultsResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'schedule/:id',
    component: SchedulesComponent,
    resolve: {
      schedules: ScheduleResolver,
      control: ControlResolver,
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'settings',
    component: SettingsComponent,
    resolve: {
      settings: SettingsResolver,
      settingsDefaults: SettingsDefaultsResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: '**',
    component: PageNotFoundComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule {
}
