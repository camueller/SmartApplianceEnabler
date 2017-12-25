import {HttpClient, HttpHeaders} from '@angular/common/http';

export class SaeService {
  protected api = window.location.protocol + '//' + window.location.hostname + ':8080/sae';
  protected sempApi = window.location.protocol + '//' + window.location.hostname + ':8080/semp';
  protected headersContentTypeJson: HttpHeaders = new HttpHeaders().set('Content-Type', 'application/json');
  protected headersContentTypeXml: HttpHeaders = new HttpHeaders().set('Content-Type', 'application/xml');

  constructor(protected http: HttpClient) {
  }
}
