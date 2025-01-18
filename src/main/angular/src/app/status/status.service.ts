import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {SaeService} from '../shared/sae-service';
import { HttpClient } from '@angular/common/http';
import {Status} from './status';
import {Logger} from '../log/logger';
import {map, tap} from 'rxjs/operators';
// import * as statusFile from './status.json';

@Injectable()
export class StatusService extends SaeService {

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
  }

  getStatus(): Observable<Array<Status>> {
    return this.http.get(`${SaeService.API}/status`)
      .pipe(map((statuses: Array<Status>) => statuses));
    // return new Observable<Array<Status>>(subscriber => {
    //   subscriber.next((statusFile as any).default);
    // });
  }

  suggestRuntime(id: string): Observable<string> {
    const url = `${SaeService.API}/runtime?id=${id}`;
    this.logger.debug('Get suggested runtime using ' + url);
    return this.http.get(url, {responseType: 'text'})
      .pipe(tap(next => this.logger.debug('Suggested runtime: ' + next)));
  }

  setRuntime(id: string, runtime: number): Observable<any> {
    const url = `${SaeService.API}/runtime?id=${id}&runtime=${runtime}`;
    this.logger.debug('Set runtime using ' + url);
    return this.http.put(url, '', {responseType: 'text'});
  }

  getSoc(id: string, evId: number): Observable<string> {
    const url = `${SaeService.API}/evcharge?applianceid=${id}&evid=${evId}`;
    this.logger.debug('Get SOC using ' + url);
    return this.http.get(url, {responseType: 'text'})
      .pipe(tap(next => this.logger.debug('SOC: ' + next)));
  }

  requestEvCharge(applianceid: string, evid: string, socCurrent?: number,
                  socTarget?: number, chargeStart?: string, chargeEnd?: string): Observable<any> {
    let url = `${SaeService.API}/evcharge?applianceid=${applianceid}&evid=${evid}`;
    if (socCurrent) {
      url += `&socCurrent=${socCurrent}`;
    }
    if (socTarget) {
      url += `&socTarget=${socTarget}`;
    }
    if (chargeStart) {
      url += `&chargeStart=${chargeStart}`;
    }
    if (chargeEnd) {
      url += `&chargeEnd=${chargeEnd}`;
    }
    this.logger.debug('Request ev charge using ' + url);
    return this.http.put(url, '', {responseType: 'text'});
  }

  clearEvChargeRequest(applianceid: string) {
    let url = `${SaeService.API}/evcharge?applianceid=${applianceid}`;
    this.logger.debug('Clear ev charge request using ' + url);
    return this.http.delete(url);
  }

  updateSoc(applianceid: string, evid: string, socCurrent: number|undefined, socTarget: number|undefined): Observable<any> {
    let url = `${SaeService.API}/evcharge?applianceid=${applianceid}&evid=${evid}`;
    if (socCurrent) {
      url += `&socCurrent=${socCurrent}`;
    }
    if (socTarget) {
      url += `&socTarget=${socTarget}`;
    }
    this.logger.debug('Update SOC using ' + url);
    return this.http.patch(url, '', {responseType: 'text'});
  }

  toggleAppliance(id: string, turnOn: boolean): Observable<any> {
    const url = `${SaeService.SEMP_API}`;
    const content = '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl>' +
      '<DeviceId>' + id + '</DeviceId><On>' + turnOn + '</On></DeviceControl></EM2Device>';
    this.logger.debug('Toggle appliance using ' + url);
    this.logger.debug('Content: ' + content);
    return this.http.post(url, content,
      {headers: this.headersContentTypeXml, responseType: 'text'});
  }

  // FIXME: verschieben in richtigen Service (obige Services auch)
  setAcceptControlRecommendations(id: string, acceptControlRecommendations: boolean): Observable<any> {
    const url = `${SaeService.API}/controlrecommendations?id=${id}&accept=${acceptControlRecommendations}`;
    return this.http.put(url, '');
  }

  resetAcceptControlRecommendations(id: string): Observable<any> {
    const url = `${SaeService.API}/controlrecommendations?id=${id}`;
    return this.http.delete(url);
  }
}
