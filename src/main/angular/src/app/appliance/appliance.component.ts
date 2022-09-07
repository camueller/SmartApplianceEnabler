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

import {Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ApplianceService} from './appliance.service';
import {ActivatedRoute, CanDeactivate, Router} from '@angular/router';
import {AppliancesReloadService} from './appliances-reload-service';
import {Location} from '@angular/common';
import {AbstractControl, UntypedFormGroup, ValidatorFn, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ErrorMessages} from '../shared/error-messages';
import {Appliance} from './appliance';
import {Observable} from 'rxjs';
import {DialogService} from '../shared/dialog.service';
import {Logger} from '../log/logger';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../shared/error-message';
import {FormHandler} from '../shared/form-handler';
import {getValidInt, getValidString} from '../shared/form-util';
import {ApplianceType} from './appliance-type';
import {ListItem} from '../shared/list-item';

@Component({
  selector: 'app-appliance',
  templateUrl: './appliance.component.html',
  styleUrls: ['./appliance.component.scss']
})
export class ApplianceComponent implements OnChanges, OnInit, CanDeactivate<ApplianceComponent> {
  applianceId: string;
  appliance: Appliance;
  applianceIdsUsedElsewhere: string[];
  form: UntypedFormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  isNew = false;
  discardChangesMessage: string;
  confirmDeletionMessage: string;
  applianceTypes: ListItem[] = [];
  ApplianceType = ApplianceType;

  constructor(private logger: Logger,
              private applianceService: ApplianceService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private translate: TranslateService,
              private dialogService: DialogService,
              private location: Location,
              private router: Router
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.appliance && changes.appliance.currentValue) {
      this.appliance = changes.appliance.currentValue;
      this.buildForm();
    }
    if (changes.form) {
      this.buildForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ApplianceComponent.error.', [
      new ErrorMessage('id', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('id', ValidatorType.pattern),
      new ErrorMessage('id', ValidatorType.custom),
      new ErrorMessage('vendor', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('name', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('serial', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('minPowerConsumption', ValidatorType.pattern),
      new ErrorMessage('maxPowerConsumption', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('maxPowerConsumption', ValidatorType.pattern),
      new ErrorMessage('minOnTime', ValidatorType.pattern),
      new ErrorMessage('maxOnTime', ValidatorType.pattern),
      new ErrorMessage('minOffTime', ValidatorType.pattern),
      new ErrorMessage('maxOffTime', ValidatorType.pattern),
    ], this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.translate.get('ApplianceComponent.confirmDeletion').subscribe(translated => this.confirmDeletionMessage = translated);
    const applianceTypeKeys = Object.keys(ApplianceType).map(key => `ApplianceComponent.type.${ApplianceType[key]}`);
    this.translate.get(applianceTypeKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.applianceTypes.push({value: key.split('.')[2], viewValue: translatedStrings[key]});
      });
    });
    this.route.paramMap.subscribe(() => this.isNew = this.route.snapshot.paramMap.get('id') == null);
    this.route.data.subscribe((data: { appliance: Appliance, applianceIds: string[] }) => {
      this.appliance = data.appliance;
      this.applianceId = this.appliance?.id;
      this.applianceIdsUsedElsewhere = data.applianceIds && data.applianceIds.filter(id => id !== data.appliance.id);
      this.buildForm();
      this.form.statusChanges.subscribe(() => {
        this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
      });
      if (this.form) {
        this.form.markAsPristine();
      }
    });
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  isEvCharger() {
    return this.form && this.form.controls.type.value === ApplianceType.EV_CHARGER;
  }

  isInterruptionAllowed() {
    return this.form.controls.interruptionsAllowed.value;
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

  buildForm() {
    this.form = new UntypedFormGroup({});
    this.formHandler.addFormControl(this.form, 'id', this.appliance && this.appliance.id,
      [Validators.required, Validators.pattern(InputValidatorPatterns.APPLIANCE_ID),
        this.isApplianceIdValid(this.applianceIdsUsedElsewhere)]);
    this.formHandler.addFormControl(this.form, 'vendor', this.appliance && this.appliance.vendor,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'name', this.appliance && this.appliance.name,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'type', this.appliance && this.appliance.type,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'serial', this.appliance && this.appliance.serial,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'minPowerConsumption',
      this.appliance && this.appliance.minPowerConsumption,
      Validators.pattern(InputValidatorPatterns.INTEGER));
    this.formHandler.addFormControl(this.form, 'maxPowerConsumption',
      this.appliance && this.appliance.maxPowerConsumption,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'interruptionsAllowed',
      this.appliance && this.appliance.interruptionsAllowed);
    this.formHandler.addFormControl(this.form, 'minOnTime',
      this.appliance && this.appliance.minOnTime,
      Validators.pattern(InputValidatorPatterns.INTEGER));
    this.formHandler.addFormControl(this.form, 'maxOnTime',
      this.appliance && this.appliance.maxOnTime,
      Validators.pattern(InputValidatorPatterns.INTEGER));
    this.formHandler.addFormControl(this.form, 'minOffTime',
      this.appliance && this.appliance.minOffTime,
      Validators.pattern(InputValidatorPatterns.INTEGER));
    this.formHandler.addFormControl(this.form, 'maxOffTime',
      this.appliance && this.appliance.maxOffTime,
      Validators.pattern(InputValidatorPatterns.INTEGER));
    this.formHandler.addFormControl(this.form, 'notificationSenderId', this.appliance && this.appliance.notificationSenderId);
  }

  updateModelFromForm() {
    if (!this.appliance) {
      this.appliance = new Appliance();
    }
    this.appliance.id = getValidString(this.form.controls.id.value);
    this.appliance.vendor = getValidString(this.form.controls.vendor.value);
    this.appliance.name = getValidString(this.form.controls.name.value);
    this.appliance.type = getValidString(this.form.controls.type.value);
    this.appliance.serial = getValidString(this.form.controls.serial.value);
    this.appliance.minPowerConsumption = this.isEvCharger() ? getValidInt(this.form.controls.minPowerConsumption.value) : undefined;
    this.appliance.maxPowerConsumption = getValidInt(this.form.controls.maxPowerConsumption.value);
    this.appliance.interruptionsAllowed = this.form.controls.interruptionsAllowed.value;
    this.appliance.minOnTime = this.appliance.interruptionsAllowed ? getValidInt(this.form.controls.minOnTime.value) : undefined;
    this.appliance.maxOnTime = this.appliance.interruptionsAllowed ? getValidInt(this.form.controls.maxOnTime.value) : undefined;
    this.appliance.minOffTime = this.appliance.interruptionsAllowed ? getValidInt(this.form.controls.minOffTime.value) : undefined;
    this.appliance.maxOffTime = this.appliance.interruptionsAllowed ? getValidInt(this.form.controls.maxOffTime.value) : undefined;
    this.appliance.notificationSenderId = getValidString(this.form.controls.notificationSenderId.value);
  }

  isApplianceIdValid(applianceIdsUsedElsewhere: string[]): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
      if (applianceIdsUsedElsewhere && applianceIdsUsedElsewhere.find(id => id === control.value)) {
        return {['custom']: true};
      }
    };
  }

  submitForm() {
    this.updateModelFromForm();
    this.applianceService.updateAppliance(this.applianceId ?? this.appliance.id, this.appliance, this.isNew).subscribe(() => {
      this.appliancesReloadService.reload();
      this.router.navigateByUrl(`appliance/${this.appliance.id}`);
    });
    this.form.markAsPristine();
  }
}
