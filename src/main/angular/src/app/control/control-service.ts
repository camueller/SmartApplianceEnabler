import {Injectable} from '@angular/core';
import {ControlFactory} from './control-factory';
import {Observable} from 'rxjs/Observable';
import {Control} from './control';
import {ControlDefaults} from './control-defaults';
import {SaeService} from '../shared/sae-service';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class ControlService extends SaeService {

  constructor(protected http: HttpClient) {
    super(http);
  }

  getControlDefaults(): Observable<ControlDefaults> {
    return this.http.get(`${this.api}/controldefaults`)
      .map(response => ControlFactory.defaultsFromJSON(response));
  }

  getControl(id: string): Observable<Control> {
    return this.http.get(`${this.api}/control?id=${id}`)
      .map(response => {
        if (response == null) {
          return ControlFactory.createEmptyControl();
        }
        return ControlFactory.fromJSON(response);
      });
  }

  updateControl(control: Control, id: string): Observable<any> {
    const url = `${this.api}/control?id=${id}`;
    const content = ControlFactory.toJSON(control);
    console.log('Update control using ' + url);
    if (content != null) {
      return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
    } else {
      return this.http.delete(url, {headers: this.headersContentTypeJson, responseType: 'text'});
    }
  }
}
