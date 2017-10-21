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

import {Component, OnInit} from '@angular/core';
import {ApplianceService} from './shared/appliance.service';
import {Appliance} from './shared/appliance';
import {AppliancesReloadService} from './shared/appliances-reload-service';

declare const $: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  appliances: Appliance[];

  constructor(private applianceService: ApplianceService, private appliancesReloadService: AppliancesReloadService) {}

  toggleSidebar() {
    $('.ui.sidebar').sidebar({dimPage: false, closable: false});
    $('.ui.sidebar').sidebar('toggle');
  }

  ngOnInit()  {
    this.loadAppliances();
    this.appliancesReloadService.triggered.subscribe(() => {
      this.loadAppliances();
    });
  }

  loadAppliances() {
    this.applianceService.getAppliances().subscribe(appliances => this.appliances = appliances);
  }
}
