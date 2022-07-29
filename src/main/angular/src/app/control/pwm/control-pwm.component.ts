/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroupDirective, UntypedFormGroup, Validators} from '@angular/forms';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {PwmSwitch} from './pwm-switch';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {getValidFloat, getValidInt} from '../../shared/form-util';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';

@Component({
  selector: 'app-control-pwm',
  templateUrl: './control-pwm.component.html',
  styleUrls: ['./control-pwm.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ControlPwmComponent implements OnChanges, OnInit {
  @Input()
  pwmSwitch: PwmSwitch;
  @Input()
  form: UntypedFormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.pwmSwitch) {
      if (changes.pwmSwitch.currentValue) {
        this.pwmSwitch = changes.pwmSwitch.currentValue;
      } else {
        this.pwmSwitch = new PwmSwitch();
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlPwmComponent.error.', [
      new ErrorMessage('gpio', ValidatorType.pattern),
      new ErrorMessage('pwmFrequency', ValidatorType.pattern),
      new ErrorMessage('minDutyCycle', ValidatorType.pattern),
      new ErrorMessage('maxDutyCycle', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'gpio',
      this.pwmSwitch && this.pwmSwitch.gpio,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'pwmFrequency',
      this.pwmSwitch && this.pwmSwitch.pwmFrequency,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'minDutyCycle',
      this.pwmSwitch && this.pwmSwitch.minDutyCycle,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    this.formHandler.addFormControl(this.form, 'maxDutyCycle',
      this.pwmSwitch && this.pwmSwitch.maxDutyCycle,
      [Validators.required, Validators.pattern(InputValidatorPatterns.FLOAT)]);
  }

  updateModelFromForm(): PwmSwitch | undefined {
    const gpio = getValidInt(this.form.controls.gpio.value);
    const pwmFrequency = getValidInt(this.form.controls.pwmFrequency.value);
    const minDutyCycle = getValidFloat(this.form.controls.minDutyCycle.value);
    const maxDutyCycle = getValidFloat(this.form.controls.maxDutyCycle.value);

    if (!(gpio || pwmFrequency || minDutyCycle || maxDutyCycle)) {
      return undefined;
    }

    this.pwmSwitch.gpio = gpio;
    this.pwmSwitch.pwmFrequency = pwmFrequency;
    this.pwmSwitch.minDutyCycle = minDutyCycle;
    this.pwmSwitch.maxDutyCycle = maxDutyCycle;
    return this.pwmSwitch;
  }
}
