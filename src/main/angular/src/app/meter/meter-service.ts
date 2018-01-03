import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {MeterFactory} from './meter-factory';
import {Observable} from 'rxjs/Observable';
import {Meter} from './meter';
import {MeterDefaults} from './meter-defaults';
import {HttpClient} from '@angular/common/http';
import {Logger} from '../log/logger';

@Injectable()
export class MeterService extends SaeService {

  meterFactory: MeterFactory;

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
    this.meterFactory = new MeterFactory(logger);
  }

  getMeterDefaults(): Observable<MeterDefaults> {
    return this.http.get(`${SaeService.API}/meterdefaults`)
      .map(response => this.meterFactory.defaultsFromJSON(response));
  }

  getMeter(id: string): Observable<Meter> {
    return this.http.get(`${SaeService.API}/meter?id=${id}`)
      .map(response => {
        if (response == null) {
          return this.meterFactory.createEmptyMeter();
        }
        return this.meterFactory.fromJSON(response);
      });
  }

  updateMeter(meter: Meter, id: string): Observable<any> {
    const url = `${SaeService.API}/meter?id=${id}`;
    const content = this.meterFactory.toJSON(meter);
    console.log('Update meter using ' + url);
    if (content != null) {
      return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
    } else {
      return this.http.delete(url, {headers: this.headersContentTypeJson, responseType: 'text'});
    }
  }
}
