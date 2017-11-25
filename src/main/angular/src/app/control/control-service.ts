import {Injectable} from '@angular/core';
import {ControlFactory} from './control-factory';
import {Observable} from 'rxjs/Observable';
import {Control} from './control';
import {ControlDefaults} from './control-defaults';
import {SaeService} from '../shared/sae-service';
import {Http} from '@angular/http';

@Injectable()
export class ControlService extends SaeService {

  constructor(protected http: Http) {
    super(http);
  }

  getControlDefaults(): Observable<ControlDefaults> {
    return this.http.get(`${this.api}/controldefaults`)
      .map(response => {
        return ControlFactory.defaultsFromJSON(response.json());
      })
      .catch(this.errorHandler);
  }

  getControl(id: string): Observable<Control> {
    return this.http.get(`${this.api}/control?id=${id}`)
      .map(response => {
        if (response['_body'].length > 0) {
          return ControlFactory.fromJSON(response.json());
        }
        return ControlFactory.createEmptyControl();
      })
      .catch(this.errorHandler);
  }

  updateControl(control: Control, id: string): Observable<any> {
    const url = `${this.api}/control?id=${id}`;
    const content = ControlFactory.toJSON(control);
    console.log('Update control using ' + url);
    return this.httpPutOrDelete(url, content);
  }
}
