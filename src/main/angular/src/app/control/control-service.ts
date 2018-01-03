import {Injectable} from '@angular/core';
import {ControlFactory} from './control-factory';
import {Observable} from 'rxjs/Observable';
import {Control} from './control';
import {ControlDefaults} from './control-defaults';
import {SaeService} from '../shared/sae-service';
import {HttpClient} from '@angular/common/http';
import {Logger} from '../log/logger';

@Injectable()
export class ControlService extends SaeService {

  controlFactory: ControlFactory;

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
    this.controlFactory = new ControlFactory(logger);
  }

  getControlDefaults(): Observable<ControlDefaults> {
    return this.http.get(`${SaeService.API}/controldefaults`)
      .map(response => this.controlFactory.defaultsFromJSON(response));
  }

  getControl(id: string): Observable<Control> {
    return this.http.get(`${SaeService.API}/control?id=${id}`)
      .map(response => {
        if (response == null) {
          return this.controlFactory.createEmptyControl();
        }
        return this.controlFactory.fromJSON(response);
      });
  }

  updateControl(control: Control, id: string): Observable<any> {
    const url = `${SaeService.API}/control?id=${id}`;
    const content = this.controlFactory.toJSON(control);
    this.logger.debug('Update control using ' + url);
    if (content != null) {
      return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
    } else {
      return this.http.delete(url, {headers: this.headersContentTypeJson, responseType: 'text'});
    }
  }
}
