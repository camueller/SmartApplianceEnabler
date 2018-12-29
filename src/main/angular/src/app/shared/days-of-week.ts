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

import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';

export interface DayOfWeek {
  id: number;
  name: string;
}

export class DaysOfWeek {

  private static daysOfWeek: DayOfWeek[] = [
    {id: 1, name: 'daysOfWeek_monday'},
    {id: 2, name: 'daysOfWeek_tuesday'},
    {id: 3, name: 'daysOfWeek_wednesday'},
    {id: 4, name: 'daysOfWeek_thursday'},
    {id: 5, name: 'daysOfWeek_friday'},
    {id: 6, name: 'daysOfWeek_saturday'},
    {id: 7, name: 'daysOfWeek_sunday'},
    {id: 8, name: 'daysOfWeek_holiday'},
  ];
  private static initialized = false;
  private static subject: Subject<DayOfWeek[]> = new Subject();

  public static getDows(translate: TranslateService): Observable<DayOfWeek[]> {
    if (! DaysOfWeek.initialized) {
      DaysOfWeek.initialized = true;
      const keys = DaysOfWeek.daysOfWeek.map(dayOfWeek => dayOfWeek.name);
      translate.get(keys).subscribe(
        translatedKeys => {
          DaysOfWeek.daysOfWeek.forEach(dayOfWeek => dayOfWeek.name = translatedKeys[dayOfWeek.name]);
          DaysOfWeek.subject.next(DaysOfWeek.daysOfWeek);
        });
    }
    return DaysOfWeek.subject;
  }
}
