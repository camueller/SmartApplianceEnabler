import {Headers, Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';

export class SaeService {
  // private api = 'http://localhost:8080/sae';
  protected api = window.location.protocol + '//' + window.location.hostname + ':8080/sae';
  protected headers: Headers = new Headers();

  constructor(protected http: Http) {
    this.headers.append('Content-Type', 'application/json');
  }

  protected errorHandler(error: Error | any): Observable<any> {
    console.error(error);
    return Observable.throw(error);
  }

  httpPutOrDelete(url: string, content: string): Observable<any> {
    const observer = new Subject();
    console.log('Content: ' + content);
    if (content != null) {
      this.http.put(url, content, {headers: this.headers})
        .catch(this.errorHandler)
        .subscribe(res => {
          console.log(res);
          observer.next();
        });
    } else {
      this.http.delete(url, {headers: this.headers})
        .catch(this.errorHandler)
        .subscribe(res => {
          console.log(res);
          observer.next();
        });
    }
    return observer;
  }
}
