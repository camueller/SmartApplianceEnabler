/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

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

export enum ValidatorType {
  required,
  pattern
}

export const ERROR_INPUT_REQUIRED = 'error.input_required';

export class ErrorMessage {

  public text: string;

  /**
   * Creates a new error message.
   * @param forControl
   * @param forValidator
   * @param key By default forControl is used to look up the message if no key is specified.
   * @param keyIsComplete if true, only key is ued to look up message
   */
  constructor(
    public forControl: string,
    public forValidator: ValidatorType,
    public key?: string,
    public keyIsComplete = false
  ) { }
}
