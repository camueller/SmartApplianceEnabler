import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {SaeService} from '../shared/sae-service';
import {Observable} from 'rxjs/Observable';
import {SettingsDefaults} from './settings-defaults';
import {SettingsFactory} from './settings-factory';
import {Settings} from './settings';

@Injectable()
export class SettingsService extends SaeService {

  constructor(protected http: Http) {
    super(http);
  }

  getSettingsDefaults(): Observable<SettingsDefaults> {
    return this.http.get(`${this.api}/settingsdefaults`)
      .map(response => {
        return SettingsFactory.defaultsFromJSON(response.json());
      })
      .catch(this.errorHandler);
  }

  getSettings(): Observable<Settings> {
    return this.http.get(`${this.api}/settings`)
      .map(response => response.json())
      .map(settings => SettingsFactory.fromJSON(settings))
      .catch(this.errorHandler);
  }

  updateSettings(settings: Settings) {
    const url = `${this.api}/settings`;
    const content = SettingsFactory.toJSON(settings);
    console.log('Update settings using ' + url);
    console.log('Content: ' + content);
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => console.log(res));
  }
}
