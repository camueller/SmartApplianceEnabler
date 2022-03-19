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

import {MasterElectricityMeter} from '../../../../../main/angular/src/app/meter/master/master-electricity-meter';
import {
  assertCheckbox,
  assertSelectOption,
  selectOption,
  selectorCheckboxByFormControlName,
  selectorCheckboxCheckedByFormControlName,
  selectorSelectByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';

export class MasterMeterPage {
  private static readonly switchOnTrue = 'switchOn.true';
  private static readonly switchOnFalse = 'switchOn.false';
  private static readonly switchOnUndefined = 'switchOn.undefined';

  public static async setMasterMeter(t: TestController, isMasterMeter: boolean, masterMeter: MasterElectricityMeter) {
    await MasterMeterPage.setMasterMeterEnabled(t, isMasterMeter);
    if (isMasterMeter) {
      await MasterMeterPage.setMasterSwitchOn(t, masterMeter?.masterSwitchOn);
      await MasterMeterPage.setSlaveSwitchOn(t, masterMeter?.slaveSwitchOn);
    }
  }

  public static async assertMasterMeter(t: TestController, isMasterMeter: boolean, masterMeter: MasterElectricityMeter) {
    await MasterMeterPage.assertMasterMeterEnabled(t, isMasterMeter);
    if (isMasterMeter) {
      await MasterMeterPage.assertMasterSwitchOn(t, masterMeter?.masterSwitchOn);
      await MasterMeterPage.assertSlaveSwitchOn(t, masterMeter?.slaveSwitchOn);
    }
  }

  public static async setMasterMeterEnabled(t: TestController, isMasterMeter: boolean) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('isMasterMeter'), isMasterMeter);
  }
  public static async assertMasterMeterEnabled(t: TestController, isMasterMeter: boolean) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('isMasterMeter'), isMasterMeter);
  }

  public static async setMasterSwitchOn(t: TestController, masterSwitchOn: boolean | undefined) {
    if (masterSwitchOn !== undefined) {
      await selectOption(t, selectorSelectByFormControlName('masterSwitchOn'), MasterMeterPage.toSwitchOnKey(masterSwitchOn));
    }
  }
  public static async assertMasterSwitchOn(t: TestController, masterSwitchOn: boolean | undefined) {
    if (masterSwitchOn !== undefined) {
      await assertSelectOption(t, selectorSelectByFormControlName('masterSwitchOn'), MasterMeterPage.toSwitchOnKey(masterSwitchOn), 'MeterMasterComponent.');
    }
  }

  public static async setSlaveSwitchOn(t: TestController, slaveSwitchOn: boolean | undefined) {
    if (slaveSwitchOn !== undefined) {
      await selectOption(t, selectorSelectByFormControlName('slaveSwitchOn'), MasterMeterPage.toSwitchOnKey(slaveSwitchOn));
    }
  }
  public static async assertSlaveSwitchOn(t: TestController, slaveSwitchOn: boolean | undefined) {
    if (slaveSwitchOn !== undefined) {
      await assertSelectOption(t, selectorSelectByFormControlName('slaveSwitchOn'), MasterMeterPage.toSwitchOnKey(slaveSwitchOn), 'MeterMasterComponent.');
    }
  }

  private static toSwitchOnKey(switchOn: boolean | null | undefined): string | undefined {
    if (switchOn !== null && switchOn !== undefined) {
      return switchOn ? MasterMeterPage.switchOnTrue : MasterMeterPage.switchOnFalse;
    }
    return undefined;
  }

  private static fromSwitchOnKey(switchOnKey: string): boolean | null {
    if (switchOnKey === MasterMeterPage.switchOnTrue) {
      return true;
    }
    if (switchOnKey === MasterMeterPage.switchOnFalse) {
      return false;
    }
    return null;
  }
}
