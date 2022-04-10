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

import {ControlPage} from './control.page';
import {LevelSwitch} from '../../../../../main/angular/src/app/control/level/level-switch';
import {
  assertCheckbox,
  assertInput,
  assertSelectOption,
  clickButton, inputText,
  selectOption,
  selectorButton, selectorCheckboxByFormControlName, selectorCheckboxCheckedByFormControlName, selectorInputByFormControlName,
  selectorSelectByFormControlName,
  selectorSelectedByFormControlName, setCheckboxEnabled
} from '../../shared/form';
import {simpleControlType} from '../../../../../main/angular/src/app/shared/form-util';
import {Switch} from '../../../../../main/angular/src/app/control/switch/switch';
import {SwitchPage} from './switch.page';

export class LevelControlPage extends ControlPage {

  private static selectorPrefix = 'app-control-level';

  private static controlSelectorPrefix(controlIndex: number, controlTypeElement: string) {
    return `*[formarrayname="controls"] > ${controlTypeElement}:nth-child(${controlIndex + 1})`;
  }

  private static powerLevelSelectorPrefix(powerLevelIndex: number) {
    return `*[formarrayname="powerLevels"] > *[ng-reflect-name="${powerLevelIndex}"]`;
  }

  public static async setLevelSwitch(t: TestController, levelSwitch: LevelSwitch) {
    await LevelControlPage.setType(t, LevelSwitch.TYPE);
    const firstControlType = levelSwitch.controls[0]['@class'];
    await LevelControlPage.setRealType(t, firstControlType);
    for(let i=0; i<levelSwitch.controls.length; i++) {
      await LevelControlPage.clickAddControl(t);
      if (firstControlType === Switch.TYPE) {
        await SwitchPage.setSwitch(t, levelSwitch.controls[i] as Switch,
          LevelControlPage.controlSelectorPrefix(i, 'app-control-switch'), false);
      }
    }
    for(let i=0; i<levelSwitch.powerLevels.length; i++) {
      await LevelControlPage.clickAddPowerLevel(t);
      await LevelControlPage.setPower(t, levelSwitch.powerLevels[i].power.toString(), LevelControlPage.powerLevelSelectorPrefix(i));
      for(let j=0; j<levelSwitch.controls.length; j++) {
        await LevelControlPage.setSwitchStatus(t, j, levelSwitch.powerLevels[i].switchStatuses[j].on, LevelControlPage.powerLevelSelectorPrefix(i));
      }
    }
  }

  public static async assertLevelSwitch(t: TestController, levelSwitch: LevelSwitch) {
    await LevelControlPage.assertType(t, LevelSwitch.TYPE);
    const firstControlType = levelSwitch.controls[0]['@class'];
    await LevelControlPage.assertRealType(t, firstControlType);
    for(let i=0; i<levelSwitch.controls.length; i++) {
      if (firstControlType === Switch.TYPE) {
        await SwitchPage.assertSwitch(t, levelSwitch.controls[i] as Switch,
          LevelControlPage.controlSelectorPrefix(i, 'app-control-switch'), false);
      }
    }
    for(let i=0; i<levelSwitch.powerLevels.length; i++) {
      await LevelControlPage.assertPower(t, levelSwitch.powerLevels[i].power.toString(), LevelControlPage.powerLevelSelectorPrefix(i));
      for(let j=0; j<levelSwitch.controls.length; j++) {
        await LevelControlPage.assertSwitchStatus(t, j, levelSwitch.powerLevels[i].switchStatuses[j].on, LevelControlPage.powerLevelSelectorPrefix(i));
      }
    }
  }

  public static async setRealType(t: TestController, realControlType: string) {
    await selectOption(t, selectorSelectByFormControlName('realControlType'), simpleControlType(realControlType));
  }
  public static async assertRealType(t: TestController, controlType: string) {
    await assertSelectOption(t, selectorSelectedByFormControlName('realControlType'), controlType);
  }

  public static async setPower(t: TestController, power: string, selectorPrefix?: string) {
    await inputText(t, selectorInputByFormControlName('power', selectorPrefix), power);
  }
  public static async assertPower(t: TestController, power: string, selectorPrefix?: string) {
    await assertInput(t, selectorInputByFormControlName('power', selectorPrefix), power);
  }

  public static async setSwitchStatus(t: TestController, controlIndex: number, on: boolean, selectorPrefix?: string) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName((controlIndex + 1).toString(), selectorPrefix), on);
  }
  public static async assertSwitchStatus(t: TestController, controlIndex: number, on: boolean, selectorPrefix?: string) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName((controlIndex + 1).toString(), selectorPrefix), on);
  }

  public static async clickAddControl(t: TestController) {
    await clickButton(t, selectorButton(LevelControlPage.selectorPrefix, 'ControlLevelComponent__addControl'));
  }

  public static async clickAddPowerLevel(t: TestController) {
    await clickButton(t, selectorButton(LevelControlPage.selectorPrefix, 'ControlLevelComponent__addPowerLevel'));
  }
}
