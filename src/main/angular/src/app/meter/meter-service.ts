import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {MeterFactory} from './meter-factory';
import {Observable} from 'rxjs/Observable';
import {Meter} from './meter';
import {MeterDefaults} from './meter-defaults';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class MeterService extends SaeService {

  constructor(protected http: HttpClient) {
    super(http);
  }

  getMeterDefaults(): Observable<MeterDefaults> {
    return this.http.get(`${this.api}/meterdefaults`)
      .map(response => MeterFactory.defaultsFromJSON(response));
  }

  getMeter(id: string): Observable<Meter> {
    return this.http.get(`${this.api}/meter?id=${id}`)
      .map(response => {
        if (response == null) {
          return MeterFactory.createEmptyMeter();
        }
        return MeterFactory.fromJSON(response);
      });
  }

  updateMeter(meter: Meter, id: string): Observable<any> {
    const url = `${this.api}/meter?id=${id}`;
    const content = MeterFactory.toJSON(meter);
    console.log('Update meter using ' + url);
    if (content != null) {
      return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
    } else {
      return this.http.delete(url, {headers: this.headersContentTypeJson, responseType: 'text'});
    }
  }
}
