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
import {ActivatedRoute, CanDeactivateFn, Router} from '@angular/router';
import {AppliancesReloadService} from './appliances-reload-service';
import {Location} from '@angular/common';
import {AbstractControl, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ErrorMessages} from '../shared/error-messages';
import {Appliance} from './appliance';
import {Observable} from 'rxjs';
import {DialogService} from '../shared/dialog.service';
import {Logger} from '../log/logger';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../shared/error-message';
import {getValidInt, getValidString, isRequired} from '../shared/form-util';
import {ApplianceType} from './appliance-type';
import {ListItem} from '../shared/list-item';
import {ApplianceModel} from './appliance.model';

@Component({
  selector: 'app-appliance',
  templateUrl: './appliance.component.html',
  styleUrls: ['./appliance.component.scss']
})
export class ApplianceComponent  implements OnChanges, OnInit {
  applianceId: string;
  appliance: Appliance;
  applianceIdsUsedElsewhere: string[];
  form: FormGroup<ApplianceModel>;
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
    return this.form.controls.type.value === ApplianceType.EV_CHARGER;
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
    this.form = new FormGroup({
      id: new FormControl( this.appliance?.id, [Validators.required, Validators.pattern(InputValidatorPatterns.APPLIANCE_ID)]),
      vendor: new FormControl(this.appliance?.vendor, Validators.required),
      name: new FormControl(this.appliance?.name, Validators.required),
      type: new FormControl(this.appliance?.type, Validators.required),
      serial: new FormControl(this.appliance?.serial, Validators.required),
      minPowerConsumption: new FormControl(this.appliance?.minPowerConsumption, Validators.pattern(InputValidatorPatterns.INTEGER)),
      maxPowerConsumption: new FormControl(this.appliance?.maxPowerConsumption, [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]),
      interruptionsAllowed: new FormControl(this.appliance?.interruptionsAllowed),
      minOnTime: new FormControl(this.appliance?.minOnTime, Validators.pattern(InputValidatorPatterns.INTEGER)),
      minOffTime: new FormControl(this.appliance?.minOffTime, Validators.pattern(InputValidatorPatterns.INTEGER)),
      maxOnTime: new FormControl(this.appliance?.maxOnTime, Validators.pattern(InputValidatorPatterns.INTEGER)),
      maxOffTime: new FormControl(this.appliance?.maxOffTime, Validators.pattern(InputValidatorPatterns.INTEGER)),
      notificationSenderId: new FormControl(this.appliance?.notificationSenderId),
    });
  }

  updateModelFromForm() {
    if (!this.appliance) {
      this.appliance = new Appliance();
    }
    this.appliance.id = this.form.controls.id.value;
    this.appliance.vendor = this.form.controls.vendor.value;
    this.appliance.name = this.form.controls.name.value;
    this.appliance.type = this.form.controls.type.value;
    this.appliance.serial = this.form.controls.serial.value;
    this.appliance.minPowerConsumption = this.form.controls.minPowerConsumption.value;
    this.appliance.maxPowerConsumption = this.form.controls.maxPowerConsumption.value;
    this.appliance.interruptionsAllowed = this.form.controls.interruptionsAllowed.value;
    this.appliance.minOnTime = this.appliance.interruptionsAllowed ? this.form.controls.minOnTime.value : undefined;
    this.appliance.maxOnTime = this.appliance.interruptionsAllowed ? this.form.controls.maxOnTime.value : undefined;
    this.appliance.minOffTime = this.appliance.interruptionsAllowed ? this.form.controls.minOffTime.value : undefined;
    this.appliance.maxOffTime = this.appliance.interruptionsAllowed ? this.form.controls.maxOffTime.value : undefined;
    this.appliance.notificationSenderId = this.form.controls.notificationSenderId.value;
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  isApplianceIdValid(applianceIdsUsedElsewhere: string[]): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
      if (applianceIdsUsedElsewhere && applianceIdsUsedElsewhere.find(id => id === control.value)) {
        return {custom: true};
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
