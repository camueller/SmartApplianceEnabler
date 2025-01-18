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

import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {simpleControlType} from '../../control/control';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {
  AbstractControl,
  ControlContainer,
  FormArray,
  FormControl,
  FormGroup,
  FormGroupDirective,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {LevelSwitch} from './level-switch';
import {Switch} from '../switch/switch';
import {HttpSwitch} from '../http/http-switch';
import {ListItem} from '../../shared/list-item';
import {ControlDefaults} from '../control-defaults';
import {PowerLevel} from './power-level';
import {ModbusSwitch} from '../modbus/modbus-switch';
import {HttpWrite} from '../../http/write/http-write';
import {ModbusSetting} from '../../settings/modbus/modbus-setting';
import {ModbusWrite} from '../../modbus/write/modbus-write';
import {ControlHttpComponent} from '../http/control-http.component';
import {ControlModbusComponent} from '../modbus/control-modbus.component';
import {ControlSwitchComponent} from '../switch/control-switch.component';
import {SwitchStatus} from './switch-status';
import {MqttSwitch} from '../mqtt/mqtt-switch';
import {ControlMqttComponent} from '../mqtt/control-mqtt.component';
import {ControlLevelModel, ControlLevelSupportedTypes, PowerLevelModel, SwitchStatusModel} from './control-level.model';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../../shared/form-util';

@Component({
  selector: 'app-control-level',
  templateUrl: './control-level.component.html',
  styleUrls: ['./control-level.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ControlLevelComponent implements OnChanges, OnInit {
  @Input()
  levelSwitch: LevelSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  modbusSettings: ModbusSetting[];
  @Input()
  modbusConfigured: boolean;
  @ViewChildren('controlSwitchComponents')
  controlSwitchComps: QueryList<ControlSwitchComponent>;
  @ViewChildren('controlHttpComponents')
  controlHttpComps: QueryList<ControlHttpComponent>;
  @ViewChildren('controlModbusComponents')
  controlModbusComps: QueryList<ControlModbusComponent>;
  @ViewChildren('controlMqttComponents')
  controlMqttComps: QueryList<ControlMqttComponent>;
  @Input()
  form: FormGroup<ControlLevelModel>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  controlTypes: ListItem[] = [];
  controlIds: string[] = [];

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private changeDetectorRef: ChangeDetectorRef,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.levelSwitch) {
      if (changes.levelSwitch.currentValue) {
        this.levelSwitch = changes.levelSwitch.currentValue;
      } else {
        this.levelSwitch = new LevelSwitch();
        this.levelSwitch.controls = [];
        this.levelSwitch.powerLevels = [];
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    const controlTypeKeys = [Switch.TYPE, HttpSwitch.TYPE, MqttSwitch.TYPE];
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    let realControlType = simpleControlType(Switch.TYPE);
    let controls = [];
    this.controlIds = [];
    if(this.levelSwitch?.controls && this.levelSwitch?.controls.length > 0) {
      realControlType = simpleControlType(this.levelSwitch?.controls[0]['@class']) ?? Switch.TYPE;
      controls = this.levelSwitch.controls;
      this.controlIds = this.levelSwitch.controls.map(control => control.id);
    }
    this.form.addControl('realControlType', new FormControl(realControlType));
    this.form.addControl('controls', buildFormArrayWithEmptyFormGroups(controls));
    this.form.addControl('powerLevels', new FormArray([]));
    this.powerLevelFormArray?.clear();
    this.levelSwitch.powerLevels?.forEach(powerlevel => this.addPowerLevel(powerlevel));
    this.form.setValidators(this.isFormValid());
  }

  isFormValid(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (this.controlsFormArray?.length > 0 && this.powerLevelFormArray?.length > 0) {
        return null;
      }
      return {['custom']: true};
    };
  }

  realControlTypeChanged(newType?: string | undefined) {
    for(let i=0; i<this.controlsFormArray.length; i++) {
      this.controlsFormArray.removeAt(i);
    }
    this.levelSwitch.controls = [];
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

  get isRealControlTypeMqtt() {
    return this.realControlType === simpleControlType(MqttSwitch.TYPE);
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
    else if(this.isRealControlTypeMqtt) {
      const mqttSwitch = new MqttSwitch({id: nextId.toString()});
      this.levelSwitch.controls.push(mqttSwitch)
    }
    this.controlIds.push(nextId.toString());
    this.controlsFormArray.push(new FormGroup({} as ControlLevelSupportedTypes))
    for(let i=0; i<this.powerLevelFormArray.length; i++) {
      const switchStatusFormGroup = new FormGroup<SwitchStatusModel>({
        idref: new FormControl(nextId.toString()),
        on: new FormControl(false)
      });
      this.powerLevelFormArray.at(i).controls.switchStatuses.push(switchStatusFormGroup);
    }
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  removeControl(index: number) {
    this.controlsFormArray.removeAt(index);
    this.levelSwitch.controls.splice(index, 1);

    this.controlIds.splice(index, 1);

    const formControlName = index.toString();
    for(let i=0; i<this.powerLevelFormArray.length; i++) {
      this.powerLevelFormArray.at(i).controls.switchStatuses.removeAt(i);
    }
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  get controlsFormArray() {
    return this.form.controls.controls;
  }

  get powerLevelFormArray() {
    return this.form.controls.powerLevels;
  }

  public addPowerLevel(powerLevel?: PowerLevel) {
    const powerLevelFormGroup = new FormGroup<PowerLevelModel>({} as PowerLevelModel);
    powerLevelFormGroup.addControl('power', new FormControl(powerLevel?.power, Validators.required));

    const switchStatusFormArray = new FormArray([]);
    this.controlIds.forEach(controlId => {
      const switchStatusFormGroup = new FormGroup<SwitchStatusModel>({
        idref: new FormControl(controlId),
        on: new FormControl(powerLevel?.switchStatuses.find(status => status.idref === controlId)?.on ?? false)
      });
      switchStatusFormArray.push(switchStatusFormGroup);
    });
    powerLevelFormGroup.addControl('switchStatuses', switchStatusFormArray);
    this.powerLevelFormArray.push(powerLevelFormGroup);
  }

  public removePowerLevel(index: number) {
    this.powerLevelFormArray.removeAt(index);
  }

  private getSwitchingStatus(powerLevelFormGroup: FormGroup<PowerLevelModel>, idref: string) {
    const switchingStatuses = powerLevelFormGroup.controls.switchStatuses;
    for(let i=0; i<switchingStatuses.length; i++) {
      if(switchingStatuses.at(i).controls.idref.value === idref) {
        return switchingStatuses.at(i).controls.on.value;
      }
    }
  }

  updateModelFromForm(): LevelSwitch | undefined {
    let controls: (Switch | HttpSwitch | ModbusSwitch | MqttSwitch)[] = [];
    if(this.isRealControlTypeSwitch) {
      this.controlSwitchComps.forEach(controlSwitchComponent => {
        const controlSwitch = controlSwitchComponent.updateModelFromForm();
        if(controlSwitch) {
          controls.push(controlSwitch);
        }
      });
    } else if(this.isRealControlTypeHttp) {
      this.controlHttpComps.forEach(controlHttpComponent => {
        const controlHttp = controlHttpComponent.updateModelFromForm();
        if(controlHttp) {
          controls.push(controlHttp);
        }
      });
    } else if(this.isRealControlTypeModbus) {
      this.controlModbusComps.forEach(controlModbusComponent => {
        const controlModbus = controlModbusComponent.updateModelFromForm();
        if(controlModbus) {
          controls.push(controlModbus);
        }
      });
    } else if(this.isRealControlTypeMqtt) {
      this.controlMqttComps.forEach(controlMqttComponent => {
        const controlMqtt = controlMqttComponent.updateModelFromForm();
        if(controlMqtt) {
          controls.push(controlMqtt);
        }
      });
    }

    const powerLevels: PowerLevel[] = [];
    for(let i=0; i<this.powerLevelFormArray.length; i++) {
      // const power = this.getPowerLevelFormGroup(i).controls.power.value;
      const powerLevelFormGroup = this.powerLevelFormArray.at(i);
      const power = powerLevelFormGroup.controls.power.value;

      const switchStatuses: SwitchStatus[] = [];
      this.controlIds.forEach(controlId => {
        const switchStatus = new SwitchStatus({
          idref: controlId,
          on: this.getSwitchingStatus(powerLevelFormGroup, controlId),
        });
        switchStatuses.push(switchStatus);
      });

      const powerLevel = new PowerLevel({power, switchStatuses});
      powerLevels.push(powerLevel);
    }

    if (!(controls.length > 0 || powerLevels.length > 0)) {
      return undefined;
    }

    this.levelSwitch.controls = controls;
    this.levelSwitch.powerLevels = powerLevels;
    return this.levelSwitch;
  }
}
