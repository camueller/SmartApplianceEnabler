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

import {SlaveElectricityMeter} from '../../../../../main/angular/src/app/meter/slave/master-electricity-meter';
import {assertSelectOption, selectOption, selectorSelectByFormControlName} from '../../shared/form';
import {MeterPage} from './meter.page';

export class SlaveMeterPage extends MeterPage {

  public static async setSlaveMeter(t: TestController, slaveMeter: SlaveElectricityMeter) {
    await SlaveMeterPage.setType(t, SlaveElectricityMeter.TYPE);
    await SlaveMeterPage.setMasterMeterAppliance(t, slaveMeter.masterElectricityMeterApplianceId);
  }

  public static async assertSlaveMeter(t: TestController, slaveMeter: SlaveElectricityMeter) {
    await SlaveMeterPage.assertType(t, SlaveElectricityMeter.TYPE);
    await SlaveMeterPage.assertMasterMeterAppliance(t, slaveMeter.masterElectricityMeterApplianceId);
  }

  public static async setMasterMeterAppliance(t: TestController, masterMeterAppliance: string) {
    await selectOption(t, selectorSelectByFormControlName('masterMeterAppliance'), masterMeterAppliance);
  }
  public static async assertMasterMeterAppliance(t: TestController, masterMeterAppliance: string) {
    await assertSelectOption(t, selectorSelectByFormControlName('masterMeterAppliance'), masterMeterAppliance);
  }
}
