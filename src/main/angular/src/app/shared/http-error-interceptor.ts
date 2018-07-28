import {Inject, Injectable} from '@angular/core';
import {HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/empty';
import 'rxjs/add/operator/retry';
import {Logger} from '../log/logger';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(@Inject(Logger) private logger) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).catch((err: HttpErrorResponse) => {
        if (err.error instanceof Error) {
          this.logger.error('An client-side or network error occurred:', err.error.message);
        } else {
          this.logger.error(`Backend error: code ${err.status}, error: ${err.error}, message: ${err.message}`);
        }
        return Observable.of<HttpEvent<any>>();
      });
  }
}
