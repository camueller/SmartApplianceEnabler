import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {Observable} from 'rxjs/Observable';
import {SettingsDefaults} from './settings-defaults';
import {SettingsFactory} from './settings-factory';
import {Settings} from './settings';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class SettingsService extends SaeService {

  constructor(protected http: HttpClient) {
    super(http);
  }

  getSettingsDefaults(): Observable<SettingsDefaults> {
    return this.http.get(`${this.api}/settingsdefaults`)
      .map(response => SettingsFactory.defaultsFromJSON(response));
  }

  getSettings(): Observable<Settings> {
    return this.http.get(`${this.api}/settings`)
      .map(settings => SettingsFactory.fromJSON(settings));
  }

  updateSettings(settings: Settings) {
    const url = `${this.api}/settings`;
    const content = SettingsFactory.toJSON(settings);
    console.log('Update settings using ' + url);
    return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
  }
}
