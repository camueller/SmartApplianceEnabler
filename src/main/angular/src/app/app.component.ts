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

import {Component, OnInit, ViewChild} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {MatSidenav} from '@angular/material/sidenav';
import {filter, Subscription} from 'rxjs';
import {MediaChange, MediaObserver} from '@angular/flex-layout';
import {map} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild('sidenav')
  sidenav: MatSidenav;
  watcher: Subscription;
  activeMediaQueryAlias = '';

  constructor(
    private route: ActivatedRoute,
    private mediaObserver: MediaObserver,
    private translate: TranslateService
  ) {
    translate.setDefaultLang('de');
    translate.addLangs(['en']);
    const  currentLanguage  =  this.translate.getBrowserLang();
    // For "en" we have to force German since Browserstack does not support setting the accepted languages in the browser.
    // This would cause the tests to fail since they expect German string from Drop-Downs etc.
    if (currentLanguage !== 'de') {
      translate.use('en');
    }
  }

  ngOnInit(): void {
    // Allow forcing a language by query param to initial URL since testcafe-browser-provider-browserstack
    // cannot handle it otherwise:
    // e.g. http://localhost:4200/?lang=de
    this.route.queryParams
      .subscribe(params => {
        if(params?.lang) {
          console.log(`*** Forcing Language ${params?.lang}`);
          this.translate.use(params?.lang);
        }
      });
    this.watcher = this.mediaObserver.asObservable()
      .pipe(
        filter((changes: MediaChange[]) => changes.length > 0),
        map((changes: MediaChange[]) => changes[0])
      ).subscribe((change: MediaChange) => {
        this.activeMediaQueryAlias = change.mqAlias;
      });
  }

  get sideNavMode() {
    return this.activeMediaQueryAlias === 'xs' ? 'over' : 'side';
  }

  closeSideNav(force: boolean) {
    if (force || this.activeMediaQueryAlias === 'xs') {
      this.sidenav.close();
    }
  }

  toggleSideNav() {
    this.sidenav.toggle();
  }

}
