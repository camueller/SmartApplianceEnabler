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

import {PwmSwitch} from '../../../../../main/angular/src/app/control/pwm/pwm-switch';
import {assertInput, inputText, selectorInputByFormControlName} from '../../shared/form';
import {ControlPage} from './control.page';

export class PwmControlPage extends ControlPage {

  public static async setPwmSwitch(t: TestController,  pwmSwitch: PwmSwitch) {
    await PwmControlPage.setType(t, PwmSwitch.TYPE);
    await PwmControlPage.setGpio(t, pwmSwitch.gpio);
    await PwmControlPage.setPwmFrequency(t, pwmSwitch.pwmFrequency);
    await PwmControlPage.setMinDutyCycle(t, pwmSwitch.minDutyCycle);
    await PwmControlPage.setMaxDutyCycle(t, pwmSwitch.maxDutyCycle);
  }

  public static async assertPwmSwitch(t: TestController,  pwmSwitch: PwmSwitch) {
    await PwmControlPage.assertType(t, PwmSwitch.TYPE);
    await PwmControlPage.assertGpio(t, pwmSwitch.gpio);
    await PwmControlPage.assertPwmFrequency(t, pwmSwitch.pwmFrequency);
    await PwmControlPage.assertMinDutyCycle(t, pwmSwitch.minDutyCycle);
    await PwmControlPage.assertMaxDutyCycle(t, pwmSwitch.maxDutyCycle);
  }

  public static async setGpio(t: TestController, gpio: number) {
    await inputText(t, selectorInputByFormControlName('gpio'), gpio && gpio.toString());
  }
  public static async assertGpio(t: TestController, gpio: number) {
    await assertInput(t, selectorInputByFormControlName('gpio'), gpio && gpio.toString());
  }

  public static async setPwmFrequency(t: TestController, pwmFrequency: number) {
    await inputText(t, selectorInputByFormControlName('pwmFrequency'), pwmFrequency && pwmFrequency.toString());
  }
  public static async assertPwmFrequency(t: TestController, pwmFrequency: number) {
    await assertInput(t, selectorInputByFormControlName('pwmFrequency'), pwmFrequency && pwmFrequency.toString());
  }

  public static async setMinDutyCycle(t: TestController, minDutyCycle: number) {
    await inputText(t, selectorInputByFormControlName('minDutyCycle'), minDutyCycle && minDutyCycle.toString());
  }
  public static async assertMinDutyCycle(t: TestController, minDutyCycle: number) {
    await assertInput(t, selectorInputByFormControlName('minDutyCycle'), minDutyCycle && minDutyCycle.toString());
  }

  public static async setMaxDutyCycle(t: TestController, maxDutyCycle: number) {
    await inputText(t, selectorInputByFormControlName('maxDutyCycle'), maxDutyCycle && maxDutyCycle.toString());
  }
  public static async assertMaxDutyCycle(t: TestController, maxDutyCycle: number) {
    await assertInput(t, selectorInputByFormControlName('maxDutyCycle'), maxDutyCycle && maxDutyCycle.toString());
  }
}
