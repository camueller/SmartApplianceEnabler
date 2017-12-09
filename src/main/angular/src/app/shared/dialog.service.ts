/*
Copyright 2017 Google Inc. All Rights Reserved.
Use of this source code is governed by an MIT-style license that
can be found in the LICENSE file at http://angular.io/license
*/
import 'rxjs/add/observable/of';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

/**
 * Async modal dialog service
 * DialogService makes this app easier to test by faking this service.
 * TODO: better modal implementation that doesn't use window.confirm
 */
@Injectable()
export class DialogService {
  /**
   * Ask user to confirm an action. `message` explains the action and choices.
   * Returns observable resolving to `true`=confirm or `false`=cancel
   */
  confirm(message?: string): Observable<boolean> {
    const confirmation = window.confirm(message);
    return Observable.of(confirmation);
  }
}
