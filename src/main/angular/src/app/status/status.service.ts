import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import {SaeService} from '../shared/sae-service';
import {HttpClient} from '@angular/common/http';
import {Status} from './status';
import {StatusFactory} from './status-factory';

@Injectable()
export class StatusService extends SaeService {

  constructor(protected http: HttpClient) {
    super(http);
  }

  getStatus(): Observable<Array<Status>> {
    return this.http.get(`${SaeService.API}/status`)
      .map((statuses: Array<Status>) => {
        return statuses.map(
          status => StatusFactory.toStatusFromJSON(status));
      });
  }

  suggestRuntime(id: string): Observable<string> {
    const url = `${SaeService.API}/runtime?id=${id}`;
    console.log('Get suggested runtime using ' + url);
    return this.http.get(url, {responseType: 'text'});
  }

  setRuntime(id: string, runtime: number): Observable<any> {
    const url = `${SaeService.API}/runtime?id=${id}&runtime=${runtime}`;
    console.log('Set runtime using ' + url);
    return this.http.put(url, '', {responseType: 'text'});
  }

  toggleAppliance(id: string, turnOn: boolean): Observable<any> {
    const url = `${SaeService.SEMP_API}`;
    const content = '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl>' +
      '<DeviceId>' + id + '</DeviceId><On>' + turnOn + '</On></DeviceControl></EM2Device>';
    console.log('Toggle appliance using ' + url);
    console.log('Content: ' + content);
    return this.http.post(url, content,
      {headers: this.headersContentTypeXml, responseType: 'text'});
  }
}
