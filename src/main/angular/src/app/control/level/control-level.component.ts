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

import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {simpleControlType} from '../../shared/form-util';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormArray, FormBuilder, FormControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {MultiSwitch} from './multi-switch';
import {Switch} from '../switch/switch';
import {HttpSwitch} from '../http/http-switch';
import {ListItem} from '../../shared/list-item';
import {ControlDefaults} from '../control-defaults';
import {PowerLevel} from './power-level';
import {ModbusSwitch} from '../modbus/modbus-switch';
import {HttpWrite} from '../../http/write/http-write';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {ModbusSetting} from '../../settings/modbus/modbus-setting';

@Component({
  selector: 'app-control-multi',
  templateUrl: './control-multi.component.html',
  styleUrls: ['./control-multi.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ControlMultiComponent implements OnChanges, OnInit {
  @Input()
  multiSwitch: MultiSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  modbusSettings: ModbusSetting[];
  @Input()
  modbusConfigured: boolean;
  @Input()
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  controlTypes: ListItem[] = [];
  controlIds: string[] = [];

  constructor(private logger: Logger,
              private fb: FormBuilder,
              private parent: FormGroupDirective,
              private changeDetectorRef: ChangeDetectorRef,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.multiSwitch) {
      if (changes.multiSwitch.currentValue) {
        this.multiSwitch = changes.multiSwitch.currentValue;
      } else {
        this.multiSwitch = new MultiSwitch();
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    const controlTypeKeys = [Switch.TYPE, HttpSwitch.TYPE];
    if(this.modbusConfigured) {
      controlTypeKeys.push(ModbusSwitch.TYPE);
    }
    this.translate.get(controlTypeKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.controlTypes.push({value: simpleControlType(key), viewValue: translatedStrings[key]} as ListItem);
      });
    });
    this.expandParentForm();
  }

  expandParentForm() {
    const firstControlType = simpleControlType(this.multiSwitch?.controls[0]['@class']) ?? Switch.TYPE;
    this.formHandler.addFormControl(this.form, 'realControlType', firstControlType);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'controls',
      this.multiSwitch.controls);
    this.controlIds = this.multiSwitch.controls.map(control => control.id);
    this.form.addControl('powerLevels', this.fb.array([]));
    this.powerLevelFormArray.clear();
    this.multiSwitch.powerLevels.forEach(powerlevel => this.addPowerLevel(powerlevel));
  }

  realControlTypeChanged(newType?: string | undefined) {
    for(let i=0; i<this.controlsFormArray.length; i++) {
      this.controlsFormArray.removeAt(i);
    }
    this.multiSwitch.controls = [];
    this.controlIds = [];
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  get realControlType() {
    return this.form.controls.realControlType.value;
  }

  get isRealControlTypeSwitch() {
    return this.realControlType === simpleControlType(Switch.TYPE);
  }

  get isRealControlTypeHttp() {
    return this.realControlType === simpleControlType(HttpSwitch.TYPE);
  }

  get isRealControlTypeModbus() {
    return this.realControlType === simpleControlType(ModbusSwitch.TYPE);
  }

  getControlFormGroup(index: number) {
    return this.controlsFormArray.controls[index];
  }

  addControl() {
    const nextId = this.controlIds.length > 0 ? Math.max(...this.controlIds.map(controlId => Number.parseInt(controlId))) + 1 : 1;
    if(this.isRealControlTypeSwitch) {
      const switch_ = new Switch({id: nextId.toString()});
      this.levelSwitch.controls.push(switch_)
    }
    else if(this.isRealControlTypeHttp) {
      const httpSwitch = new HttpSwitch({id: nextId.toString()});
      httpSwitch.httpWrites = [HttpWrite.createWithSingleChild()];
      this.levelSwitch.controls.push(httpSwitch)
    }
    else if(this.isRealControlTypeModbus) {
      const modbusSwitch = new ModbusSwitch({id: nextId.toString()});
      modbusSwitch.modbusWrites = [ModbusWrite.createWithSingleChild()];
      this.levelSwitch.controls.push(modbusSwitch)
    }
    this.controlIds.push(nextId.toString());
    this.controlsFormArray.push(new FormGroup({}))
    for(let i=0; i<this.powerLevelFormArray.length; i++) {
      (this.powerLevelFormArray.at(i) as FormGroup).addControl(nextId.toString(), new FormControl());
    }
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  removeControl(index: number) {
    this.controlsFormArray.removeAt(index);
    this.multiSwitch.controls.splice(index, 1);

    this.controlIds.splice(index, 1);

    const formControlName = index.toString();
    for(let i=0; i<this.powerLevelFormArray.length; i++) {
      (this.powerLevelFormArray.at(i) as FormGroup).removeControl(formControlName);
    }
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  get controlsFormArray() {
    return this.form.controls.controls as FormArray;
  }

  getPowerLevelFormGroup(index: number): FormGroup {
    return this.powerLevelFormArray.controls[index] as FormGroup;
  }

  get powerLevelFormArray() {
    return this.form.controls.powerLevels as FormArray;
  }

  public addPowerLevel(powerLevel?: PowerLevel) {
    const powerLevelFormGroup = new FormGroup({
      power: new FormControl(powerLevel?.power),
    });
    this.controlIds.forEach(controlId => powerLevelFormGroup
      .addControl(controlId, new FormControl(powerLevel?.switchStatuses.find(status => status.idref === controlId)?.on ?? false)))
    this.powerLevelFormArray.push(powerLevelFormGroup);
  }

  public removePowerLevel(index: number) {
    this.powerLevelFormArray.removeAt(index);
  }

  updateModelFromForm(): MultiSwitch | undefined {
    return this.multiSwitch;
  }
}
