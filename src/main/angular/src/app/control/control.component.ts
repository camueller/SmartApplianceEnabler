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
import { ActivatedRoute } from '@angular/router';
import {ControlFactory} from './control-factory';
import {TranslateService} from '@ngx-translate/core';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {ControlDefaults} from './control-defaults';
import {ControlService} from './control-service';
import {Control} from './control';
import {Observable} from 'rxjs';
import {DialogService} from '../shared/dialog.service';
import {MockSwitch} from './mock-switch';
import {Logger} from '../log/logger';
import {Settings} from '../settings/settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {Appliance} from '../appliance/appliance';
import {Switch} from './switch/switch';
import {ControlSwitchComponent} from './switch/control-switch.component';
import {ControlHttpComponent} from './http/control-http.component';
import {HttpSwitch} from './http/http-switch';
import {AlwaysOnSwitch} from './alwayson/always-on-switch';
import {ModbusSwitch} from './modbus/modbus-switch';
import {StartingCurrentSwitch} from './startingcurrent/starting-current-switch';
import {ControlModbusComponent} from './modbus/control-modbus.component';
import {ControlEvchargerComponent} from './evcharger/control-evcharger.component';
import {ControlStartingcurrentComponent} from './startingcurrent/control-startingcurrent.component';
import {EvCharger} from './evcharger/ev-charger';
import {ListItem} from '../shared/list-item';
import {simpleControlType} from '../control/control';
import {MeterDefaults} from '../meter/meter-defaults';
import {MeterReportingSwitch} from './meterreporting/meter-reporting-switch';
import {NotificationComponent} from '../notification/notification.component';
import {NotificationType} from '../notification/notification-type';
import {ControlMeterreportingComponent} from './meterreporting/control-meterreporting.component';
import {ControlPwmComponent} from './pwm/control-pwm.component';
import {PwmSwitch} from './pwm/pwm-switch';
import {LevelSwitch} from './level/level-switch';
import {ControlLevelComponent} from './level/control-level.component';
import {EvChargerTemplate} from './evcharger/ev-charger-template';
import {SwitchOption} from './switchoption/switch-option';
import {ControlSwitchOptionComponent} from './switchoption/control-switchoption.component';
import {MqttSwitch} from './mqtt/mqtt-switch';
import {ControlMqttComponent} from './mqtt/control-mqtt.component';
import {FormControl, FormGroup} from '@angular/forms';
import {ControlModel} from './control.model';
import {isRequired} from '../shared/form-util';

@Component({
  selector: 'app-control',
  templateUrl: './control.component.html',
  styleUrls: ['./control.component.scss'],
})
export class ControlComponent  implements OnInit {
  @ViewChild(ControlEvchargerComponent)
  controlEvchargerComp: ControlEvchargerComponent;
  @ViewChild(ControlHttpComponent)
  controlHttpComp: ControlHttpComponent;
  @ViewChild(ControlLevelComponent)
  controlLevelComp: ControlLevelComponent;
  @ViewChild(ControlMeterreportingComponent)
  controlMeterreportingComp: ControlMeterreportingComponent;
  @ViewChild(ControlModbusComponent)
  controlModbusComp: ControlModbusComponent;
  @ViewChild(ControlMqttComponent)
  controlMqttComp: ControlMqttComponent;
  @ViewChild(ControlPwmComponent)
  controlPwmComp: ControlPwmComponent;
  @ViewChild(ControlStartingcurrentComponent)
  controlStartingcurrentComp: ControlStartingcurrentComponent;
  @ViewChild(ControlSwitchOptionComponent)
  controlSwitchOptionComp: ControlSwitchOptionComponent;
  @ViewChild(ControlSwitchComponent)
  controlSwitchComp: ControlSwitchComponent;
  @ViewChild(NotificationComponent)
  notificationComp: NotificationComponent;
  form: FormGroup<ControlModel>;
  applianceId: string;
  controlDefaults: ControlDefaults;
  meterDefaults: MeterDefaults;
  control: Control;
  controlFactory: ControlFactory;
  appliance: Appliance;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  evChargerTemplates: EvChargerTemplate[];
  discardChangesMessage: string;
  confirmDeleteMessage: string;
  controlTypes: ListItem[] = [];

  constructor(private logger: Logger,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private dialogService: DialogService,
              private translate: TranslateService) {
    this.controlFactory = new ControlFactory(logger);
    this.control = this.controlFactory.createEmptyControl();
  }

