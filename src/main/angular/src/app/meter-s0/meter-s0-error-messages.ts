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

import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ErrorMessages} from '../shared/error-messages';
import {TranslateService} from '@ngx-translate/core';

export class MeterS0ErrorMessages extends ErrorMessages {

  constructor(protected translate: TranslateService) {
    super('MeterS0Component.error.',
      [
        new ErrorMessage('gpio', ValidatorType.required),
        new ErrorMessage('gpio', ValidatorType.pattern),
        new ErrorMessage('impulsesPerKwh', ValidatorType.required),
        new ErrorMessage('impulsesPerKwh', ValidatorType.pattern),
      ], translate
    );
  }
}
