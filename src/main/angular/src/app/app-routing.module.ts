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
import {ApplianceDetailsComponent} from './appliance-details/appliance-details.component';
import {NgModule} from '@angular/core';
import {PageNotFoundComponent} from './not-found.component';
import {ApplianceMeterComponent} from './appliance-meter/appliance-meter.component';
import {ApplianceControlComponent} from './appliance-control/appliance-control.component';
import {SchedulesComponent} from './schedule/schedule.component';
import {SettingsComponent} from './settings/settings.component';
import {ApplianceResolver} from './shared/appliance-resolver.service';

const routes: Routes = [
  {
    path: 'appliance/:id',
    component: ApplianceDetailsComponent,
    resolve: {
      appliance: ApplianceResolver
    }
  },
  {
    path: 'appliance',
    component: ApplianceDetailsComponent
  },
  {
    path: 'meter/:id',
    component: ApplianceMeterComponent
  },
  {
    path: 'control/:id',
    component: ApplianceControlComponent
  },
  {
    path: 'schedule/:id',
    component: SchedulesComponent
  },
  {
    path: 'settings',
    component: SettingsComponent
  },
  { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule {
}
