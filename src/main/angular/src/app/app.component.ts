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

import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {MatSidenav} from '@angular/material/sidenav';
import {Subject, Subscription, takeUntil} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {LanguageService} from './shared/language-service';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {EnvPipe} from './shared/env-pipe';
import {EnvPipeService} from './shared/env-pipe-service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent implements OnInit, OnDestroy {
  @ViewChild('sidenav')
  sidenav: MatSidenav;
  watcher: Subscription;
  destroyed = new Subject<void>();
  isSmallScreen = false;
  forceSideMenuStayOpen = false;

  constructor(
    breakpointObserver: BreakpointObserver,
    private route: ActivatedRoute,
    private translate: TranslateService,
    private languageService: LanguageService,
    private envPipeService: EnvPipeService,
  ) {
    translate.setDefaultLang('de');
    translate.addLangs(['en']);
    const  currentLanguage  =  this.translate.getBrowserLang();
    // For "en" we have to force German since Browserstack does not support setting the accepted languages in the browser.
    // This would cause the tests to fail since they expect German string from Drop-Downs etc.
    if (currentLanguage !== 'de') {
      this.setLanguage('en')
    }

    breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small])
      .pipe(takeUntil(this.destroyed))
      .subscribe(result => this.isSmallScreen = result.matches)
  }

  ngOnInit(): void {
    // Allow forcing a language by query param to initial URL since testcafe-browser-provider-browserstack
    // cannot handle it otherwise:
    // e.g. http://localhost:4200/?lang=de
    this.route.queryParams
      .subscribe(params => {
        if(params?.lang) {
          console.log(`*** Force Language ${params?.lang}`);
          this.setLanguage(params?.lang);
          console.log('*** Disable tooltips; otherwise Testcafe cannot click elements hidden by tooltip');
          this.envPipeService.setMatTooltipDisabled(true);
          console.log('*** Force side menu to stay open in order remove timing issues because of menu still open hiding elements to be selected');
          this.forceSideMenuStayOpen = true;
          this.envPipeService.setForceSideMenuStayOpen(true);
        }
      });
  }

  ngOnDestroy() {
    this.destroyed.next();
    this.destroyed.complete();
  }

  get sideNavMode() {
    return this.isSmallScreen ? 'over' : 'side';
  }

  closeSideNav(force: boolean) {
    if (!this.forceSideMenuStayOpen && (force || this.isSmallScreen)) {
      this.sidenav.close();
    }
  }

  toggleSideNav() {
    if(this.forceSideMenuStayOpen) {
      if(!this.sidenav.opened) {
        this.sidenav.toggle();
      }
    } else {
      this.sidenav.toggle();
    }
  }

  private setLanguage(language: string) {
    this.translate.use(language);
    this.languageService.setLanguage(language);
  }
}
