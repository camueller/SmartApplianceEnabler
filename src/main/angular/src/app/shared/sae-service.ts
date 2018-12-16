import {HttpClient, HttpHeaders} from '@angular/common/http';
import { environment } from '../../environments/environment';

export class SaeService {
  private static PORT = environment.port ? environment.port : window.location.port;
  private static BASE_URL = window.location.protocol + '//' + window.location.hostname + ':' + SaeService.PORT;
  public static API = SaeService.BASE_URL + '/sae';
  public static SEMP_API = SaeService.BASE_URL + '/semp';
  protected headersContentTypeJson: HttpHeaders = new HttpHeaders().set('Content-Type', 'application/json');
  protected headersContentTypeXml: HttpHeaders = new HttpHeaders().set('Content-Type', 'application/xml');

  constructor(protected http: HttpClient) {
  }
}
