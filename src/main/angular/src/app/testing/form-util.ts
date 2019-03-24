/*
Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>

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
import {By} from '@angular/platform-browser';
import {ComponentFixture} from '@angular/core/testing';

export class FormUtil {

  static setInputValue(fixture: ComponentFixture<any>, selector: string, value: string) {
    fixture.detectChanges();
    const input = fixture.debugElement.query(By.css(selector)).nativeElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  static selectOption(fixture: ComponentFixture<any>, formControlName: string, text: string) {
    fixture.detectChanges();
    const selector = `sui-select[formControlName='${formControlName}']>div.menu>sui-select-option>span:nth-child(2)`;
    const options = fixture.debugElement.queryAll(By.css(selector));
    options.forEach((option) => {
      const html = option.properties['innerHTML'];
      if (html.indexOf(text) >= 0) {
        option.nativeElement.click();
      }
    });
  }

}

