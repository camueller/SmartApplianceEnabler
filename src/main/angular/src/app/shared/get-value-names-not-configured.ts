/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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

import {FormArray, FormGroup} from '@angular/forms';

export function getValueNamesNotConfigured(outerFormArray: FormArray, valueFormControlName: string, allNames: string[]) {
  if (!outerFormArray) {
    return [];
  }
  const valueNamesFound = new Set();
  for (let index = 0; index < outerFormArray.length; index++) {
    const valueFormArray = (outerFormArray.at(index) as FormGroup).controls[valueFormControlName] as FormArray;
    for (let valueIndex = 0; valueIndex < valueFormArray?.length; valueIndex++) {
      const nameControl = (valueFormArray.at(valueIndex) as FormGroup).controls.name;
      if (nameControl) {
        valueNamesFound.add(nameControl.value);
      }
    }
  }
  const missingNames = [...allNames];
  valueNamesFound.forEach((name: string) => missingNames.splice(missingNames.indexOf(name), 1));
  return missingNames;
}
