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

import { Component, OnInit } from '@angular/core';
import {ApplianceService} from '../shared/appliance.service';
import {Settings} from '../shared/settings';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styles: []
})
export class SettingsComponent implements OnInit {

  settings: Settings;

  constructor(private applianceService: ApplianceService) {
    this.settings = this.applianceService.getSettings();
  }

  ngOnInit() {
  }

  submitForm() {
    this.applianceService.updateSettings(this.settings);
  }

}
