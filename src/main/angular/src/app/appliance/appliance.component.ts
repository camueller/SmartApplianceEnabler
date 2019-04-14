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
import {ApplianceService} from './appliance.service';
import {ActivatedRoute, CanDeactivate, Router} from '@angular/router';
import {ApplianceFactory} from './appliance-factory';
import {AppliancesReloadService} from './appliances-reload-service';
import {Location} from '@angular/common';
import {NgForm} from '@angular/forms';
import {ApplianceErrorMessages} from './appliance-error-messages';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ErrorMessages} from '../shared/error-messages';
import {Appliance} from './appliance';
import {Observable} from 'rxjs';
import {DialogService} from '../shared/dialog.service';
import {Logger} from '../log/logger';

@Component({
  selector: 'app-appliance-details',
  templateUrl: './appliance.component.html',
  styleUrls: ['./appliance.component.css', '../global.css']
})
export class ApplianceComponent implements OnInit, CanDeactivate<ApplianceComponent> {
  @ViewChild('detailsForm') detailsForm: NgForm;
  appliance: Appliance;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_ID = InputValidatorPatterns.APPLIANCE_ID;
  isNew = false;
  discardChangesMessage: string;
  confirmDeletionMessage: string;

  constructor(private logger: Logger,
              private applianceService: ApplianceService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private translate: TranslateService,
              private dialogService: DialogService,
              private location: Location,
              private router: Router
  ) {
    const applianceFactory = new ApplianceFactory(logger);
    this.appliance = applianceFactory.createEmptyAppliance();
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.logger.debug('ApplianceComponent.ngOnInit()');
    this.errorMessages =  new ApplianceErrorMessages(this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.translate.get('ApplianceComponent.confirmDeletion').subscribe(translated => this.confirmDeletionMessage = translated);
    this.route.paramMap.subscribe(() => this.isNew = this.route.snapshot.paramMap.get('id') == null);
    this.route.data.subscribe((data: {appliance: Appliance}) => {
      if (data.appliance) {
        this.appliance = data.appliance;
        this.detailsForm.reset(this.appliance);
      }
    });
    this.detailsForm.statusChanges.subscribe(() =>
      this.errors = this.errorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.detailsForm, this.errorMessages));
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.detailsForm.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  submitForm() {
    this.logger.debug('ApplianceComponent.submitForm()');

    // FIXME use form model in order to avoid empty strings being assigned to properties of type number
    this.appliance.minPowerConsumption = this.appliance.minPowerConsumption
    && this.appliance.minPowerConsumption.toString() !== '' ? this.appliance.minPowerConsumption : undefined;
    this.appliance.minOnTime = this.appliance.minOnTime
    && this.appliance.minOnTime.toString() !== '' ? this.appliance.minOnTime : undefined;
    this.appliance.maxOnTime = this.appliance.maxOnTime
    && this.appliance.maxOnTime.toString() !== '' ? this.appliance.maxOnTime : undefined;
    this.appliance.minOffTime = this.appliance.minOffTime
    && this.appliance.minOffTime.toString() !== '' ? this.appliance.minOffTime : undefined;
    this.appliance.maxOffTime = this.appliance.maxOffTime
    && this.appliance.maxOffTime.toString() !== '' ? this.appliance.maxOffTime : undefined;

    this.applianceService.updateAppliance(this.appliance, this.isNew).subscribe(() => {
      this.appliancesReloadService.reload();
      this.router.navigateByUrl(`appliance/${this.appliance.id}`);
    });
    this.detailsForm.form.markAsPristine();
  }

  deleteAppliance() {
    this.logger.debug('ApplianceComponent.deleteAppliance()');
    this.dialogService.confirm(this.confirmDeletionMessage).subscribe(confirmed => {
      if (confirmed) {
        this.applianceService.deleteAppliance(this.appliance.id).subscribe(() => this.appliancesReloadService.reload());
        this.location.back();
      }
    });
  }
}
