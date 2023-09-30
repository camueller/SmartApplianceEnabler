import { ActivatedRouteSnapshot } from '@angular/router';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {Settings} from './settings';
import {SettingsService} from './settings-service';

@Injectable()
export class SettingsResolver  {

  constructor(private settingsService: SettingsService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<Settings> {
    return this.settingsService.getSettings();
  }
}
