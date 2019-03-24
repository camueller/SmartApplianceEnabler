import {Inject, Injectable} from '@angular/core';
import {HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {Logger} from '../log/logger';
import {catchError} from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(@Inject(Logger) private logger) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(catchError((err: HttpErrorResponse) => {
        if (err.error instanceof Error) {
          this.logger.error('An client-side or network error occurred:', err.error.message);
        } else {
          this.logger.error(`Backend error: code ${err.status}, error: ${err.error}, message: ${err.message}`);
        }
        return of<HttpEvent<any>>();
      }));
  }
}
