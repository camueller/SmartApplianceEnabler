import { ActivatedRouteSnapshot } from '@angular/router';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {SettingsService} from './settings-service';
import {SettingsDefaults} from './settings-defaults';

@Injectable()
export class SettingsDefaultsResolver  {

  constructor(private settingsService: SettingsService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<SettingsDefaults> {
    return this.settingsService.getSettingsDefaults();
  }
}