  ngOnInit() {
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.translate.get('dialog.confirmDelete').subscribe(translated => this.confirmDeleteMessage = translated);
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {
      control: Control,
      controlDefaults: ControlDefaults,
      meterDefaults: MeterDefaults,
      appliance: Appliance,
      settings: Settings,
      settingsDefaults: SettingsDefaults,
      evChargerTemplates: EvChargerTemplate[]
    }) => {
      this.control = data.control;
      this.controlDefaults = data.controlDefaults;
      this.meterDefaults = data.meterDefaults;
      this.appliance = data.appliance;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.evChargerTemplates = data.evChargerTemplates;
      if (this.appliance.type === 'EVCharger') {
        this.control.type = EvCharger.TYPE;
      }
      const controlTypeKeys = [MeterReportingSwitch.TYPE, Switch.TYPE, HttpSwitch.TYPE, MqttSwitch.TYPE, AlwaysOnSwitch.TYPE, LevelSwitch.TYPE, PwmSwitch.TYPE];
      if(this.settings.modbusSettings) {
        controlTypeKeys.splice(3, 0, ModbusSwitch.TYPE);
      }
      this.translate.get(controlTypeKeys).subscribe(translatedStrings => {
        Object.keys(translatedStrings).forEach(key => {
          this.controlTypes.push({value: simpleControlType(key), viewValue: translatedStrings[key]} as ListItem);
        });
      });
      this.buildForm();
      if (this.form) {
        this.form.markAsPristine();
      }
    });
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  buildForm() {
    this.form = new FormGroup({
      controlType: new FormControl(this.control && simpleControlType(this.control.type)),
      startingCurrentSwitchUsed: new FormControl(this.control && this.control.startingCurrentSwitchUsed),
      switchOptionUsed: new FormControl(this.control && this.control.switchOptionUsed),
    });
    this.setStartingCurrentDetection(this.control?.startingCurrentSwitchUsed ?? false, this.control?.startingCurrentSwitch, false);
    this.setSwitchOption(this.control.switchOptionUsed ?? false, this.control.switchOption, false);
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  get notficationTypes() {
    if (this.isAlwaysOnSwitch) {
      return [
        NotificationType.COMMUNICATION_ERROR
      ];
    }
    if (this.isEvCharger) {
      return [
        NotificationType.EVCHARGER_VEHICLE_NOT_CONNECTED,
        NotificationType.EVCHARGER_VEHICLE_CONNECTED,
        NotificationType.EVCHARGER_CHARGING,
        NotificationType.EVCHARGER_CHARGING_COMPLETED,
        NotificationType.EVCHARGER_ERROR,
        NotificationType.COMMUNICATION_ERROR
      ];
    }
    return [
        NotificationType.CONTROL_ON,
        NotificationType.CONTROL_OFF,
        NotificationType.COMMUNICATION_ERROR
      ];
  }

  get isNotifcationEnabled() {
    return !!this.settings.notificationCommand;
  }

  get isMeterReportingSwitch() {
    return this.control && this.control.type === MeterReportingSwitch.TYPE;
  }

  get isAlwaysOnSwitch() {
    return this.control && this.control.type === AlwaysOnSwitch.TYPE;
  }

  get isSwitch() {
    return this.control && this.control.type === Switch.TYPE;
  }

  get isModbusSwitch() {
    return this.control && this.control.type === ModbusSwitch.TYPE;
  }

  get isMqttSwitch() {
    return this.control && this.control.type === MqttSwitch.TYPE;
  }

  get isHttpSwitch() {
    return this.control && this.control.type === HttpSwitch.TYPE;
  }

  get isLevelSwitch() {
    return this.control && this.control.type === LevelSwitch.TYPE;
  }

  get isPwmSwitch() {
    return this.control && this.control.type === PwmSwitch.TYPE;
  }

  get isEvCharger() {
    return this.control && this.control.type === EvCharger.TYPE;
  }

  delete() {
    this.dialogService.confirm(this.confirmDeleteMessage).subscribe(confirmed => {
      if (confirmed) {
        this.controlService.deleteControl(this.applianceId).subscribe(() => this.typeChanged());
      }
    });
  }

  isDeleteEnabled() {
    return this.control && (this.control.switch_ || this.control.httpSwitch || this.control.modbusSwitch
      || this.control.alwaysOnSwitch || this.control.evCharger);
  }

  typeChanged(newType?: string | undefined) {
    this.control.type = `de.avanux.smartapplianceenabler.control.${newType}`;
    // make form invalid initially in order to avoid "NG0100: Expression has changed after it was checked"
    if (!this.isAlwaysOnSwitch) {
      this.form.setErrors({'typeChanged': true});
    }
    if (!this.control.type || this.isMeterReportingSwitch) {
      this.control.startingCurrentSwitchUsed = false;
      this.control.switchOptionUsed = false;
    } else if (this.isAlwaysOnSwitch) {
      this.control.alwaysOnSwitch = this.controlFactory.createAlwaysOnSwitch();
    } else if (this.isEvCharger) {
      this.control.startingCurrentSwitchUsed = false;
      this.control.switchOptionUsed = false;
    }
    if (this.isAlwaysOnSwitch || this.isMeterReportingSwitch) {
      this.form.markAsDirty();
    }
  }

  get canHaveDecoratingControl(): boolean {
    return !(this.isMeterReportingSwitch || this.isAlwaysOnSwitch || this.isPwmSwitch || this.isLevelSwitch || this.control.type === MockSwitch.TYPE);
  }

  toggleStartingCurrentDetection() {
    this.setStartingCurrentDetection(!this.control.startingCurrentSwitchUsed);
  }

  setStartingCurrentDetection(startingCurrentSwitchUsed: boolean, startingCurrentSwitch = new StartingCurrentSwitch(), markFormAsDirty = true) {
    this.control.startingCurrentSwitchUsed = startingCurrentSwitchUsed;
    if (startingCurrentSwitchUsed) {
      this.control.startingCurrentSwitch = startingCurrentSwitch;
      this.setSwitchOption(false);
      this.setSwitchOptionControlState(false);
    } else {
      this.control.startingCurrentSwitch = null;
      this.setSwitchOptionControlState(true);
    }
    if(markFormAsDirty) {
      this.form.markAsDirty();
    }
  }

  setStartingCurrentDetectionControlState(enabled: boolean) {
    if (enabled) {
      this.form.controls.startingCurrentSwitchUsed.enable();
    } else {
      this.form.controls.startingCurrentSwitchUsed.disable();
      this.form.controls.startingCurrentSwitchUsed.setValue(false);
    }
  }

  toggleSwitchOption() {
    this.setSwitchOption(!this.control.switchOptionUsed);
  }

  setSwitchOption(switchOptionUsed: boolean, switchOption = new SwitchOption(), markFormAsDirty = true) {
    this.control.switchOptionUsed = switchOptionUsed;
    if (switchOptionUsed) {
      this.control.switchOption = switchOption;
      this.setStartingCurrentDetection(false);
      this.setStartingCurrentDetectionControlState(false);
    } else {
      this.control.switchOption = null;
      this.setStartingCurrentDetectionControlState(true);
    }
    if(markFormAsDirty) {
      this.form.markAsDirty();
    }
  }

  setSwitchOptionControlState(enabled: boolean) {
    if (enabled) {
      this.form.controls.switchOptionUsed.enable();
    } else {
      this.form.controls.switchOptionUsed.disable();
      this.form.controls.switchOptionUsed.setValue(false);
    }
  }

  submitForm() {
    if (this.controlMeterreportingComp) {
      this.control.meterReportingSwitch = this.controlMeterreportingComp.updateModelFromForm();
    }
    else if (this.controlSwitchComp) {
      this.control.switch_ = this.controlSwitchComp.updateModelFromForm();
    }
    else if (this.controlModbusComp) {
      this.control.modbusSwitch = this.controlModbusComp.updateModelFromForm();
    }
    else if (this.controlMqttComp) {
      this.control.mqttSwitch = this.controlMqttComp.updateModelFromForm();
    }
    else if (this.controlHttpComp) {
      this.control.httpSwitch = this.controlHttpComp.updateModelFromForm();
    }
    else if (this.controlLevelComp) {
      this.control.levelSwitch = this.controlLevelComp.updateModelFromForm();
    }
    else if (this.controlPwmComp) {
      this.control.pwmSwitch = this.controlPwmComp.updateModelFromForm();
    }
    else if (this.controlEvchargerComp) {
      this.control.evCharger = this.controlEvchargerComp.updateModelFromForm();
    }
    if (this.control.startingCurrentSwitchUsed) {
      this.control.startingCurrentSwitch = this.controlStartingcurrentComp.updateModelFromForm();
    }
    if(this.control.switchOptionUsed) {
      this.control.switchOption = this.controlSwitchOptionComp.updateModelFromForm();
    }
    if (this.notificationComp) {
      this.control.notifications = this.notificationComp.updateModelFromForm();
    }
    this.controlService.updateControl(this.control, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
  }
}
