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
import {ApplianceService} from './appliance/appliance.service';
import {ApplianceHeader} from './appliance/appliance-header';
import {AppliancesReloadService} from './appliance/appliances-reload-service';
import {TranslateService} from '@ngx-translate/core';

declare const $: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  applianceHeaders: ApplianceHeader[];
  typePrefix = 'ApplianceComponent.type.';
  translatedTypes = new Object();

  constructor(private applianceService: ApplianceService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService) {
    translate.setDefaultLang('de');
  }

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
    this.applianceService.getApplianceHeaders().subscribe(applianceHeaders => {
      this.applianceHeaders = applianceHeaders;

      const types = [];
      this.applianceHeaders.forEach(applianceHeader => types.push(this.typePrefix + applianceHeader.type));
      if (types.length > 0) {
        this.translate.get(types).subscribe(translatedTypes => this.translatedTypes = translatedTypes);
      }
    });
  }

  getTranslatedType(type: string): string {
    if (this.translatedTypes != null) {
      return this.translatedTypes[this.typePrefix + type];
    }
    return '';
  }
}
