import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {Http} from '@angular/http';
import {MeterFactory} from './meter-factory';
import {Observable} from 'rxjs/Observable';
import {Meter} from './meter';
import {MeterDefaults} from './meter-defaults';

@Injectable()
export class MeterService extends SaeService {

  constructor(protected http: Http) {
    super(http);
  }

  getMeterDefaults(): Observable<MeterDefaults> {
    return this.http.get(`${this.api}/meterdefaults`)
      .map(response => {
        return MeterFactory.defaultsFromJSON(response.json());
      })
      .catch(this.errorHandler);
  }

  getMeter(id: string): Observable<Meter> {
    return this.http.get(`${this.api}/meter?id=${id}`)
      .map(response => {
        if (response['_body'].length > 0) {
          return MeterFactory.fromJSON(response.json());
        }
        return MeterFactory.createEmptyMeter();
      })
      .catch(this.errorHandler);
  }

  updateMeter(meter: Meter, id: string): Observable<any> {
    const url = `${this.api}/meter?id=${id}`;
    const content = MeterFactory.toJSON(meter);
    console.log('Update meter using ' + url);
    return this.httpPutOrDelete(url, content);
  }
}
