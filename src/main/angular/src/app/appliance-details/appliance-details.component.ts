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
import {Appliance} from '../shared/appliance';
import {ApplianceService} from '../shared/appliance.service';
import {ActivatedRoute, ParamMap} from '@angular/router';
import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'app-appliance-details',
  templateUrl: './appliance-details.component.html',
  styles: []
})
export class ApplianceDetailsComponent implements OnInit {

  appliance: Appliance;
  isNew = false;

  constructor(private applianceService: ApplianceService, private route: ActivatedRoute) {
    // TODO auf Observables umstellen - siehe Tour of Heroes (Routing)!
    /*
    this.appliance = this.route.paramMap.switchMap(
      (params: ParamMap) => this.applianceService.getAppliance(params.get('id'))
    );
    */
    route.params.subscribe(val => {
      const id = this.route.snapshot.paramMap.get('id');
      if (id == null) {
        this.appliance = new Appliance();
        this.isNew = true;
      } else {
        this.appliance = this.applianceService.getAppliance(id);
      }
    });
  }

  // TODO Namensänderungen sollen sich sofort im Menü auswirken - siehe Tour of Heroes!
  // TODO save/cancel verwenden  - siehe Tour of Heroes (Routing)!
  // TODO CanDeactivate guard verwenden, um nach Änderungen das Wegnavigieren zu verhindern - siehe Tour of Heroes (Routing)!

  ngOnInit() {
  }

  submitForm() {
    if (this.isNew) {
      this.applianceService.createAppliance(this.appliance);
    } else {
      this.applianceService.updateAppliance(this.appliance);
    }
  }

}
