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

import {Component, OnInit, ViewChild} from '@angular/core';
import {ApplianceService} from '../shared/appliance.service';
import {ActivatedRoute} from '@angular/router';
import 'rxjs/add/operator/switchMap';
import {ApplianceFactory} from '../shared/appliance-factory';
import {AppliancesReloadService} from '../shared/appliances-reload-service';
import {Location} from '@angular/common';
import {NgForm} from '@angular/forms';
import {ApplianceDetailsErrorMessages} from './appliance-details-error-messages';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ErrorMessages} from '../shared/error-messages';

@Component({
  selector: 'app-appliance-details',
  templateUrl: './appliance-details.component.html',
  styles: []
})
export class ApplianceDetailsComponent implements OnInit {
  @ViewChild('detailsForm') detailsForm: NgForm;
  appliance = ApplianceFactory.createEmptyAppliance();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_ID = InputValidatorPatterns.APPLIANCE_ID;
  isNew = false;

  constructor(private applianceService: ApplianceService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private translate: TranslateService,
              private location: Location) {
    this.errorMessages =  new ApplianceDetailsErrorMessages(this.translate);
  }

  ngOnInit() {
    this.route.params.subscribe(val => {
      const id = this.route.snapshot.paramMap.get('id');
      if (id == null) {
        this.isNew = true;
      } else {
        this.applianceService.getAppliance(id).subscribe(appliance => {
          this.detailsForm.reset(appliance);
        });
      }
    });

    this.detailsForm.statusChanges.subscribe(() =>
      this.errors = ErrorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.detailsForm, this.errorMessages));
  }

  submitForm() {
    this.applianceService.updateAppliance(this.appliance, this.isNew).subscribe(() => this.appliancesReloadService.reload());
  }

  deleteAppliance() {
    this.applianceService.deleteAppliance(this.appliance.id).subscribe(() => this.appliancesReloadService.reload());
    this.location.back();
  }
}
