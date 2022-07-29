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

import {ControlValueName} from './control-value-name';
import {AbstractControl, UntypedFormArray, UntypedFormGroup, ValidatorFn} from '@angular/forms';

export function isControlValid(form: UntypedFormGroup, writeArrayName: string, writeValueArrayName: string): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    const writes = form.controls[writeArrayName] as UntypedFormArray;
    if (writes.length === 1) {
      const write = writes.at(0) as UntypedFormGroup;
      const writeValuesArray = write.controls[writeValueArrayName] as UntypedFormArray;
      if (writeValuesArray && writeValuesArray.length === 2) {
        if (hasOnAndOff(writeValuesArray?.at(0) as UntypedFormGroup, writeValuesArray?.at(1) as UntypedFormGroup)) {
          return null;
        }
      }

    } else if (writes.length === 2) {
      const write1 = writes.at(0) as UntypedFormGroup;
      const write2 = writes.at(1) as UntypedFormGroup;
      const writeValuesArray1 = write1.controls[writeValueArrayName] as UntypedFormArray;
      const writeValuesArray2 = write2.controls[writeValueArrayName] as UntypedFormArray;
      if (hasOnAndOff(writeValuesArray1?.at(0) as UntypedFormGroup, writeValuesArray2?.at(0) as UntypedFormGroup)) {
        return null;
      }
    }
    return {['custom']: true};
  };
}

function hasOnAndOff(writeValues1: UntypedFormGroup, writeValues2: UntypedFormGroup) {
  if ((writeValues1?.controls.name?.value === ControlValueName.On && writeValues2?.controls.name?.value === ControlValueName.Off)
    || (writeValues1?.controls.name?.value === ControlValueName.Off && writeValues2?.controls.name?.value === ControlValueName.On)) {
    return true;
  }
  return false;
}
