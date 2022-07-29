/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormHandler} from '../../shared/form-handler';
import {ControlContainer, UntypedFormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Logger} from '../../log/logger';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {MeterReportingSwitch} from './meter-reporting-switch';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ControlDefaults} from '../control-defaults';
import {Switch} from '../switch/switch';
import {getValidInt} from '../../shared/form-util';

@Component({
  selector: 'app-control-meterreporting',
  templateUrl: './control-meterreporting.component.html',
  styles: [
  ],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlMeterreportingComponent implements OnChanges, OnInit {
  @Input()
  meterReportingSwitch: MeterReportingSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  form: UntypedFormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.switch_) {
      if (changes.switch_.currentValue) {
        this.meterReportingSwitch = changes.switch_.currentValue;
      } else {
        this.meterReportingSwitch = new MeterReportingSwitch();
      }
    }
    this.expandParentForm();
  }

  ngOnInit(): void {
    this.errorMessages = new ErrorMessages('ControlMeterreportingComponent.error.', [
      new ErrorMessage('powerThreshold', ValidatorType.pattern),
      new ErrorMessage('offDetectionDelay', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.expandParentForm();
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'powerThreshold', this.meterReportingSwitch.powerThreshold,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'offDetectionDelay', this.meterReportingSwitch.offDetectionDelay,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(): MeterReportingSwitch | undefined {
    const powerThreshold = getValidInt(this.form.controls.powerThreshold.value);
    const offDetectionDelay = getValidInt(this.form.controls.offDetectionDelay.value);

    this.meterReportingSwitch.powerThreshold = powerThreshold;
    this.meterReportingSwitch.offDetectionDelay = offDetectionDelay;
    return this.meterReportingSwitch;
  }
}
