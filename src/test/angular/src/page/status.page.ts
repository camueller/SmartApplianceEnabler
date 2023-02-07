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

import {Selector} from 'testcafe';
import {assertInput, clickButton, selectorButton, selectorInputByFormControlName} from '../shared/form';

export class StatusPage {

  public static pageSelector(t: TestController): Selector {
    return Selector('.StatusComponent');
  }

  public static async waitForPage(t: TestController): Promise<void> {
    await t.expect(await this.pageExists(t)).ok();
  }

  public static async pageExists(t: TestController): Promise<boolean> {
    return await (await this.pageSelector(t)).exists;
  }

  public static async openFlowExportDialog(t: TestController): Promise<void> {
    await clickButton(t, '.StatusComponent__nodered-export-button');
  }

  public static async closeFlowExportDialog(t: TestController): Promise<void> {
    await clickButton(t, '.FlowExportComponent__close-button');
  }

  public static async assertFlowExportDialogContent(t: TestController): Promise<void> {
    const exportJsonSelector = await Selector('#exportJson');
    await t.expect(await exportJsonSelector.exists).ok();
    const actual = await exportJsonSelector.innerText;
    await t.expect(actual.toString()).contains('"type": "ui_tab"');
  }
}
