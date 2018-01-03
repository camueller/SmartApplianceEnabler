import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {Observable} from 'rxjs/Observable';
import {SettingsDefaults} from './settings-defaults';
import {SettingsFactory} from './settings-factory';
import {Settings} from './settings';
import {HttpClient} from '@angular/common/http';
import {Logger} from '../log/logger';

@Injectable()
export class SettingsService extends SaeService {

  settingsFactory: SettingsFactory;

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
    this.settingsFactory = new SettingsFactory(logger);
  }

  getSettingsDefaults(): Observable<SettingsDefaults> {
    return this.http.get(`${SaeService.API}/settingsdefaults`)
      .map(response => this.settingsFactory.defaultsFromJSON(response));
  }

  getSettings(): Observable<Settings> {
    return this.http.get(`${SaeService.API}/settings`)
      .map(settings => this.settingsFactory.fromJSON(settings));
  }

  updateSettings(settings: Settings) {
    const url = `${SaeService.API}/settings`;
    const content = this.settingsFactory.toJSON(settings);
    this.logger.debug('Update settings using ' + url);
    return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
  }
}
