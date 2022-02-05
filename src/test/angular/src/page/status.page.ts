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

export class StatusPage {

  public static pageSelector(t: TestController): Selector {
    return Selector('.StatusComponent');
  }

  public static async waitForPage(t: TestController): Promise<void> {
    await t.expect(this. pageExists(t)).ok();
  }

  public static async pageExists(t: TestController): Promise<boolean> {
    return (await this.pageSelector(t)).exists;
  }
}
