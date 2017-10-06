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
import {ActivatedRoute} from '@angular/router';
import {ApplianceService} from '../shared/appliance.service';
import {Appliance} from '../shared/appliance';

@Component({
  selector: 'app-appliance-switch',
  templateUrl: './appliance-switch.component.html',
  styles: []
})
export class ApplianceSwitchComponent implements OnInit {

  appliance: Appliance;

  constructor(private applianceService: ApplianceService, private route: ActivatedRoute) {
    // TODO auf Observables umstellen - siehe Tour of Heroes (Routing)!
    /*
    this.appliance = this.route.paramMap.switchMap(
      (params: ParamMap) => this.applianceService.getAppliance(params.get('id'))
    );
    */
    route.params.subscribe(val => {
      const id = this.route.snapshot.paramMap.get('id');
      this.appliance = this.applianceService.getAppliance(id);
    });
  }

  ngOnInit() {
  }

  submitForm() {
    this.applianceService.updateAppliance(this.appliance);
  }

}
